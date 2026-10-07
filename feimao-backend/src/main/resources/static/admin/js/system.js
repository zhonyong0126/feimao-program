(function ($) {
  'use strict';
  var page = window.businessSystemPage;
  if (!page) return;
  var configs = {
    config: {url: '/api/admin/business/system/config', paged: false, cols: [['group', '分组'], ['title', '标题'], ['name', '配置名'], ['type', '类型'], ['value', '配置值']]},
    attachments: {url: '/api/admin/business/system/attachments', cols: [['category', '类别'], ['preview', '预览'], ['filename', '文件名'], ['filesize', '文件大小'], ['imagewidth', '宽度'], ['imageheight', '高度'], ['imagetype', '图片类型'], ['storage', '存储引擎'], ['mimetype', '文件类型'], ['createtime', '创建日期']]},
    addresses: {url: '/api/admin/business/addresses', cols: [['user_id', '用户'], ['name', '客户姓名'], ['administrative', '行政区'], ['address', '详细地址'], ['mobile', '手机号'], ['createtime', '创建时间'], ['updatetime', '修改时间']], fields: [['user_id', '用户', 'number'], ['name', '客户姓名'], ['administrative', '行政区'], ['address', '详细地址'], ['mobile', '手机号']]},
    identity: {url: '/api/admin/business/user-auth/identity', cols: [['nickname', '用户'], ['authentication_name', '姓名'], ['authentication_mobile', '手机号'], ['authentication_card', '身份证号'], ['authentication', '状态']], auth: true},
    staff: {url: '/api/admin/business/user-auth/staff', cols: [['nickname', '用户'], ['staff_name', '姓名'], ['staff_mobile', '手机号'], ['staff_work', '工作证明'], ['staff_authentication', '状态']], auth: true},
    'sign-logs': {url: '/api/admin/business/sign-logs', cols: [['user_id', '签到用户'], ['score', '奖励积分'], ['createtime', '签到时间']], fields: [['user_id', '签到用户', 'number'], ['score', '奖励积分', 'number']]}
  };
  var c = configs[page.section];
  var state = {rows: [], current: 1, last: 1, total: 0, editing: null, selected: {}};
  var attachmentCategories = [{category: 'unclassed', label: '未归类'}];
  var attachmentCategoryIds = [];
  var attachmentUploadRunning = false;
  function escapeHtml(value) { return $('<div>').text(value == null || value === '' ? '-' : String(value)).html(); }
  function escapeAttribute(value) { return String(value == null ? '' : value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
  function mediaUrl(value) {
    var url = String(value || '').trim();
    return url.indexOf('/uploads/') === 0 || /^https?:\/\//i.test(url) ? escapeHtml(url) : '';
  }
  function fileSize(value) {
    var bytes = Number(value);
    if (!isFinite(bytes) || bytes < 0) return escapeHtml(value);
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(bytes < 10240 ? 1 : 0) + ' KB';
    if (bytes < 1073741824) return (bytes / 1048576).toFixed(bytes < 10485760 ? 1 : 0) + ' MB';
    return (bytes / 1073741824).toFixed(1) + ' GB';
  }
  function categoryLabel(value) {
    var key = value == null || value === '' ? 'unclassed' : String(value);
    for (var i = 0; i < attachmentCategories.length; i++) if (attachmentCategories[i].category === key) return attachmentCategories[i].label;
    return value == null || value === '' ? '未归类' : value;
  }
  function cell(row, key) {
    var value = row[key];
    if (key === 'preview') {
      var attachmentUrl = mediaUrl(row.fullurl || row.url || value);
      if (!attachmentUrl) return '-';
      if (/^image\//i.test(String(row.mimetype || '')))
        return '<a href="' + attachmentUrl + '" target="_blank" rel="noopener"><img src="' + attachmentUrl + '" alt="附件预览" style="width:64px;height:48px;object-fit:cover;border-radius:4px"></a>';
      return '<a href="' + attachmentUrl + '" target="_blank" rel="noopener">查看附件</a>';
    }
    if (page.section === 'attachments' && key === 'category') return escapeHtml(categoryLabel(value));
    if (page.section === 'attachments' && key === 'filesize') return fileSize(value);
    if (key === 'staff_work' && value) {
      var url = mediaUrl(value);
      if (!url) return escapeHtml(value);
      return '<a href="' + url + '" target="_blank" rel="noopener"><img src="' + url + '" alt="工作证明" style="width:72px;height:52px;object-fit:cover;border-radius:4px"></a>';
    }
    if ((key === 'authentication' || key === 'staff_authentication') && Number(value) === 2) return '待审核';
    return escapeHtml(adminDetailValue(key, value));
  }
  function load() {
    var query = [];
    if (c.paged !== false) query.push('page=' + state.current, 'limit=10');
    $('#systemFilters input, #systemFilters select').each(function () { var value=this.value;if(value&&this.type==='date')value=Math.floor(new Date(value+'T00:00:00').getTime()/1000)+(/To$/.test(this.name)?86399:0);if(value)query.push(encodeURIComponent(this.name) + '=' + encodeURIComponent(value)); });
    api(c.url + (query.length ? '?' + query.join('&') : ''), 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      var data = response.data || [];
      state.rows = Array.isArray(data) ? data : (data.rows || data.data || []);
      state.total = Number(Array.isArray(data) ? data.length : (data.total == null ? state.rows.length : data.total));
      state.current = Number(data.current_page || data.page || 1);
      state.last = Number(data.last_page || Math.max(1, Math.ceil(state.total / 10)));
      render();
    });
  }
  function actions(row, index) {
    var buttons = page.section === 'attachments' ? '' : '<button class="btn-link detail" data-index="' + index + '">详情</button>';
    if (page.section === 'config') buttons += ' <button class="btn-link edit" data-index="' + index + '">编辑</button>';
    if (page.section === 'addresses') buttons += ' <button class="btn-link edit" data-index="' + index + '">编辑</button> <button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    if (page.section === 'attachments') buttons += '<button class="btn-link edit" data-index="' + index + '">编辑</button> <button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    if (page.section === 'sign-logs') buttons += ' <button class="btn-link edit" data-index="' + index + '">编辑</button> <button class="btn-link danger remove" data-id="' + row.id + '">删除</button>';
    if (c.auth) buttons += ' <button class="btn-link approve" data-id="' + row.id + '">通过</button> <button class="btn-link danger reject" data-id="' + row.id + '">拒绝</button>';
    return buttons;
  }
  function render() {
    var selectable = page.section === 'addresses' || page.section === 'attachments';
    var selectHead = selectable ? '<th><input type="checkbox" id="selectAllSystemRows" aria-label="全选当前页"></th>' : '';
    $('#systemHead').html(selectHead+'<th>编号</th>' + c.cols.map(function (column) { return '<th>' + column[1] + '</th>'; }).join('') + '<th>操作</th>');
    $('#systemBody').html(state.rows.length ? state.rows.map(function (row, index) { var select=selectable?'<td><input type="checkbox" class="system-row-select" value="'+row.id+'" '+(state.selected[row.id]?'checked':'')+' aria-label="选择记录"></td>':'';return '<tr>'+select+'<td>' + escapeHtml(row.id) + '</td>' + c.cols.map(function (column) { return '<td>' + cell(row, column[0]) + '</td>'; }).join('') + '<td>' + actions(row, index) + '</td></tr>'; }).join('') : '<tr><td colspan="' + (c.cols.length + (selectable ? 3 : 2)) + '" class="empty-state">暂无记录</td></tr>');
    $('#totalText').text('共 ' + state.total + ' 条记录'); $('#pageText').text(state.current + ' / ' + state.last);
    $('#prevButton').prop('disabled', state.current <= 1 || c.paged === false); $('#nextButton').prop('disabled', state.current >= state.last || c.paged === false);
    var selectedCount = Object.keys(state.selected).length;
    $('#bulkAddressDelete,#bulkAttachmentDelete,#bulkAttachmentClassify').prop('disabled', selectedCount === 0);
    if (selectable) $('#selectAllSystemRows').prop('checked', state.rows.length > 0 && state.rows.every(function (row) { return !!state.selected[row.id]; }));
  }
  function input(field, value) { return '<div class="form-group"><label>' + field[1] + '</label><input class="form-control" name="' + field[0] + '" type="' + (field[2] || 'text') + '" value="' + escapeAttribute(value) + '"></div>'; }
  function dateTimeValue(value) {
    if (value == null || value === '' || Number(value) === 0) return '';
    var date = /^\d+$/.test(String(value)) ? new Date(Number(value) * 1000) : new Date(String(value).replace(' ', 'T'));
    if (isNaN(date.getTime())) return '';
    var local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }
  function attachmentCategoryOptions(selected, filter) {
    var current = selected == null || selected === '' ? 'unclassed' : String(selected);
    var html = filter ? '<option value="">全部类别</option>' : '';
    attachmentCategories.forEach(function (item) { html += '<option value="' + escapeAttribute(item.category) + '"' + (item.category === current ? ' selected' : '') + '>' + escapeHtml(item.label) + '</option>'; });
    return html;
  }
  function attachmentUploadCategoryOptions(selected) {
    var current = selected == null ? '' : String(selected);
    var html = '<option value=""' + (current === '' ? ' selected' : '') + '>请选择一个类别</option>';
    attachmentCategories.forEach(function (item) { html += '<option value="' + escapeAttribute(item.category) + '"' + (item.category === current ? ' selected' : '') + '>' + escapeHtml(item.label) + '</option>'; });
    return html;
  }
  function attachmentEditor(row) {
    return '<div class="form-group"><label>类别</label><select class="form-control" name="category">' + attachmentCategoryOptions(row.category, false) + '</select></div>' +
      '<div class="form-group"><label>物理路径</label><input class="form-control" name="url" type="text" required value="' + escapeAttribute(row.url) + '"></div>' +
      '<div class="form-group"><label>宽度</label><input class="form-control" name="imagewidth" type="text" required value="' + escapeAttribute(row.imagewidth) + '"></div>' +
      '<div class="form-group"><label>高度</label><input class="form-control" name="imageheight" type="text" required value="' + escapeAttribute(row.imageheight) + '"></div>' +
      '<div class="form-group"><label>图片类型</label><input class="form-control" name="imagetype" type="text" required value="' + escapeAttribute(row.imagetype) + '"></div>' +
      '<div class="form-group"><label>图片帧数</label><input class="form-control" name="imageframes" type="number" min="0" value="' + escapeAttribute(row.imageframes) + '"></div>' +
      '<div class="form-group"><label>文件名</label><input class="form-control" name="filename" type="text" value="' + escapeAttribute(row.filename) + '"></div>' +
      '<div class="form-group"><label>文件大小</label><input class="form-control" name="filesize" type="number" min="0" value="' + escapeAttribute(row.filesize) + '"></div>' +
      '<div class="form-group"><label>文件类型</label><input class="form-control" name="mimetype" type="text" value="' + escapeAttribute(row.mimetype) + '"></div>' +
      '<div class="form-group"><label>透传数据</label><input class="form-control" name="extparam" type="text" value="' + escapeAttribute(row.extparam) + '"></div>' +
      '<div class="form-group"><label>上传时间</label><input class="form-control" name="uploadtime" type="datetime-local" value="' + escapeAttribute(dateTimeValue(row.uploadtime)) + '"></div>' +
      '<div class="form-group"><label>存储引擎</label><input class="form-control" name="storage" type="text" value="' + escapeAttribute(row.storage) + '"></div>';
  }
  function normalizeAttachmentCategories(data) {
    if (typeof data === 'string') {
      try { data = JSON.parse(data); } catch (ignore) { data = data ? [data] : []; }
    }
    if (data && !Array.isArray(data)) data = data.categories || data.categoryList || data.rows || data.data || data;
    var result = [];
    var seen = {};
    function add(category, label) {
      var key = category == null || category === '' ? 'unclassed' : String(category);
      if (seen[key]) return;
      seen[key] = true;
      result.push({category: key, label: String(label == null || label === '' ? key : label)});
    }
    if (Array.isArray(data)) {
      data.forEach(function (item) {
        if (typeof item === 'string') return add(item, item);
        if (!item || typeof item !== 'object') return;
        var key = item.category != null ? item.category : (item.key != null ? item.key : item.value);
        add(key, item.label != null ? item.label : (item.title != null ? item.title : (item.name != null ? item.name : key)));
      });
    } else if (data && typeof data === 'object') {
      Object.keys(data).forEach(function (key) {
        var value = data[key];
        if (value && typeof value === 'object') add(value.category != null ? value.category : key, value.label != null ? value.label : (value.title != null ? value.title : (value.name != null ? value.name : key)));
        else add(key, value);
      });
    }
    add('unclassed', '未归类');
    return result;
  }
  function refreshAttachmentCategoryControls() {
    var filter = $('#attachmentCategoryFilter'), filterValue = filter.val();
    if (filter.length) filter.html(attachmentCategoryOptions(filterValue, true)).val(filterValue || '');
    $('#attachmentCategorySelect').html(attachmentCategoryOptions('unclassed', false));
    var uploadCategory = $('#attachmentUploadCategory'), uploadValue = uploadCategory.val();
    if (uploadCategory.length) uploadCategory.html(attachmentUploadCategoryOptions(uploadValue)).val(uploadValue || '');
    if (state.rows.length) render();
  }
  function loadAttachmentCategories(done) {
    api(c.url + '/categories', 'GET').done(function (response) {
      if (response.code === 1) attachmentCategories = normalizeAttachmentCategories(response.data);
    }).always(function () {
      refreshAttachmentCategoryControls();
      if (done) done();
    });
  }
  function openAttachmentCategory(ids, selected) {
    attachmentCategoryIds = ids.map(Number).filter(function (id) { return id > 0; });
    if (!attachmentCategoryIds.length) return;
    var preferred = selected == null || selected === '' ? 'unclassed' : String(selected);
    if (selected == null) {
      preferred = '';
      $('#attachmentCategorySelect').html('<option value="">请选择一个类别</option>' + attachmentCategoryOptions('__none__', false)).val('');
    } else {
      $('#attachmentCategorySelect').html(attachmentCategoryOptions(preferred, false)).val(preferred);
    }
    $('#attachmentCategoryCount').text('已选择 ' + attachmentCategoryIds.length + ' 个附件');
    $('#attachmentCategoryMask').prop('hidden', false);
  }
  function renderAttachmentUploadFiles(files) {
    if (!files.length) {
      $('#attachmentUploadResults').empty();
      return;
    }
    $('#attachmentUploadResults').html('<div class="table-responsive"><table class="table"><thead><tr><th>文件</th><th>大小</th><th>结果</th></tr></thead><tbody>' + files.map(function (file, index) {
      return '<tr data-upload-index="' + index + '"><td>' + escapeHtml(file.name) + '</td><td>' + fileSize(file.size) + '</td><td class="upload-result">等待上传</td></tr>';
    }).join('') + '</tbody></table></div>');
  }
  function setAttachmentUploadResult(index, text, success, url) {
    var result = $('#attachmentUploadResults tr[data-upload-index="' + index + '"] .upload-result');
    var color = success === true ? 'var(--c-success)' : (success === false ? 'var(--c-danger)' : 'var(--c-text-secondary)');
    var link = url ? ' <a href="' + escapeAttribute(url) + '" target="_blank" rel="noopener">查看</a>' : '';
    result.html('<span style="color:' + color + '">' + escapeHtml(text) + '</span>' + link);
  }
  function uploadAttachments(files, category) {
    var token = localStorage.getItem('token') || '';
    var succeeded = 0, failed = 0;
    function finish() {
      attachmentUploadRunning = false;
      $('#attachmentUploadFiles,#attachmentUploadCategory').prop('disabled', false);
      $('#startAttachmentUpload').prop('disabled', false).html('<i class="fas fa-upload"></i> 开始上传');
      $('#attachmentUploadResults').prepend('<p><strong>上传完成：成功 ' + succeeded + ' 个，失败 ' + failed + ' 个。</strong></p>');
      state.current = 1;
      load();
    }
    function next(index) {
      if (index >= files.length) return finish();
      setAttachmentUploadResult(index, '上传中……', null);
      var form = new FormData();
      form.append('file', files[index]);
      form.append('category', category === 'unclassed' ? '' : category);
      form.append('token', token);
      $.ajax({url: '/api/common/upload', type: 'POST', data: form, processData: false, contentType: false, headers: {token: token}})
        .done(function (response) {
          if (response && response.code === 1) {
            succeeded++;
            var data = response.data || {};
            setAttachmentUploadResult(index, response.msg || '上传成功', true, data.fullurl || data.url || '');
          } else {
            failed++;
            setAttachmentUploadResult(index, response && response.msg ? response.msg : '上传失败', false);
          }
        })
        .fail(function (xhr) {
          failed++;
          var response = xhr.responseJSON || {};
          setAttachmentUploadResult(index, response.msg || '网络错误，上传失败', false);
        })
        .always(function () { next(index + 1); });
    }
    next(0);
  }
  function openEditor(row) {
    state.editing = row || null; $('#editorTitle').text((row ? '编辑' : '新增') + page.title);
    if (page.section === 'config') $('#systemForm').html('<div class="form-group"><label>配置名</label><input class="form-control" value="' + escapeHtml(row.name) + '" disabled></div><div class="form-group"><label>配置值</label><textarea class="form-control" name="value" rows="5">' + escapeHtml(row.value || '') + '</textarea></div>');
    else if (page.section === 'attachments') $('#systemForm').html(attachmentEditor(row));
    else $('#systemForm').html(c.fields.map(function (field) { return input(field, row && row[field[0]]); }).join(''));
    $('#editorMask').prop('hidden', false);
  }
  function detailLabel(key) {
    if (page.section === 'attachments') {
      var labels = {id:'记录编号', category:'类别', admin_id:'管理员编号', user_id:'会员编号', url:'物理路径', fullurl:'完整路径', preview:'预览', thumb_style:'缩略图样式', imagewidth:'宽度', imageheight:'高度', imagetype:'图片类型', imageframes:'图片帧数', filename:'文件名', filesize:'文件大小', mimetype:'文件类型', extparam:'透传数据', createtime:'创建日期', updatetime:'更新时间', uploadtime:'上传时间', storage:'存储引擎', sha1:'文件校验值'};
      if (labels[key]) return labels[key];
    }
    return adminDetailLabel(key);
  }
  function showDetail(row) { $('#detailBody').html(Object.keys(row).map(function (key) { return '<tr><th style="width:150px">' + escapeHtml(detailLabel(key)) + '</th><td>' + escapeHtml(adminDetailValue(key, row[key])) + '</td></tr>'; }).join('')); $('#detailMask').prop('hidden', false); }
  var configRows = [], configGroups = [], activeConfigGroup = '';
  function configOptions(row) {
    var content = row.content;
    if (content == null || content === '') return [];
    try { content = typeof content === 'string' ? JSON.parse(content) : content; } catch (ignore) { return []; }
    if (Array.isArray(content)) return content.map(function (label, index) { return {value: String(index), label: label == null ? '' : String(label)}; });
    return Object.keys(content || {}).map(function (key) { return {value: key, label: content[key] == null ? '' : String(content[key])}; });
  }
  function configSetting(row) {
    var setting = row.setting;
    if (!setting) return {};
    if (typeof setting === 'object') return setting;
    try { return JSON.parse(setting) || {}; } catch (ignore) { return {}; }
  }
  function configValueList(value) {
    if (Array.isArray(value)) return value.map(function (item) { return item == null ? '' : String(item); });
    if (value == null) return [];
    return String(value).split(',');
  }
  function configHtml(value) { return $('<div>').text(value == null ? '' : String(value)).html(); }
  function configObject(value) {
    if (value && typeof value === 'object' && !Array.isArray(value)) return value;
    if (!value) return {};
    try { var parsed = JSON.parse(String(value)); return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}; } catch (ignore) { return {}; }
  }
  function configRequired(row) { return /(^|[;,])required([;,]|$)/i.test(String(row.rule || '')); }
  function configArrayInput(row, name, required) {
    var setting = configSetting(row), value = configObject(row.value), keys = Object.keys(value);
    var keyLabel = setting.key || '键名', valueLabel = setting.value || '键值';
    return '<div class="config-array" data-config-name="' + name + '"' + (required ? ' data-required="1"' : '') + '>' +
      '<div class="config-array-head"><span>' + configHtml(keyLabel) + '</span><span>' + configHtml(valueLabel) + '</span><span>操作</span></div>' +
      '<div class="config-array-rows">' + keys.map(function (key) { return configArrayRow(key, value[key]); }).join('') + '</div>' +
      '<button class="btn btn-secondary btn-sm config-array-add" type="button"><i class="fas fa-plus"></i> 添加一项</button></div>';
  }
  function configArrayRow(key, value) {
    return '<div class="config-array-row"><input class="form-control config-array-key" type="text" value="' + escapeAttribute(key) + '" placeholder="键名"><input class="form-control config-array-value" type="text" value="' + escapeAttribute(value) + '" placeholder="键值"><button class="btn btn-danger btn-sm config-array-remove" type="button" title="删除">删除</button></div>';
  }
  function configDynamicInput(row, name, required) {
    var setting = configSetting(row), selected = configValueList(row.value), multiple = String(row.type) === 'selectpages';
    return '<div class="config-dynamic-selector" data-config-id="' + escapeAttribute(row.id) + '" data-key-field="' + escapeAttribute(setting.primarykey || setting.key || 'id') + '" data-label-field="' + escapeAttribute(setting.field || setting.value || 'name') + '" data-selected="' + escapeAttribute(selected.join(',')) + '">' +
      '<div class="config-dynamic-search"><input class="form-control" type="search" placeholder="输入关键词查询"><button class="btn btn-secondary config-dynamic-query" type="button"><i class="fas fa-search"></i> 查询</button></div>' +
      '<select class="form-control config-dynamic-select" name="' + name + '"' + (multiple ? ' multiple size="6"' : '') + required + '><option value="">正在加载...</option></select>' +
      '<small class="form-text text-muted">数据来自旧配置指定的关联表，可按显示字段或主键查询。</small></div>';
  }
  function configDateValue(type, value) {
    value = value == null ? '' : String(value);
    if (type === 'datetime' && value.indexOf(' ') > -1) return value.replace(' ', 'T');
    return value;
  }
  function configInput(row) {
    var name = escapeAttribute(row.name), values = configValueList(row.value), value = Array.isArray(row.value) ? values.join(',') : (row.value == null ? '' : String(row.value)), type = String(row.type || 'string');
    var required = configRequired(row) ? ' required' : '';
    if (type === 'text' || type === 'editor') return '<textarea class="form-control" name="' + name + '" rows="' + (type === 'editor' ? '12' : '5') + '"' + required + '>' + configHtml(value) + '</textarea>';
    if (type === 'number') return '<input class="form-control" name="' + name + '" type="number" step="any" value="' + escapeAttribute(value) + '"' + required + '>';
    if (type === 'date' || type === 'time' || type === 'datetime') return '<input class="form-control" name="' + name + '" type="' + (type === 'datetime' ? 'datetime-local' : type) + '" value="' + escapeAttribute(configDateValue(type, value)) + '"' + required + '>';
    if (type === 'datetimerange') return '<input class="form-control" name="' + name + '" type="text" value="' + escapeAttribute(value) + '" placeholder="开始时间 - 结束时间"' + required + '>';
    if (type === 'image' || type === 'images' || type === 'file' || type === 'files') return '<div class="upload-field"><input class="form-control" name="' + name + '" value="' + escapeAttribute(value) + '"' + required + '><button class="btn btn-secondary" type="button" onclick="uploadInto(this.previousElementSibling,' + ((type === 'images' || type === 'files') ? 'true' : 'false') + ')">上传</button></div>';
    if (type === 'select' || type === 'selects' || type === 'radio' || type === 'checkbox') {
      var selected = values, options = configOptions(row);
      if (type === 'select' || type === 'selects') return '<select class="form-control" name="' + name + '"' + (type === 'selects' ? ' multiple' : '') + required + '>' + options.map(function (item) { return '<option value="' + escapeAttribute(item.value) + '"' + (selected.indexOf(String(item.value)) >= 0 ? ' selected' : '') + '>' + configHtml(item.label) + '</option>'; }).join('') + '</select>';
      return '<div class="config-choice-list">' + options.map(function (item, index) { return '<label><input name="' + name + '" type="' + type + '" value="' + escapeAttribute(item.value) + '"' + (selected.indexOf(String(item.value)) >= 0 ? ' checked' : '') + ((type === 'radio' && index === 0) ? required : '') + '> ' + configHtml(item.label) + '</label>'; }).join('') + '</div>';
    }
    if (type === 'bool' || type === 'switch') return '<select class="form-control" name="' + name + '"><option value="1"' + (value === '1' ? ' selected' : '') + '>是</option><option value="0"' + (value === '0' ? ' selected' : '') + '>否</option></select>';
    if (type === 'array') return configArrayInput(row, name, required);
    if (type === 'selectpage' || type === 'selectpages') return configDynamicInput(row, name, required);
    if (type === 'custom') return '<input class="form-control" name="' + name + '" type="text" value="' + escapeAttribute(value) + '"' + required + '>';
    return '<input class="form-control" name="' + name + '" type="' + (type === 'password' ? 'password' : 'text') + '" value="' + escapeAttribute(value) + '"' + required + '>';
  }
  function dynamicSelected($wrapper) {
    var value = $wrapper.attr('data-selected') || '';
    return value ? value.split(',').filter(function (item) { return item !== ''; }) : [];
  }
  function renderDynamicOptions($wrapper) {
    var options = $wrapper.data('config-options') || {}, selected = dynamicSelected($wrapper), select = $wrapper.find('.config-dynamic-select');
    selected.forEach(function (key) { if (!Object.prototype.hasOwnProperty.call(options, key)) options[key] = key; });
    var html = select.prop('multiple') ? '' : '<option value="">请选择</option>';
    Object.keys(options).forEach(function (key) { html += '<option value="' + escapeAttribute(key) + '"' + (selected.indexOf(String(key)) >= 0 ? ' selected' : '') + '>' + configHtml(options[key]) + '</option>'; });
    select.html(html).val(select.prop('multiple') ? selected : (selected[0] || ''));
  }
  function loadDynamicOptions($wrapper, keyword, selectedOnly) {
    var id = Number($wrapper.attr('data-config-id')), keyField = $wrapper.attr('data-key-field'), labelField = $wrapper.attr('data-label-field');
    var selected = dynamicSelected($wrapper), query = ['id=' + encodeURIComponent(id), 'pageSize=50'];
    if (selectedOnly && selected.length) query.push('keyValue=' + encodeURIComponent(selected.join(',')));
    else if (keyword) query.push('keyword=' + encodeURIComponent(keyword));
    api(c.url + '/options?' + query.join('&'), 'GET').done(function (response) {
      if (!response || response.code !== 1) { renderDynamicOptions($wrapper); $wrapper.find('small').text('关联数据加载失败，已保留原值。'); return; }
      var data = response.data || {}, options = $wrapper.data('config-options') || {};
      (data.list || []).forEach(function (item) {
        var key = item[keyField], label = item[labelField];
        if (key != null) options[String(key)] = label == null || label === '' ? String(key) : String(label);
      });
      $wrapper.data('config-options', options);
      renderDynamicOptions($wrapper);
      if (selectedOnly) loadDynamicOptions($wrapper, '', false);
    }).fail(function () { renderDynamicOptions($wrapper); $wrapper.find('small').text('关联数据加载失败，已保留原值。'); });
  }
  function initDynamicSelectors() {
    $('#configForm .config-dynamic-selector').each(function () {
      var wrapper = $(this), selected = dynamicSelected(wrapper);
      loadDynamicOptions(wrapper, '', selected.length > 0);
    });
  }
  function configFormValues() {
    var values = {};
    $('#configForm .config-row').each(function () {
      var row = $(this), name = String(row.attr('data-name') || ''), type = String(row.attr('data-type') || 'string'), value;
      if (!name) return;
      if (type === 'array') {
        value = {};
        row.find('.config-array-row').each(function () { var key = $(this).find('.config-array-key').val(); if (key !== '') value[key] = $(this).find('.config-array-value').val(); });
      } else if (type === 'checkbox') value = row.find('input[type=checkbox]:checked').map(function () { return this.value; }).get();
      else if (type === 'selects' || type === 'selectpages') value = row.find('[name="' + name.replace(/"/g, '\\"') + '"]').val() || [];
      else if (type === 'radio') { var checked = row.find('input[type=radio]:checked'); if (!checked.length) return; value = checked.val(); }
      else value = row.find('[name="' + name.replace(/"/g, '\\"') + '"]').val();
      if (type === 'datetime' && value) value = String(value).replace('T', ' ');
      if (value !== undefined) values[name] = value;
    });
    return values;
  }
  function compareConfigValue(left, operator, right) {
    if (operator === '=') {
      var quoted = /^(?:'(.*)'|"(.*)")$/.exec(right);
      if (quoted) { operator = '=='; right = quoted[1] != null ? quoted[1] : quoted[2]; }
      else operator = 'in';
    }
    if (/^regex:/.test(right)) { operator = 'regex'; right = right.substring(6); }
    if (operator === 'in') return right.split(',').indexOf(String(left)) >= 0;
    if (operator === 'regex') { try { var parts = right.match(/^\/(.*?)\/([gim]*)$/); return (parts ? new RegExp(parts[1], parts[2]) : new RegExp(right)).test(String(left)); } catch (ignore) { return false; } }
    if (operator === '>' || operator === '>=' || operator === '<' || operator === '<=') { left = parseFloat(left); right = parseFloat(right); }
    if (operator === '>') return left > right;
    if (operator === '>=') return left >= right;
    if (operator === '<') return left < right;
    if (operator === '<=') return left <= right;
    if (operator === '!=') return String(left) !== String(right);
    return String(left) === String(right);
  }
  function configConditionMatches(condition, values) {
    var parts = String(condition || '').split('&&');
    if (!parts.length) return false;
    return parts.every(function (part) {
      var match = /^([a-z0-9_]+)(>=|<=|!=|==|>|<|=)(.*)$/i.exec($.trim(part));
      if (!match || !Object.prototype.hasOwnProperty.call(values, match[1])) return false;
      var left = values[match[1]];
      if (Array.isArray(left)) left = left.join(',');
      return compareConfigValue(left, match[2], match[3]);
    });
  }
  function applyConfigVisibility() {
    var values = configFormValues();
    $('#configForm .config-row[data-visible]').each(function () {
      var visible = String($(this).attr('data-visible') || '');
      var show = !visible || visible.split('||').some(function (group) { return configConditionMatches(group, values); });
      $(this).prop('hidden', !show);
    });
  }
  function renderConfigGroup(group) {
    activeConfigGroup = group;
    $('#configGroupTabs a').removeClass('active').filter('[data-group="' + escapeAttribute(group) + '"]').addClass('active');
    var rows = configRows.filter(function (row) { return row.group === group; });
    $('#configForm').html(rows.length ? rows.map(function (row) {
      var tip = row.tip ? '<small class="form-text text-muted">' + configHtml(row.tip) + '</small>' : '';
      return '<div class="form-group config-row" data-name="' + escapeAttribute(row.name) + '" data-type="' + escapeAttribute(row.type || 'string') + '" data-visible="' + escapeAttribute(row.visible || '') + '"><label>' + configHtml(row.title) + '</label><div class="config-control">' + configInput(row) + tip + '</div></div>';
    }).join('') : '<div class="empty-state">暂无配置</div>');
    initDynamicSelectors();
    applyConfigVisibility();
  }
  function loadConfig() {
    api(c.url, 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      configRows = Array.isArray(response.data) ? response.data : [];
      configGroups = [];
      configRows.forEach(function (row) { if (configGroups.indexOf(row.group) < 0) configGroups.push(row.group); });
      $('#configGroupTabs').html(configGroups.map(function (group) {
        var row = configRows.filter(function (item) { return item.group === group; })[0] || {};
        return '<a href="#" data-group="' + escapeAttribute(group) + '">' + configHtml(row.group_title || group) + '</a>';
      }).join(''));
      renderConfigGroup(configGroups.indexOf(activeConfigGroup) >= 0 ? activeConfigGroup : (configGroups[0] || ''));
    });
  }
  function initConfigPage() {
    $('#configGroupTabs').on('click', 'a', function (event) { event.preventDefault(); renderConfigGroup(String($(this).data('group'))); });
    $('#resetConfig').on('click', function () { renderConfigGroup(activeConfigGroup); });
    $('#configForm').on('click', '.config-array-add', function () { $(this).siblings('.config-array-rows').append(configArrayRow('', '')); })
      .on('click', '.config-array-remove', function () { $(this).closest('.config-array-row').remove(); applyConfigVisibility(); })
      .on('click', '.config-dynamic-query', function () { var wrapper = $(this).closest('.config-dynamic-selector'); loadDynamicOptions(wrapper, wrapper.find('input[type=search]').val(), false); })
      .on('keydown', '.config-dynamic-search input', function (event) { if (event.key === 'Enter') { event.preventDefault(); $(this).siblings('.config-dynamic-query').trigger('click'); } })
      .on('input change click', 'input,textarea,select', applyConfigVisibility)
      .on('change', '.config-dynamic-select', function () { var value = $(this).val() || []; if (!Array.isArray(value)) value = value ? [value] : []; $(this).closest('.config-dynamic-selector').attr('data-selected', value.join(',')); });
    $('#saveConfig').on('click', function () {
      var form = document.getElementById('configForm');
      var hiddenInputs = $('#configForm .config-row[hidden] :input:not(:disabled)').prop('disabled', true);
      var valid = form.checkValidity();
      if (!valid) { form.reportValidity(); hiddenInputs.prop('disabled', false); return; }
      hiddenInputs.prop('disabled', false);
      var missingChoice = $('#configForm .config-row:not([hidden])').filter(function () {
        var element = $(this), name = String(element.attr('data-name'));
        var row = configRows.filter(function (item) { return item.name === name; })[0];
        return row && row.type === 'checkbox' && configRequired(row) && element.find('input[type=checkbox]:checked').length === 0;
      });
      if (missingChoice.length) { alert('请至少选择一项' + missingChoice.first().find('> label').text()); return; }
      var missingArray = $('#configForm .config-row:not([hidden]) .config-array[data-required="1"]').filter(function () { return $(this).find('.config-array-key').filter(function () { return this.value !== ''; }).length === 0; });
      if (missingArray.length) { alert('请至少填写一项' + missingArray.first().closest('.config-row').find('> label').text()); return; }
      var body = configFormValues();
      if (!Object.keys(body).length) return;
      api(c.url, 'PUT', body).done(function (response) { if (response.code === 1) { alert('保存成功'); loadConfig(); } else alert(response.msg); });
    });
    loadConfig();
  }
  if (page.section === 'config') { initConfigPage(); return; }
  var filters = '';
  if (page.section === 'attachments') filters = '<input class="form-control" name="keyword" placeholder="记录编号/文件名/物理路径"><select class="form-control" id="attachmentCategoryFilter" name="category"><option value="">全部类别</option></select><input class="form-control" name="adminId" type="number" placeholder="管理员编号"><input class="form-control" name="userId" type="number" placeholder="会员编号"><input class="form-control" name="filename" placeholder="文件名"><input class="form-control" name="sizeMin" type="number" min="0" placeholder="最小字节"><input class="form-control" name="sizeMax" type="number" min="0" placeholder="最大字节"><input class="form-control" name="imagewidth" placeholder="宽度"><input class="form-control" name="imageheight" placeholder="高度"><input class="form-control" name="imagetype" placeholder="图片类型"><input class="form-control" name="storage" placeholder="存储引擎"><input class="form-control" name="mimetype" placeholder="文件类型"><input class="form-control" name="createdFrom" type="date" title="创建日期起"><input class="form-control" name="createdTo" type="date" title="创建日期止">';
  if (page.section === 'addresses') filters = '<input class="form-control" name="user_id" type="number" placeholder="用户"><input class="form-control" name="name" placeholder="客户姓名"><input class="form-control" name="administrative" placeholder="行政区"><input class="form-control" name="address" placeholder="详细地址"><input class="form-control" name="mobile" placeholder="手机号"><input class="form-control" name="createdFrom" type="date" title="创建时间起"><input class="form-control" name="createdTo" type="date" title="创建时间止"><input class="form-control" name="updatedFrom" type="date" title="修改时间起"><input class="form-control" name="updatedTo" type="date" title="修改时间止">';
  if (page.section === 'sign-logs') filters = '<input class="form-control" name="userId" type="number" placeholder="签到用户"><input class="form-control" name="createdFrom" type="date" title="签到开始日期"><input class="form-control" name="createdTo" type="date" title="签到结束日期">';
  $('#systemFilters').html(filters + '<button class="btn btn-primary" id="searchButton">查询</button>');
  if (page.section === 'addresses' || page.section === 'sign-logs') $('#addButton').prop('hidden', false).html('<i class="fas fa-plus"></i> 新增');
  $('#systemFilters').on('click', '#searchButton', function () { state.current = 1; load(); });
  $('#addButton').on('click', function () { openEditor(null); });
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } }); $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } });
  $('.close-editor').on('click', function () { $('#editorMask').prop('hidden', true); }); $('.close-detail').on('click', function () { $('#detailMask').prop('hidden', true); }); $('.close-category').on('click', function () { $('#attachmentCategoryMask').prop('hidden', true); }); $('.close-upload').on('click', function () { if (!attachmentUploadRunning) $('#attachmentUploadMask').prop('hidden', true); });
  $('#systemBody').on('click', '.detail', function () { showDetail(state.rows[Number($(this).data('index'))]); }).on('click', '.edit', function () { openEditor(state.rows[Number($(this).data('index'))]); }).on('click', '.remove', function () { var id = Number($(this).data('id')); if (!confirm('确定删除这条记录吗？')) return; api(c.url + '/' + id, 'DELETE').done(function (r) { if (r.code === 1) { delete state.selected[id]; load(); } else alert(r.msg); }); }).on('click', '.classify', function () { var id = Number($(this).data('id')), row = state.rows.filter(function (item) { return Number(item.id) === id; })[0]; openAttachmentCategory([id], row && row.category); }).on('click', '.approve,.reject', function () { var approved = $(this).hasClass('approve') ? 1 : 0; if (!confirm(approved ? '确认通过该认证？' : '确认拒绝该认证？')) return; api(c.url + '/examine', 'POST', {id: Number($(this).data('id')), approved: approved}).done(function (r) { if (r.code === 1) load(); else alert(r.msg); }); });
  $('#systemBody').on('change','.system-row-select',function(){if(this.checked)state.selected[this.value]=true;else delete state.selected[this.value];render();});
  $('#systemHead').on('change','#selectAllSystemRows',function(){var checked=this.checked;state.rows.forEach(function(row){if(checked)state.selected[row.id]=true;else delete state.selected[row.id];});render();});
  $('#bulkAddressDelete').on('click',function(){var ids=Object.keys(state.selected).map(Number);if(!ids.length||!confirm('确定删除选中的 '+ids.length+' 条地址吗？'))return;api(c.url,'DELETE',{ids:ids}).done(function(r){if(r.code===1){state.selected={};load();}else alert(r.msg);});});
  $('#bulkAttachmentDelete').on('click',function(){var ids=Object.keys(state.selected).map(Number);if(!ids.length||!confirm('确定删除选中的 '+ids.length+' 个附件吗？本地附件文件也会删除。'))return;api(c.url,'DELETE',{ids:ids}).done(function(r){if(r.code===1){state.selected={};load();}else alert(r.msg);});});
  $('#bulkAttachmentClassify').on('click',function(){openAttachmentCategory(Object.keys(state.selected), null);});
  $('#saveAttachmentCategory').on('click',function(){var category=$('#attachmentCategorySelect').val();if(!attachmentCategoryIds.length)return;api(c.url+'/classify','PUT',{ids:attachmentCategoryIds,category:category}).done(function(r){if(r.code===1){attachmentCategoryIds.forEach(function(id){delete state.selected[id];});$('#attachmentCategoryMask').prop('hidden',true);load();}else alert(r.msg);});});
  $('#refreshAttachments').on('click',function(){load();});
  $('#openAttachmentUpload').on('click',function(){$('#attachmentUploadCategory').html(attachmentUploadCategoryOptions('')).val('');$('#attachmentUploadFiles').val('').prop('disabled',false);$('#attachmentUploadCategory').prop('disabled',false);$('#attachmentUploadResults').empty();$('#startAttachmentUpload').prop('disabled',false).html('<i class="fas fa-upload"></i> 开始上传');$('#attachmentUploadMask').prop('hidden',false);});
  $('#attachmentUploadFiles').on('change',function(){renderAttachmentUploadFiles(Array.prototype.slice.call(this.files || []));});
  $('#startAttachmentUpload').on('click',function(){if(attachmentUploadRunning)return;var files=Array.prototype.slice.call(document.getElementById('attachmentUploadFiles').files || []);if(!files.length)return alert('请选择要上传的文件');attachmentUploadRunning=true;$('#attachmentUploadFiles,#attachmentUploadCategory').prop('disabled',true);$(this).prop('disabled',true).html('<i class="fas fa-spinner fa-spin"></i> 正在上传');uploadAttachments(files,$('#attachmentUploadCategory').val() || '');});
  $('#saveSystem').on('click', function () { var form = document.getElementById('systemForm'); if (form && !form.checkValidity()) { form.reportValidity(); return; } var body = {}; $('#systemForm').serializeArray().forEach(function (item) { body[item.name] = item.value; }); var url = c.url, method = 'POST'; if (page.section === 'config') { body = {}; body[state.editing.name] = $('#systemForm [name=value]').val(); method = 'PUT'; } else if (state.editing) { url += '/' + state.editing.id; method = 'PUT'; } if (page.section === 'attachments') { if (body.category === 'unclassed') body.category = ''; if (body.uploadtime) body.uploadtime = Math.floor(new Date(body.uploadtime).getTime() / 1000); } api(url, method, body).done(function (r) { if (r.code === 1) { $('#editorMask').prop('hidden', true); load(); } else alert(r.msg); }); });
  if (page.section === 'attachments') loadAttachmentCategories(load); else load();
})(jQuery);
