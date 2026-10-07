(function ($) {
  'use strict';

  var page = window.businessAccessPage;
  if (!page) return;

  var configs = {
    users: {
      url: '/api/admin/business/users', paged: true, member: true,
      cols: [
        ['avatar', '头像', 'avatar'], ['nickname', '昵称'], ['mobile', '手机号'],
        ['jointime', '注册时间'], ['status', '状态', 'status']
      ],
      fields: [
        ['avatar', '头像', 'avatar'], ['nickname', '昵称'], ['mobile', '手机号'],
        ['jointime', '注册时间', 'datetime-local'], ['status', '状态', 'status']
      ]
    },
    'user-groups': {
      url: '/api/admin/business/user-access/groups',
      cols: [['name', '组名'], ['rules', '权限编号'], ['status', '状态', 'status']],
      fields: [['name', '组名'], ['rule_ids', '权限编号（逗号分隔）'], ['status', '状态', 'status']]
    },
    'user-rules': {
      url: '/api/admin/business/user-access/rules', tree: true,
      cols: [['title', '标题'], ['name', '规则名'], ['pid', '上级编号'], ['weigh', '权重'], ['status', '状态', 'status']],
      fields: [['pid', '上级编号', 'number'], ['name', '规则名'], ['title', '标题'], ['remark', '备注'], ['ismenu', '菜单', 'number'], ['weigh', '权重', 'number'], ['status', '状态', 'status']]
    },
    admins: {
      url: '/api/admin/business/rbac/admins', paged: true, rbac: true, selectable: true,
      filters: 'admins', noDetail: true,
      cols: [
        ['username', '用户名'], ['nickname', '昵称'], ['groups_text', '所属组别'],
        ['mobile', '手机号'], ['status', '状态', 'status'], ['logintime', '最后登录']
      ],
      fields: [
        ['group_ids', '所属组别', 'group-multi'], ['username', '用户名'],
        ['mobile', '手机号'], ['nickname', '昵称'], ['password', '密码', 'password'],
        ['status', '状态', 'status']
      ]
    },
    'admin-groups': {
      url: '/api/admin/business/rbac/groups', tree: true, rbac: true, selectable: true,
      filters: false, noDetail: true,
      cols: [['pid', '父级'], ['name', '组名', 'group-name'], ['status', '状态', 'status']],
      fields: [
        ['pid', '父级权限组', 'group-parent'], ['name', '组名'],
        ['rule_ids', '权限', 'rule-tree'], ['status', '状态', 'status']
      ]
    },
    'admin-rules': {
      url: '/api/admin/business/rbac/rules', tree: true, ruleTree: true, rbac: true,
      selectable: true, filters: false, noDetail: true,
      cols: [
        ['title', '标题', 'rule-title'], ['icon', '图标', 'rule-icon'], ['name', '规则'],
        ['weigh', '权重'], ['status', '状态', 'status'], ['ismenu', '菜单', 'menu-toggle']
      ],
      fields: [
        ['ismenu', '菜单', 'yes-no'], ['pid', '父级', 'rule-parent'], ['name', '规则'],
        ['title', '标题'], ['url', '链接', 'text', true], ['icon', '图标'],
        ['condition', '条件', 'textarea'], ['menutype', '打开方式', 'menu-type', true],
        ['extend', '扩展属性', 'textarea', true], ['remark', '备注', 'textarea'],
        ['weigh', '权重', 'number'], ['status', '状态', 'status']
      ]
    },
    logs: {
      url: '/api/admin/business/operation-logs', paged: true,
      cols: [['username', '管理员'], ['title', '标题'], ['url', '请求地址'], ['ip', 'IP'], ['useragent', '浏览器'], ['createtime', '创建时间']],
      fields: []
    }
  };

  var config = configs[page.section];
  if (!config) return;
  var state = {rows: [], current: 1, last: 1, total: 0, editing: null};
  var context = {admin_id: 0, own_group_ids: [], groups: [], rules: [], permissions: {add: true, edit: true, delete: true}};
  var logSelected = {};
  var memberSelected = {};
  var rbacSelected = {};
  var expandedRules = {};

  function escapeText(value) {
    return $('<div>').text(value == null || value === '' ? '-' : String(value)).html();
  }

  function escapeAttribute(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function number(value) {
    var result = Number(value);
    return Number.isFinite(result) ? result : 0;
  }

  function ids(value) {
    if (Array.isArray(value)) return value.map(function (item) { return number(item.id == null ? item : item.id); }).filter(Boolean);
    return String(value == null ? '' : value).split(',').map(function (item) { return number(item.trim()); }).filter(Boolean);
  }

  function flatten(rows, output, depth, ancestors) {
    output = output || [];
    depth = depth || 0;
    ancestors = ancestors || [];
    (rows || []).forEach(function (row) {
      row._depth = depth;
      row._ancestors = ancestors.slice();
      row._hasChildren = Array.isArray(row.children) && row.children.length > 0;
      output.push(row);
      flatten(row.children, output, depth + 1, ancestors.concat([number(row.id)]));
    });
    return output;
  }

  function normalize(data) {
    if (Array.isArray(data)) return config.tree ? flatten(data) : data;
    if (data.rows) return config.tree ? flatten(data.rows) : data.rows;
    return data.data || [];
  }

  function currentSelection() {
    if (config.member) return memberSelected;
    if (config.rbac) return rbacSelected;
    return logSelected;
  }

  function selectedIds() {
    return Object.keys(currentSelection()).map(Number).filter(function (id) { return id > 0; });
  }

  function protectedRow(row) {
    if (page.section === 'admins') return number(row.id) === number(context.admin_id);
    if (page.section === 'admin-groups') return ids(context.own_group_ids).indexOf(number(row.id)) >= 0;
    return false;
  }

  function updateBulkButtons() {
    var disabled = selectedIds().length === 0;
    $('#bulkLogDelete').prop('disabled', disabled);
    $('#bulkMemberDelete,#bulkMemberNormal,#bulkMemberHidden').prop('disabled', disabled);
    $('#bulkRbacDelete').prop('disabled', disabled || !context.permissions.delete);
  }

  function appendFilters(query) {
    $('#accessFilters').find('input,select').each(function () {
      var value = this.value;
      if (!value) return;
      if (this.type === 'date' || this.type === 'datetime-local') {
        var date = new Date(value + (this.type === 'date' ? 'T00:00:00' : ''));
        if (Number.isNaN(date.getTime())) return;
        value = Math.floor(date.getTime() / 1000);
        if (this.type === 'date' && /To$/.test(this.name)) value += 86399;
      }
      query.push(encodeURIComponent(this.name || 'keyword') + '=' + encodeURIComponent(value));
    });
  }

  function load() {
    var query = [];
    if (config.paged) {
      query.push('page=' + state.current);
      query.push('limit=10');
    } else {
      query.push('x=1');
    }
    appendFilters(query);
    api(config.url + '?' + query.join('&'), 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      var data = response.data || {};
      state.rows = normalize(data);
      state.total = Number(data.total == null ? state.rows.length : data.total);
      state.current = Number(data.current_page || data.page || 1);
      state.last = Number(data.last_page || Math.max(1, Math.ceil(state.total / 10)));
      render();
    });
  }

  function memberAvatar(value, nickname) {
    var url = value || '/assets/img/avatar.png';
    return '<img class="member-avatar" src="' + escapeAttribute(url) + '" alt="' +
      escapeAttribute((nickname || '会员') + '头像') +
      '" style="width:48px;height:48px;border-radius:4px;object-fit:cover">';
  }

  function statusText(value) {
    return value === 'normal' ? '正常' : value === 'hidden' ? '隐藏' : '-';
  }

  function cellValue(row, column) {
    var key = column[0];
    var type = column[2];
    var value = row[key];
    if (type === 'avatar') return memberAvatar(value, row.nickname);
    if (type === 'status') return statusText(value);
    if (type === 'group-name') {
      return '<span style="padding-left:' + (number(row._depth) * 20) + 'px">' + escapeText(value) + '</span>';
    }
    if (type === 'rule-title') {
      var expandable = row._hasChildren || number(row.ismenu) === 1;
      var caret = expandable ? (expandedRules[row.id] ? '▾' : '▸') : '·';
      return '<button type="button" class="btn-link tree-toggle" data-id="' + number(row.id) + '" ' +
        (expandable ? '' : 'disabled') + ' style="padding-left:' + (number(row._depth) * 18) + 'px">' +
        caret + ' ' + escapeText(value) + '</button>';
    }
    if (type === 'rule-icon') {
      var icon = /^[a-zA-Z0-9 _-]+$/.test(String(value || '')) ? String(value) : '';
      return icon ? '<i class="' + escapeAttribute(icon) + '" aria-label="' + escapeAttribute(value) + '"></i>' : '-';
    }
    if (type === 'menu-toggle') {
      var enabled = number(value) === 1;
      if (context.permissions.edit) {
        return '<button type="button" class="btn-link toggle-menu" data-id="' + number(row.id) +
          '" data-value="' + (enabled ? 1 : 0) + '">' + (enabled ? '是' : '否') + '</button>';
      }
      return enabled ? '是' : '否';
    }
    if (page.section === 'logs' && key === 'useragent') value = String(value || '').split(' ')[0];
    return escapeText(adminDetailValue(key, value));
  }

  function operationButtons(row) {
    var id = number(row.id);
    if (config.member) {
      return '<button class="btn-link edit" data-id="' + id + '">编辑</button> ' +
        '<button class="btn-link danger remove" data-id="' + id + '">删除</button>';
    }
    if (config.rbac) {
      if (protectedRow(row)) return '';
      var buttons = [];
      if (context.permissions.edit) buttons.push('<button class="btn-link edit" data-id="' + id + '">编辑</button>');
      if (context.permissions.delete) buttons.push('<button class="btn-link danger remove" data-id="' + id + '">删除</button>');
      return buttons.join(' ');
    }
    return '<button class="btn-link detail" data-id="' + id + '">详情</button>' +
      (page.section !== 'logs' ? ' <button class="btn-link edit" data-id="' + id + '">编辑</button>' : '') +
      ' <button class="btn-link danger remove" data-id="' + id + '">删除</button>';
  }

  function findRow(id) {
    id = number(id);
    return state.rows.filter(function (row) { return number(row.id) === id; })[0];
  }

  function displayedRows() {
    if (!config.ruleTree) return state.rows;
    return state.rows.filter(function (row) {
      return (row._ancestors || []).every(function (parent) { return expandedRules[parent]; });
    });
  }

  function render() {
    var selectable = config.selectable || config.member || page.section === 'logs';
    var selection = currentSelection();
    var rows = displayedRows();
    var selectableRows = rows.filter(function (row) { return !protectedRow(row); });
    var checkHead = selectable ? '<th><input type="checkbox" id="selectAllRows" aria-label="全选当前页"></th>' : '';
    $('#accessHead').html(
      checkHead + '<th>ID</th>' +
      config.cols.map(function (column) { return '<th>' + column[1] + '</th>'; }).join('') +
      '<th>操作</th>'
    );
    $('#accessBody').html(rows.length ? rows.map(function (row) {
      var disabled = protectedRow(row);
      var checkbox = selectable
        ? '<td><input type="checkbox" class="row-select" value="' + number(row.id) + '" ' +
          (selection[row.id] ? 'checked ' : '') + (disabled ? 'disabled ' : '') + 'aria-label="选择记录"></td>'
        : '';
      var cells = config.cols.map(function (column) { return '<td>' + cellValue(row, column) + '</td>'; }).join('');
      return '<tr>' + checkbox + '<td>' + escapeText(row.id) + '</td>' + cells +
        '<td>' + operationButtons(row) + '</td></tr>';
    }).join('') : '<tr><td colspan="10" class="empty-state">暂无记录</td></tr>');
    $('.member-avatar').on('error', function () {
      this.onerror = null;
      this.src = '/assets/img/avatar.png';
    });
    $('#totalText').text('共 ' + state.total + ' 条记录');
    $('#pageText').text(state.current + ' / ' + state.last);
    $('#prevButton').prop('disabled', state.current <= 1 || !config.paged);
    $('#nextButton').prop('disabled', state.current >= state.last || !config.paged);
    var allSelected = selectableRows.length > 0 && selectableRows.every(function (row) { return selection[row.id]; });
    $('#selectAllRows').prop('checked', allSelected);
    updateBulkButtons();
  }

  function dateTimeInputValue(value) {
    if (value == null || value === '') return '';
    var date = /^\d+$/.test(String(value)) ? new Date(Number(value) * 1000) : new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    function pad(part) { return String(part).padStart(2, '0'); }
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
      'T' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
  }

  function rowDepth(row, rows) {
    var byId = {};
    (rows || []).forEach(function (item) { byId[number(item.id)] = item; });
    var depth = 0;
    var current = row;
    var guard = 0;
    while (current && number(current.pid) && byId[number(current.pid)] && guard++ < 50) {
      depth++;
      current = byId[number(current.pid)];
    }
    return depth;
  }

  function orderedRows(rows) {
    rows = rows || [];
    var byId = {};
    var children = {};
    var result = [];
    var visited = {};
    rows.forEach(function (row) {
      byId[number(row.id)] = row;
      var pid = number(row.pid);
      children[pid] = children[pid] || [];
      children[pid].push(row);
    });
    function visit(row) {
      var id = number(row.id);
      if (visited[id]) return;
      visited[id] = true;
      result.push(row);
      (children[id] || []).forEach(visit);
    }
    rows.forEach(function (row) {
      var pid = number(row.pid);
      if (!pid || !byId[pid]) visit(row);
    });
    rows.forEach(visit);
    return result;
  }

  function optionList(rows, selected, predicate, includeNone) {
    rows = orderedRows(rows);
    var html = includeNone ? '<option value="0"' + (number(selected) === 0 ? ' selected' : '') + '>无</option>' : '';
    (rows || []).forEach(function (row) {
      if (predicate && !predicate(row)) return;
      var prefix = new Array(rowDepth(row, rows) + 1).join('　');
      html += '<option value="' + number(row.id) + '"' +
        (number(selected) === number(row.id) ? ' selected' : '') + '>' +
        escapeText(prefix + (row.name || row.title || row.id)) + '</option>';
    });
    return html;
  }

  function groupRuleIds(groupId) {
    var group = (context.groups || []).filter(function (item) { return number(item.id) === number(groupId); })[0];
    if (!group || group.rules === '*') return (context.rules || []).map(function (rule) { return number(rule.id); });
    return ids(group.rules);
  }

  function ruleTree(value, parentId) {
    var selected = ids(value);
    var allowed = groupRuleIds(parentId);
    var rules = orderedRows((context.rules || []).filter(function (rule) { return allowed.indexOf(number(rule.id)) >= 0; }));
    return '<div class="rule-tree-controls"><label><input type="checkbox" id="checkAllRules"> 全选</label> ' +
      '<label><input type="checkbox" id="expandAllRules"> 展开全部</label></div>' +
      '<div class="rule-tree">' + rules.map(function (rule) {
        var depth = rowDepth(rule, rules);
        return '<label class="rule-tree-node" data-depth="' + depth + '" style="display:' + (depth ? 'none' : 'block') +
          ';padding-left:' + (depth * 20) + 'px"><input type="checkbox" name="rule_ids" value="' + number(rule.id) +
          '" data-parent="' + number(rule.pid) + '"' + (selected.indexOf(number(rule.id)) >= 0 ? ' checked' : '') + '> ' +
          escapeText(rule.title || rule.name) + '</label>';
      }).join('') + '</div>';
  }

  function input(field, value) {
    var name = field[0];
    var type = field[2] || 'text';
    if (type === 'status') {
      value = value || 'normal';
      return '<select class="form-control" name="status"><option value="normal"' +
        (value === 'normal' ? ' selected' : '') + '>正常</option><option value="hidden"' +
        (value === 'hidden' ? ' selected' : '') + '>隐藏</option></select>';
    }
    if (type === 'yes-no') {
      value = value == null ? 1 : number(value);
      return '<select class="form-control" name="' + name + '"><option value="1"' +
        (value === 1 ? ' selected' : '') + '>是</option><option value="0"' +
        (value === 0 ? ' selected' : '') + '>否</option></select>';
    }
    if (type === 'avatar') {
      return '<div class="upload-field"><input class="form-control" name="avatar" type="text" value="' +
        escapeAttribute(value) + '"><button class="btn btn-secondary upload-member-avatar" type="button">上传头像</button></div>';
    }
    if (type === 'group-multi') {
      var selectedGroups = ids(value);
      return '<select class="form-control" name="group_ids" multiple size="6">' +
        orderedRows(context.groups || []).map(function (group) {
          return '<option value="' + number(group.id) + '"' +
            (selectedGroups.indexOf(number(group.id)) >= 0 ? ' selected' : '') + '>' +
            escapeText(new Array(rowDepth(group, context.groups) + 1).join('　') + group.name) + '</option>';
        }).join('') + '</select>';
    }
    if (type === 'group-parent') {
      return '<select class="form-control" name="pid">' + optionList(context.groups, value, function (group) {
        return !state.editing || number(group.id) !== number(state.editing.id);
      }, false) + '</select>';
    }
    if (type === 'rule-parent') {
      return '<select class="form-control" name="pid">' + optionList(context.rules, value, function (rule) {
        return number(rule.ismenu) === 1 && (!state.editing || number(rule.id) !== number(state.editing.id));
      }, true) + '</select>';
    }
    if (type === 'rule-tree') {
      return '<div id="groupRuleTree">' + ruleTree(value, $('#accessForm [name="pid"]').val()) + '</div>';
    }
    if (type === 'textarea') {
      return '<textarea class="form-control" name="' + name + '" rows="3">' + escapeText(value == null ? '' : value) + '</textarea>';
    }
    if (type === 'menu-type') {
      value = value || 'addtabs';
      var labels = {addtabs: '选项卡', dialog: '弹窗', ajax: '异步请求', blank: '新窗口'};
      return '<select class="form-control" name="menutype">' + Object.keys(labels).map(function (key) {
        return '<option value="' + key + '"' + (value === key ? ' selected' : '') + '>' + labels[key] + '</option>';
      }).join('') + '</select>';
    }
    if (type === 'datetime-local') value = dateTimeInputValue(value);
    return '<input class="form-control" name="' + name + '" type="' + type + '" value="' +
      escapeAttribute(value == null ? '' : Array.isArray(value) ? value.join(',') : value) + '"' +
      (name === 'password' ? ' placeholder="编辑时不修改请留空" autocomplete="new-password"' : '') + '>';
  }

  function openEditor(row) {
    state.editing = row || null;
    $('#editorTitle').text(row ? (config.member ? '编辑会员' : '编辑' + page.title) : '新增' + page.title);
    var defaults = page.section === 'admin-rules' ? {ismenu: 1, icon: 'fa fa-circle-o', weigh: 0, status: 'normal', menutype: 'addtabs'} : {status: 'normal'};
    $('#accessForm').html(config.fields.map(function (field) {
      var value = row ? row[field[0]] : defaults[field[0]];
      if (row && field[0] === 'rule_ids') value = ids(row.rules);
      if (row && field[0] === 'group_ids') value = row.group_ids || row.groups;
      return '<div class="form-group"' + (field[3] ? ' data-menu-only="true"' : '') +
        '><label>' + field[1] + '</label>' + input(field, value) + '</div>';
    }).join(''));
    if (page.section === 'admin-groups') {
      var ruleIds = row ? ids(row.rules) : [];
      $('#groupRuleTree').html(ruleTree(ruleIds, $('#accessForm [name="pid"]').val()));
    }
    toggleMenuFields();
    $('#editorMask').prop('hidden', false);
  }

  function toggleMenuFields() {
    var isMenu = number($('#accessForm [name="ismenu"]').val()) === 1;
    $('#accessForm [data-menu-only]').toggle(isMenu);
  }

  function showDetail(row) {
    $('#detailBody').html(Object.keys(row).map(function (key) {
      return '<tr><th style="width:150px">' + escapeText(adminDetailLabel(key)) + '</th><td>' +
        escapeText(adminDetailValue(key, row[key])) + '</td></tr>';
    }).join(''));
    $('#detailMask').prop('hidden', false);
  }

  function filters() {
    if (config.member) {
      return '<input name="id" type="number" min="1" class="form-control" placeholder="会员编号">' +
        '<input name="nickname" class="form-control" placeholder="昵称"><input name="mobile" class="form-control" placeholder="手机号">' +
        '<span>注册时间：</span><input name="jointimeFrom" type="date" class="form-control" title="注册时间起"><span>至</span>' +
        '<input name="jointimeTo" type="date" class="form-control" title="注册时间止">' +
        '<select name="status" class="form-control"><option value="">全部状态</option><option value="normal">正常</option><option value="hidden">隐藏</option></select>';
    }
    if (config.filters === 'admins') {
      return '<input name="id" type="number" min="1" class="form-control" placeholder="管理员编号">' +
        '<input name="username" class="form-control" placeholder="用户名"><input name="nickname" class="form-control" placeholder="昵称">' +
        '<input name="mobile" class="form-control" placeholder="手机号"><span>最后登录：</span>' +
        '<input name="loginFrom" type="date" class="form-control" title="最后登录起"><span>至</span>' +
        '<input name="loginTo" type="date" class="form-control" title="最后登录止">' +
        '<select name="status" class="form-control"><option value="">全部状态</option><option value="normal">正常</option><option value="hidden">隐藏</option></select>';
    }
    if (page.section === 'logs') {
      return '<input name="adminId" type="number" class="form-control" placeholder="管理员编号">' +
        '<input name="username" class="form-control" placeholder="管理员账号"><input name="title" class="form-control" placeholder="标题">' +
        '<input name="url" class="form-control" placeholder="请求地址"><input name="ip" class="form-control" placeholder="IP">' +
        '<input name="createdFrom" type="date" class="form-control" title="创建时间起"><input name="createdTo" type="date" class="form-control" title="创建时间止">';
    }
    return config.paged ? '<input id="keyword" name="keyword" class="form-control" placeholder="关键词">' : '';
  }

  function setupFilters() {
    $('.resource-pagination').toggle(Boolean(config.paged));
    if (config.filters === false) {
      $('#accessFilters').closest('.card').hide();
      return;
    }
    $('#accessFilters').html(filters() + '<button class="btn btn-primary" id="searchButton">查询</button>');
  }

  function applyPermissions() {
    $('[data-permission="add"]').toggle(Boolean(context.permissions.add));
    $('[data-permission="delete"]').toggle(Boolean(context.permissions.delete));
    updateBulkButtons();
  }

  setupFilters();

  $('#accessFilters').on('click', '#searchButton', function () {
    state.current = 1;
    if (config.member) memberSelected = {};
    if (config.rbac) rbacSelected = {};
    load();
  });
  $('#addButton').on('click', function () { openEditor(null); });
  $('#refreshMembers,#refreshAccess').on('click', load);
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } });
  $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } });
  $('.close-editor').on('click', function () { $('#editorMask').prop('hidden', true); });
  $('.close-detail').on('click', function () { $('#detailMask').prop('hidden', true); });

  $('#accessForm')
    .on('click', '.upload-member-avatar', function () { uploadInto('#accessForm [name="avatar"]', false); })
    .on('change', '[name="ismenu"]', toggleMenuFields)
    .on('change', '[name="pid"]', function () {
      if (page.section !== 'admin-groups') return;
      var selected = $('#groupRuleTree [name="rule_ids"]:checked').map(function () { return number(this.value); }).get();
      $('#groupRuleTree').html(ruleTree(selected, this.value));
    })
    .on('change', '#checkAllRules', function () {
      $('#groupRuleTree [name="rule_ids"]').prop('checked', this.checked);
    })
    .on('change', '#expandAllRules', function () {
      $('#groupRuleTree .rule-tree-node').each(function () {
        $(this).toggle($('#expandAllRules').prop('checked') || number($(this).data('depth')) === 0);
      });
    })
    .on('change', '[name="rule_ids"]', function () {
      var id = number(this.value);
      var checked = this.checked;
      if (checked) {
        var descendants = {};
        descendants[id] = true;
        $('#groupRuleTree [name="rule_ids"]').each(function () {
          if (descendants[number($(this).data('parent'))]) {
            descendants[number(this.value)] = true;
            this.checked = true;
          }
        });
        var parent = number($(this).data('parent'));
        while (parent) {
          var parentInput = $('#groupRuleTree [name="rule_ids"][value="' + parent + '"]');
          parentInput.prop('checked', true);
          parent = number(parentInput.data('parent'));
        }
      } else {
        var descendants = {};
        descendants[id] = true;
        $('#groupRuleTree [name="rule_ids"]').each(function () {
          if (descendants[number($(this).data('parent'))]) {
            descendants[number(this.value)] = true;
            this.checked = false;
          }
        });
      }
    });

  $('#accessBody')
    .on('click', '.detail', function () {
      var row = findRow($(this).data('id'));
      if (page.section === 'logs') {
        api(config.url + '/' + row.id, 'GET').done(function (response) {
          if (response.code === 1) showDetail(response.data); else alert(response.msg);
        });
      } else showDetail(row);
    })
    .on('click', '.edit', function () { openEditor(findRow($(this).data('id'))); })
    .on('click', '.remove', function () {
      var id = number($(this).data('id'));
      var message = config.member ? '确定删除该会员吗？' : '确定删除吗？此操作可能影响账号和权限。';
      if (!confirm(message)) return;
      var url = config.url + (page.section === 'logs' ? '' : '/' + id);
      var data = page.section === 'logs' ? {ids: [id]} : null;
      api(url, 'DELETE', data).done(function (response) {
        if (response.code === 1) {
          delete memberSelected[id]; delete logSelected[id]; delete rbacSelected[id]; load();
        } else alert(response.msg);
      });
    })
    .on('click', '.tree-toggle', function () {
      var id = number($(this).data('id'));
      expandedRules[id] = !expandedRules[id];
      render();
    })
    .on('click', '.toggle-menu', function () {
      var id = number($(this).data('id'));
      api(config.url + '/' + id + '/menu', 'PUT', {ismenu: number($(this).data('value')) ? 0 : 1}).done(function (response) {
        if (response.code === 1) load(); else alert(response.msg);
      });
    })
    .on('change', '.row-select', function () {
      var selection = currentSelection();
      if (this.checked) selection[this.value] = true; else delete selection[this.value];
      updateBulkButtons();
    });

  $('#accessHead').on('change', '#selectAllRows', function () {
    var checked = this.checked;
    var selection = currentSelection();
    $('.row-select:not(:disabled)').each(function () {
      this.checked = checked;
      if (checked) selection[this.value] = true; else delete selection[this.value];
    });
    updateBulkButtons();
  });

  $('#toggleAllRules').on('click', function () {
    var expand = $(this).data('expanded') !== true;
    state.rows.forEach(function (row) { if (row._hasChildren) expandedRules[row.id] = expand; });
    $(this).data('expanded', expand).html('<i class="fas fa-' + (expand ? 'minus' : 'plus') + '"></i> ' + (expand ? '收起全部' : '显示全部'));
    render();
  });

  $('#bulkLogDelete').on('click', function () {
    var selected = selectedIds();
    if (!selected.length || !confirm('确定删除选中的 ' + selected.length + ' 条操作日志吗？')) return;
    api(config.url, 'DELETE', {ids: selected}).done(function (response) {
      if (response.code === 1) { logSelected = {}; load(); } else alert(response.msg);
    });
  });

  $('#bulkMemberDelete').on('click', function () {
    var selected = selectedIds();
    if (!selected.length || !confirm('确定删除选中的 ' + selected.length + ' 个会员吗？')) return;
    api(config.url, 'DELETE', {ids: selected}).done(function (response) {
      if (response.code === 1) { memberSelected = {}; load(); } else alert(response.msg);
    });
  });

  $('#bulkRbacDelete').on('click', function () {
    var selected = selectedIds();
    if (!selected.length || !confirm('确定删除选中的 ' + selected.length + ' 条记录吗？')) return;
    api(config.url, 'DELETE', {ids: selected}).done(function (response) {
      if (response.code === 1) { rbacSelected = {}; load(); } else alert(response.msg);
    });
  });

  function updateMemberStatus(status) {
    var selected = selectedIds();
    if (!selected.length) return;
    api(config.url + '/status', 'PUT', {ids: selected, status: status}).done(function (response) {
      if (response.code === 1) { memberSelected = {}; load(); } else alert(response.msg);
    });
  }

  $('#bulkMemberNormal').on('click', function () { updateMemberStatus('normal'); });
  $('#bulkMemberHidden').on('click', function () { updateMemberStatus('hidden'); });

  $('#saveAccess').on('click', function () {
    var data = {};
    $('#accessForm').serializeArray().forEach(function (item) {
      if (!(config.member || item.value !== '')) return;
      if (/_ids$/.test(item.name)) {
        data[item.name] = data[item.name] || [];
        String(item.value).split(',').forEach(function (part) {
          var id = number(part.trim());
          if (id && data[item.name].indexOf(id) < 0) data[item.name].push(id);
        });
      } else data[item.name] = item.value;
    });
    config.fields.forEach(function (field) {
      if (/_ids$/.test(field[0]) && data[field[0]] == null) data[field[0]] = [];
    });
    var id = state.editing && state.editing.id;
    if (config.member && !id) return alert('请选择要编辑的会员');
    api(config.url + (id ? '/' + id : ''), id ? 'PUT' : 'POST', data).done(function (response) {
      if (response.code === 1) { $('#editorMask').prop('hidden', true); load(); } else alert(response.msg);
    });
  });

  if (config.rbac) {
    api(config.url + '/context', 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      context = $.extend(true, context, response.data || {});
      applyPermissions();
      load();
    });
  } else load();
})(jQuery);
