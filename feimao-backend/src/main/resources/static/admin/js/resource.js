(function ($) {
  'use strict';

  var page = window.businessResourcePage;
  if (!page) return;

  var commonStatus = { key: 'status', label: '状态', type: 'status' };
  var configs = {
    banner: {
      showId: false,
      searchable: false,
      statusLabels: { 1: '显示', 2: '隐藏' },
      filters: ['status', 'date'],
      columns: [{ key: 'image', label: '图片', type: 'image' }, commonStatus, { key: 'createtime', label: '创建时间', type: 'datetime' }],
      fields: [{ key: 'image', label: '图片', type: 'upload', required: true }, { key: 'weigh', label: '权重', type: 'number', editOnly: true }, commonStatus]
    },
    brand: {
      showId: false,
      bulkDelete: true,
      recycleBatch: true,
      recycleNameLabel: '品牌名称',
      statusLabels: { 1: '启用', 2: '禁用' },
      filters: ['status', 'date'],
      columns: [{ key: 'weigh', label: '排序', type: 'brand-weight' }, { key: 'name', label: '品牌名称' }, commonStatus, { key: 'createtime', label: '创建时间', type: 'datetime' }],
      fields: [{ key: 'name', label: '品牌名称', required: true }, { key: 'weigh', label: '排序', type: 'number' }, commonStatus]
    },
    'platform-car': {
      showId: false,
      bulkDelete: true,
      recycleBatch: true,
      recycleNameLabel: '车辆名称',
      statusLabels: { 1: '启用', 2: '禁用' },
      filters: ['brand', 'status', 'date'],
      columns: [{ key: 'name', label: '车辆名称' }, { key: 'brand_name', label: '品牌名称' }, { key: 'image', label: '主图', type: 'image' }, { key: 'specification', label: '型号' }, commonStatus, { key: 'createtime', label: '创建时间', type: 'datetime' }],
      fields: [{ key: 'name', label: '车辆名称' }, { key: 'brand_id', label: '品牌名称', type: 'brand', required: true }, { key: 'image', label: '主图', type: 'upload', multiple: true }, { key: 'specification', label: '型号' }, { key: 'weigh', label: '排序', type: 'number', addOnly: true }, commonStatus]
    },
    maintain: {
      columns: [{ key: 'name', label: '项目名称' }, { key: 'introductory', label: '简介' }, { key: 'image', label: '图片', type: 'image' }],
      fields: [{ key: 'name', label: '项目名称', required: true }, { key: 'image', label: '图片地址', type: 'upload', required: true }, { key: 'introductory', label: '简介' }, { key: 'content', label: '详细内容', type: 'textarea' }]
    },
    coupon: {
      searchable: false,
      columns: [{ key: 'score', label: '兑换积分' }, { key: 'price', label: '使用门槛' }, { key: 'reduceprice', label: '减免金额' }, commonStatus],
      fields: [{ key: 'score', label: '兑换积分', type: 'number', required: true }, { key: 'price', label: '使用门槛', type: 'number', required: true }, { key: 'reduceprice', label: '减免金额', type: 'number', required: true }, { key: 'weigh', label: '权重', type: 'number' }, { key: 'endtime', label: '结束时间戳', type: 'number' }, commonStatus]
    },
    category: {
      columns: [{ key: 'name', label: '分类名称' }, { key: 'type', label: '类型' }, { key: 'pid', label: '上级编号' }, { key: 'nickname', label: '别名' }, { key: 'weigh', label: '权重' }, { key: 'status', label: '状态', type: 'status-string' }],
      fields: [{ key: 'pid', label: '上级分类编号', type: 'number' }, { key: 'type', label: '分类类型', required: true }, { key: 'name', label: '分类名称', required: true }, { key: 'nickname', label: '别名' }, { key: 'diyname', label: '自定义名称' }, { key: 'flag', label: '标志（热门、首页、推荐）' }, { key: 'image', label: '图片地址', type: 'upload' }, { key: 'keywords', label: '关键词' }, { key: 'description', label: '描述', type: 'textarea' }, { key: 'weigh', label: '权重', type: 'number' }, { key: 'status', label: '状态', type: 'status-string' }]
    },
    malfunction: {
      columns: [{ key: 'user_id', label: '用户编号' }, { key: 'number', label: '订单编号' }, { key: 'image', label: '故障图片', type: 'image' }, { key: 'content', label: '故障内容' }, { key: 'createtime', label: '提交时间', type: 'datetime' }],
      fields: []
    }
  };
  var config = configs[page.resource];
  var state = { current: 1, limit: 10, total: 0, last: 1, rows: [], brands: [], editingId: null, recycle: false };
  var endpoint = '/api/admin/business/resources/' + page.resource;

  function escapeHtml(value) {
    return $('<div>').text(value == null ? '' : String(value)).html();
  }

  function formatTime(value) {
    if (value == null || value === '') return '-';
    var date = new Date(Number(value) * 1000);
    if (isNaN(date.getTime())) return escapeHtml(value);
    var pad = function (part) { return String(part).padStart(2, '0'); };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
  }

  function statusLabel(value) {
    if (config.statusLabels) return config.statusLabels[Number(value)] || '-';
    return Number(value) === 1 ? '正常' : '禁用';
  }

  function renderCell(row, column) {
    var value = row[column.key];
    if (column.type === 'image') {
      if (!value) return '-';
      var raw = String(value).split(',').map(function (item) { return item.trim(); }).filter(Boolean)[0] || '';
      if (!raw) return '-';
      if (!/^\/(uploads|assets)\//.test(raw) && !/^https?:\/\//i.test(raw)) return escapeHtml(raw);
      var url = escapeHtml(raw);
      return '<a href="' + url + '" target="_blank" rel="noopener"><img class="resource-thumb" src="' + url + '" alt="图片"></a>';
    }
    if (column.type === 'brand-weight') return '<input class="form-control brand-weight" style="width:90px" type="number" min="0" data-id="' + row.id + '" data-original="' + escapeHtml(value == null ? '' : value) + '" value="' + escapeHtml(value == null ? '' : value) + '" aria-label="排序">';
    if (column.type === 'datetime') return formatTime(value);
    if (column.type === 'status') {
      var label = statusLabel(value);
      var badge = Number(value) === 1 ? 'badge-success' : 'badge-secondary';
      if (page.mutable && !state.recycle) return '<button class="btn-link toggle-status" data-id="' + row.id + '" data-status="' + (Number(value) === 1 ? 2 : 1) + '" title="点击切换状态"><span class="badge ' + badge + '">' + label + '</span></button>';
      return '<span class="badge ' + badge + '">' + label + '</span>';
    }
    if (column.type === 'status-string') return value === 'normal' ? '<span class="badge badge-success">正常</span>' : '<span class="badge badge-secondary">禁用</span>';
    return escapeHtml(value == null || value === '' ? '-' : value);
  }

  function selectedIds() {
    return $('#tableBody .row-select:checked').map(function () { return Number(this.value); }).get();
  }

  function syncSelection() {
    var boxes = $('#tableBody .row-select');
    var checked = boxes.filter(':checked');
    $('#selectPage').prop('checked', boxes.length > 0 && checked.length === boxes.length);
    $('#bulkDeleteButton,#bulkRestoreButton,#bulkDestroyButton').prop('disabled', checked.length === 0);
  }

  function renderTable() {
    var recycleBatch = !!config.recycleBatch && state.recycle;
    var selectable = recycleBatch || (!!config.bulkDelete && !state.recycle);
    var showId = recycleBatch || config.showId !== false;
    var columns = recycleBatch
      ? [{ key: 'name', label: config.recycleNameLabel }, { key: 'deletetime', label: '删除时间', type: 'datetime' }]
      : config.columns;
    var headers = (selectable ? '<th><input id="selectPage" type="checkbox" aria-label="选择当前页"></th>' : '') + (showId ? '<th>ID</th>' : '') + columns.map(function (column) { return '<th>' + column.label + '</th>'; }).join('');
    if (page.mutable) headers += '<th>操作</th>';
    $('#tableHead').html(headers);
    if (!state.rows.length) {
      $('#tableBody').html('<tr><td colspan="' + (columns.length + (page.mutable ? 1 : 0) + (showId ? 1 : 0) + (selectable ? 1 : 0)) + '" class="empty-state">暂无数据</td></tr>');
    } else {
      $('#tableBody').html(state.rows.map(function (row, index) {
        var cells = (selectable ? '<td><input class="row-select" type="checkbox" value="' + row.id + '" aria-label="选择记录"></td>' : '') + (showId ? '<td>' + escapeHtml(row.id) + '</td>' : '') + columns.map(function (column) { return '<td>' + renderCell(row, column) + '</td>'; }).join('');
        if (page.mutable) cells += state.recycle ? '<td><button class="btn-link restore-row" data-id="' + row.id + '">还原</button> <button class="btn-link danger destroy-row" data-id="' + row.id + '">销毁</button></td>' : '<td><button class="btn-link edit-row" data-index="' + index + '">编辑</button> <button class="btn-link danger delete-row" data-id="' + row.id + '">删除</button></td>';
        return '<tr>' + cells + '</tr>';
      }).join(''));
    }
    $('#bulkDeleteButton').prop('hidden', state.recycle || !selectable).prop('disabled', true);
    $('#bulkRestoreButton,#bulkDestroyButton').prop('hidden', !recycleBatch).prop('disabled', true);
    $('#restoreAllButton,#destroyAllButton').prop('hidden', !recycleBatch).prop('disabled', recycleBatch && state.total === 0);
    $('#totalText').text('共 ' + state.total + ' 条记录');
    $('#pageText').text(state.current + ' / ' + state.last);
    $('#prevButton').prop('disabled', state.current <= 1);
    $('#nextButton').prop('disabled', state.current >= state.last);
  }

  function timestamp(value, endOfDay) {
    if (!value) return '';
    var parsed = new Date(value + (value.length === 10 ? 'T00:00:00' : ''));
    if (isNaN(parsed.getTime())) return '';
    return Math.floor(parsed.getTime() / 1000) + (endOfDay && value.length === 10 ? 86399 : 0);
  }

  function queryString() {
    var query = '?page=' + state.current + '&limit=' + state.limit;
    if (state.recycle) return query;
    query += '&keyword=' + encodeURIComponent($('#keyword').val() || '');
    if (page.resource === 'category') query += '&type=' + encodeURIComponent($('#categoryType').val() || '');
    if ($('#statusFilter').length && $('#statusFilter').val()) query += '&status=' + $('#statusFilter').val();
    if ($('#brandFilter').length && $('#brandFilter').val()) query += '&brandId=' + $('#brandFilter').val();
    var from = timestamp($('#createdFrom').val(), false);
    var to = timestamp($('#createdTo').val(), true);
    if (from) query += '&createdFrom=' + from;
    if (to) query += '&createdTo=' + to;
    return query;
  }

  function load() {
    $('#tableBody').html('<tr><td class="empty-state">正在加载...</td></tr>');
    api(endpoint + (state.recycle ? '/recycle' : '') + queryString(), 'GET')
      .done(function (response) {
        if (response.code !== 1) return alert(response.msg || '加载失败');
        var data = response.data || {};
        state.rows = data.data || [];
        state.total = Number(data.total || 0);
        state.current = Number(data.current_page || 1);
        state.last = Number(data.last_page || 1);
        renderTable();
      }).fail(function () { alert('加载失败，请重新登录后重试'); });
  }

  function brandOptions(selected, allLabel) {
    var first = allLabel == null ? '<option value="">请选择品牌</option>' : '<option value="">' + allLabel + '</option>';
    return first + state.brands.map(function (brand) { return '<option value="' + brand.id + '"' + (Number(selected) === Number(brand.id) ? ' selected' : '') + '>' + escapeHtml(brand.name) + '</option>'; }).join('');
  }

  function input(field, value) {
    var required = field.required ? ' required' : '';
    if (field.type === 'textarea') return '<textarea class="form-control" name="' + field.key + '" rows="4"' + required + '>' + escapeHtml(value || '') + '</textarea>';
    if (field.type === 'brand') return '<select class="form-select" name="brand_id"' + required + '>' + brandOptions(value, null) + '</select>';
    if (field.type === 'status') {
      var labels = config.statusLabels || { 1: '正常', 2: '禁用' };
      return '<select class="form-select" name="status"><option value="1">' + labels[1] + '</option><option value="2"' + (Number(value) === 2 ? ' selected' : '') + '>' + labels[2] + '</option></select>';
    }
    if (field.type === 'status-string') return '<select class="form-select" name="status"><option value="normal">正常</option><option value="hidden"' + (value === 'hidden' ? ' selected' : '') + '>禁用</option></select>';
    if (field.type === 'upload') return '<div class="upload-field"><input class="form-control" name="' + field.key + '" value="' + escapeHtml(value || '') + '"' + required + '><button class="btn btn-secondary" type="button" onclick="uploadInto(this.previousElementSibling,' + (field.multiple ? 'true' : 'false') + ')">上传</button></div>';
    return '<input class="form-control" name="' + field.key + '" type="' + (field.type === 'number' ? 'number' : 'text') + '" value="' + escapeHtml(value == null ? '' : value) + '"' + required + '>';
  }

  function openEditor(row) {
    state.editingId = row ? row.id : null;
    $('#editorTitle').text((row ? '编辑' : '新增') + page.title);
    var fields = config.fields.filter(function (field) { return !(row && field.addOnly) && !(!row && field.editOnly); });
    $('#editorForm').html(fields.map(function (field) { return '<div class="form-group"><label>' + field.label + '</label>' + input(field, row && row[field.key]) + '</div>'; }).join(''));
    $('#editorMask').prop('hidden', false);
  }

  function closeEditor() { $('#editorMask').prop('hidden', true); }

  function renderFilters() {
    var filters = config.filters || [];
    var html = '';
    if (filters.indexOf('brand') >= 0) html += '<select id="brandFilter" class="form-select">' + brandOptions(null, '全部品牌') + '</select>';
    if (filters.indexOf('status') >= 0) {
      var labels = config.statusLabels || { 1: '正常', 2: '禁用' };
      html += '<select id="statusFilter" class="form-select"><option value="">全部状态</option><option value="1">' + labels[1] + '</option><option value="2">' + labels[2] + '</option></select>';
    }
    if (filters.indexOf('date') >= 0) html += '<input id="createdFrom" class="form-control" type="date" title="创建开始日期"><span class="text-muted">至</span><input id="createdTo" class="form-control" type="date" title="创建结束日期">';
    $('#resourceFilters').html(html);
    if (page.resource === 'category') $('#resourceFilters').prepend('<input id="categoryType" class="form-control" placeholder="分类类型（如 page）">');
  }

  function loadBrands(done) {
    if (page.resource !== 'platform-car') return done();
    api('/api/admin/business/resources/brand?page=1&limit=100', 'GET').done(function (response) {
      state.brands = response.code === 1 ? ((response.data || {}).data || []) : [];
      renderFilters();
    }).always(done);
  }

  $('#searchButton').on('click', function () { state.current = 1; load(); });
  $('#resetButton').on('click', function () { $('#keyword,#categoryType,#statusFilter,#brandFilter,#createdFrom,#createdTo').val(''); state.current = 1; load(); });
  $('#refreshButton').on('click', load);
  $('#keyword').toggle(config.searchable !== false).on('keydown', function (event) { if (event.key === 'Enter') { state.current = 1; load(); } });
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } });
  $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } });
  $('#recycleButton').on('click', function () {
    state.recycle = !state.recycle;
    state.current = 1;
    $(this).html(state.recycle ? '<i class="fas fa-arrow-left"></i> 返回列表' : '<i class="fas fa-recycle"></i> 回收站');
    $('#addButton,#resourceFilterCard').prop('hidden', state.recycle);
    load();
  });
  $('#addButton').on('click', function () { openEditor(null); });
  $('#closeEditor,#cancelEditor').on('click', closeEditor);
  $('#tableHead').on('change', '#selectPage', function () { $('#tableBody .row-select').prop('checked', this.checked); syncSelection(); });
  $('#tableBody').on('change', '.row-select', syncSelection);
  $('#bulkDeleteButton').on('click', function () {
    var ids = selectedIds();
    if (!ids.length || !confirm('确定删除选中的 ' + ids.length + ' 条记录吗？')) return;
    api(endpoint, 'DELETE', { ids: ids }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '删除失败'); });
  });
  $('#bulkRestoreButton').on('click', function () {
    var ids = selectedIds();
    if (!ids.length || !confirm('确定恢复选中的 ' + ids.length + ' 条记录吗？')) return;
    api(endpoint + '/restore', 'POST', { ids: ids }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '恢复失败'); });
  });
  $('#bulkDestroyButton').on('click', function () {
    var ids = selectedIds();
    if (!ids.length || !confirm('彻底删除后无法恢复，确定删除选中的 ' + ids.length + ' 条记录吗？')) return;
    api(endpoint + '/destroy', 'DELETE', { ids: ids }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '彻底删除失败'); });
  });
  $('#restoreAllButton').on('click', function () {
    if (!confirm('确定恢复回收站中的全部记录吗？')) return;
    api(endpoint + '/restore', 'POST', {}).done(function (response) { if (response.code === 1) { state.current = 1; load(); } else alert(response.msg || '全部还原失败'); });
  });
  $('#destroyAllButton').on('click', function () {
    if (!confirm('彻底删除后无法恢复，确定删除回收站中的全部记录吗？')) return;
    api(endpoint + '/destroy', 'DELETE', {}).done(function (response) { if (response.code === 1) { state.current = 1; load(); } else alert(response.msg || '清空回收站失败'); });
  });
  $('#tableBody').on('click', '.edit-row', function () { openEditor(state.rows[Number($(this).data('index'))]); });
  $('#tableBody').on('click', '.delete-row', function () {
    if (!confirm('确定删除该记录吗？')) return;
    api(endpoint + '/' + $(this).data('id'), 'DELETE').done(function (response) { if (response.code === 1) load(); else alert(response.msg); });
  });
  $('#tableBody').on('click', '.toggle-status', function () {
    api(endpoint + '/status', 'PUT', { ids: [Number($(this).data('id'))], status: Number($(this).data('status')) }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '状态修改失败'); });
  });
  $('#tableBody').on('change', '.brand-weight', function () {
    var field = $(this);
    if (String(field.val()) === String(field.data('original'))) return;
    api(endpoint + '/' + field.data('id') + '/sort', 'POST', { new_sort: field.val() }).done(function (response) { if (response.code === 1) load(); else { alert(response.msg || '排序失败'); field.val(field.data('original')); } });
  });
  $('#tableBody').on('click', '.restore-row', function () { api(endpoint + '/restore/' + $(this).data('id'), 'POST', {}).done(function (response) { if (response.code === 1) load(); else alert(response.msg); }); });
  $('#tableBody').on('click', '.destroy-row', function () { if (!confirm('彻底删除后无法恢复，确定继续吗？')) return; api(endpoint + '/destroy/' + $(this).data('id'), 'DELETE').done(function (response) { if (response.code === 1) load(); else alert(response.msg); }); });
  $('#saveEditor').on('click', function () {
    var form = document.getElementById('editorForm');
    if (!form.reportValidity()) return;
    var data = {};
    $(form).serializeArray().forEach(function (item) { data[item.name] = item.value; });
    api(endpoint + (state.editingId ? '/' + state.editingId : ''), state.editingId ? 'PUT' : 'POST', data)
      .done(function (response) { if (response.code === 1) { closeEditor(); load(); } else alert(response.msg || '保存失败'); });
  });

  renderFilters();
  loadBrands(load);
})(jQuery);
