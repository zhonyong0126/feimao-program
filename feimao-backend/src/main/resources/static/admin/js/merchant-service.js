(function ($) {
  'use strict';
  var page = window.businessMerchantServicePage;
  if (!page) return;

  var base = '/api/admin/business/merchant-services/';
  var endpoint = base + page.service;
  var initialMerchantId = new URLSearchParams(window.location.search).get('merchantId') || '';
  var state = { current: 1, last: 1, total: 0, rows: [], merchants: [], brands: [], options: [], editingId: null };
  var carFields = [
    { key: 'brand_id', label: '车辆品牌', type: 'brand', required: true },
    { key: 'platform_car_id', label: '选择车辆', type: 'car', required: true },
    { key: 'merchant_id', label: '商家', type: 'merchant', required: true },
    { key: 'images', label: '车辆轮播图', type: 'uploads', required: true },
    { key: 'intro', label: '车辆详情图', type: 'uploads' },
    { key: 'cash_price', label: '租车押金', type: 'number', defaultValue: 0 },
    { key: 'month_price', label: '月租单价（一个月）', type: 'number', defaultValue: 0 },
    { key: 'day_price', label: '日租单价（一天）', type: 'number', defaultValue: 0 },
    { key: 'hour_price', label: '时租单价（一小时）', type: 'number', defaultValue: 0 },
    { key: 'total_stock', label: '总库存', type: 'number', defaultValue: 0, required: true },
    { key: 'status', label: '状态', type: 'status' }
  ];
  var maintainFields = [
    { key: 'merchant_id', label: '商户', type: 'merchant', required: true },
    { key: 'maintain_id', label: '维修项目', type: 'option', required: true },
    { key: 'price', label: '服务价格', type: 'number', required: true }
  ];
  var fields = page.service === 'cars' ? carFields : maintainFields;

  function escapeHtml(value) { return $('<div>').text(value == null ? '' : String(value)).html(); }
  function text(value) { return value == null || value === '' ? '-' : escapeHtml(value); }
  function optionHtml(items, selected, label) {
    return '<option value="">请选择' + label + '</option>' + items.map(function (item) {
      return '<option value="' + item.id + '"' + (Number(selected) === Number(item.id) ? ' selected' : '') + '>' + text(item.name) + '</option>';
    }).join('');
  }
  function image(value) {
    var url = String(value || '').split(',')[0].trim();
    if (!url) return '-';
    if (!/^\/(uploads|assets)\//.test(url) && !/^https?:\/\//i.test(url)) return text(url);
    var safe = text(url);
    return '<a href="' + safe + '" target="_blank" rel="noopener"><img class="resource-thumb" src="' + safe + '" alt="车辆图片"></a>';
  }
  function time(value) {
    if (value == null || value === '') return '-';
    var date = new Date(Number(value) * 1000);
    if (isNaN(date.getTime())) return text(value);
    var pad = function (part) { return String(part).padStart(2, '0'); };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
  }
  function timestamp(value, endOfDay) {
    if (!value) return '';
    var date = new Date(value + 'T00:00:00');
    if (isNaN(date.getTime())) return '';
    return Math.floor(date.getTime() / 1000) + (endOfDay ? 86399 : 0);
  }
  function carOptions(brandId, selected) {
    var items = state.options.filter(function (item) { return !brandId || Number(item.brand_id) === Number(brandId); });
    return optionHtml(items, selected, '车辆');
  }
  function fillFilters() {
    $('#merchantFilter').html('<option value="">全部商家</option>' + state.merchants.map(function (merchant) { return '<option value="' + merchant.id + '">' + text(merchant.name) + '</option>'; }).join(''));
    $('#brandFilter').html('<option value="">全部品牌</option>' + state.brands.map(function (brand) { return '<option value="' + brand.id + '">' + text(brand.name) + '</option>'; }).join(''));
  }
  function loadOptions(done) {
    var cars = page.service === 'cars';
    var pending = cars ? 3 : 2;
    function complete() { pending--; if (!pending) { fillFilters(); if (page.service === 'cars' && initialMerchantId) $('#merchantFilter').val(initialMerchantId); if (done) done(); } }
    api('/api/admin/business/partners/merchant?page=1&limit=100', 'GET').done(function (response) {
      state.merchants = response.code === 1 ? ((response.data || {}).data || []) : [];
    }).always(complete);
    api(cars ? base + 'platform-cars' : base + 'maintain-options?page=1&limit=100', 'GET').done(function (response) {
      state.options = response.code === 1 ? (Array.isArray(response.data) ? response.data : ((response.data || {}).data || [])) : [];
    }).always(complete);
    if (cars) api('/api/admin/business/resources/brand?page=1&limit=100', 'GET').done(function (response) {
      state.brands = response.code === 1 ? ((response.data || {}).data || []) : [];
    }).always(complete);
  }
  function query() {
    var merchant = $('#merchantFilter').val();
    var keyword = $('#keyword').val() || '';
    var result = '?page=' + state.current + '&limit=10' + (merchant ? '&merchantId=' + merchant : '') + (keyword ? '&keyword=' + encodeURIComponent(keyword) : '');
    if (page.service === 'cars') {
      var brand = $('#brandFilter').val();
      var status = $('#statusFilter').val();
      var from = timestamp($('#createdFrom').val(), false);
      var to = timestamp($('#createdTo').val(), true);
      if (brand) result += '&brandId=' + brand;
      if (status) result += '&status=' + status;
      if (from) result += '&createdFrom=' + from;
      if (to) result += '&createdTo=' + to;
    }
    return result;
  }
  function load() {
    api(endpoint + query(), 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg || '加载失败');
      var data = response.data || {};
      state.rows = data.data || [];
      state.total = Number(data.total || 0);
      state.current = Number(data.current_page || 1);
      state.last = Number(data.last_page || 1);
      render();
    }).fail(function () { alert('加载失败，请重新登录后重试'); });
  }
  function selectedIds() {
    return $('#serviceBody .row-select:checked').map(function () { return Number(this.value); }).get();
  }
  function syncSelection() {
    var boxes = $('#serviceBody .row-select');
    var selected = boxes.filter(':checked');
    $('#selectPage').prop('checked', boxes.length > 0 && selected.length === boxes.length);
    $('#bulkDeleteButton').prop('disabled', selected.length === 0);
  }
  function statusButton(row) {
    var normal = Number(row.status) === 1;
    return '<button class="btn-link toggle-status" data-id="' + row.id + '" data-status="' + (normal ? 2 : 1) + '" title="点击切换状态"><span class="badge ' + (normal ? 'badge-success' : 'badge-secondary') + '">' + (normal ? '上架' : '下架') + '</span></button>';
  }
  function render() {
    var cars = page.service === 'cars';
    $('#serviceHead').html(cars
      ? '<th><input id="selectPage" type="checkbox" aria-label="选择当前页"></th><th>店铺名称</th><th>品牌名称</th><th>车辆名称</th><th>主图</th><th>租车押金</th><th>月租单价（一个月）</th><th>日租单价（一天）</th><th>时租单价（一小时）</th><th>保险单价（一人一天）</th><th>剩余库存</th><th>总库存</th><th>状态</th><th>创建时间</th><th>操作</th>'
      : '<th>ID</th><th>商户</th><th>维修项目</th><th>价格</th><th>更新时间</th><th>操作</th>');
    if (!state.rows.length) {
      $('#serviceBody').html('<tr><td colspan="' + (cars ? 15 : 6) + '" class="empty-state">暂无记录</td></tr>');
    } else {
      $('#serviceBody').html(state.rows.map(function (row, index) {
        if (cars) return '<tr><td><input class="row-select" type="checkbox" value="' + row.id + '" aria-label="选择记录"></td><td>' + text(row.merchant_name) + '</td><td>' + text(row.brand_name) + '</td><td>' + text(row.platform_car_name) + '</td><td>' + image(row.image) + '</td><td>' + text(row.cash_price) + '</td><td>' + text(row.month_price) + '</td><td>' + text(row.day_price) + '</td><td>' + text(row.hour_price) + '</td><td>' + text(row.insure_price) + '</td><td>' + text(row.stock) + '</td><td>' + text(row.total_stock) + '</td><td>' + statusButton(row) + '</td><td>' + time(row.createtime) + '</td><td>' + actions(index, row.id) + '</td></tr>';
        return '<tr><td>' + row.id + '</td><td>' + text(row.merchant_name) + '</td><td>' + text(row.maintain_name) + '</td><td>' + text(row.price) + '</td><td>' + time(row.updatetime) + '</td><td>' + actions(index, row.id) + '</td></tr>';
      }).join(''));
    }
    $('#bulkDeleteButton').prop('hidden', !cars).prop('disabled', true);
    $('#totalText').text('共 ' + state.total + ' 条记录');
    $('#pageText').text(state.current + ' / ' + state.last);
    $('#prevButton').prop('disabled', state.current <= 1);
    $('#nextButton').prop('disabled', state.current >= state.last);
  }
  function actions(index, id) { return '<button class="btn-link edit" data-index="' + index + '">编辑</button> <button class="btn-link danger remove" data-id="' + id + '">删除</button>'; }
  function input(field, value, row) {
    var actual = value == null && field.defaultValue != null ? field.defaultValue : value;
    var required = field.required ? ' required' : '';
    if (field.type === 'merchant') return '<select class="form-select" name="' + field.key + '"' + required + '>' + optionHtml(state.merchants, actual, '商家') + '</select>';
    if (field.type === 'brand') return '<select class="form-select" id="carBrand" name="brand_id"' + required + '>' + optionHtml(state.brands, actual, '品牌') + '</select>';
    if (field.type === 'car') return '<select class="form-select" id="platformCar" name="platform_car_id"' + required + '>' + carOptions(row && row.brand_id, actual) + '</select>';
    if (field.type === 'option') return '<select class="form-select" name="' + field.key + '" required>' + optionHtml(state.options, actual, '维修项目') + '</select>';
    if (field.type === 'status') return '<select class="form-select" name="status"><option value="1">上架</option><option value="2"' + (Number(actual) === 2 ? ' selected' : '') + '>下架</option></select>';
    if (field.type === 'uploads') return '<div class="upload-field"><input class="form-control" name="' + field.key + '" value="' + escapeHtml(actual || '') + '"' + required + '><button class="btn btn-secondary" type="button" onclick="uploadInto(this.previousElementSibling,true)">上传</button></div>';
    return '<input class="form-control" name="' + field.key + '" type="' + (field.type || 'text') + '" min="0" step="any" value="' + escapeHtml(actual == null ? '' : actual) + '"' + required + '>';
  }
  function openEditor(row) {
    state.editingId = row ? row.id : null;
    $('#editorTitle').text((row ? '编辑' : '新增') + page.title);
    var visible = fields.filter(function (field) { return !(page.service === 'maintains' && row && field.key !== 'price'); });
    $('#serviceForm').html(visible.map(function (field) { return '<div class="form-group"><label>' + field.label + '</label>' + input(field, row && row[field.key], row) + '</div>'; }).join(''));
    $('#editorMask').prop('hidden', false);
  }

  $('#searchButton').on('click', function () { state.current = 1; load(); });
  $('#resetButton').on('click', function () { $('#merchantFilter,#brandFilter,#statusFilter,#keyword,#createdFrom,#createdTo').val(''); state.current = 1; load(); });
  $('#refreshButton').on('click', load);
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } });
  $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } });
  $('#addButton').on('click', function () { openEditor(null); });
  $('.close-editor').on('click', function () { $('#editorMask').prop('hidden', true); });
  $('#serviceHead').on('change', '#selectPage', function () { $('#serviceBody .row-select').prop('checked', this.checked); syncSelection(); });
  $('#serviceBody').on('change', '.row-select', syncSelection);
  $('#bulkDeleteButton').on('click', function () {
    var ids = selectedIds();
    if (!ids.length || !confirm('确定删除选中的 ' + ids.length + ' 条门店车辆吗？')) return;
    api(endpoint, 'DELETE', { ids: ids }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '删除失败'); });
  });
  $('#serviceForm').on('change', '#carBrand', function () { $('#platformCar').html(carOptions($(this).val(), null)); });
  $('#serviceBody').on('click', '.edit', function () { openEditor(state.rows[Number($(this).data('index'))]); })
    .on('click', '.remove', function () { if (!confirm('确定删除该记录吗？')) return; api(endpoint + '/' + $(this).data('id'), 'DELETE').done(function (response) { if (response.code === 1) load(); else alert(response.msg); }); })
    .on('click', '.toggle-status', function () { api(endpoint + '/status', 'PUT', { ids: [Number($(this).data('id'))], status: Number($(this).data('status')) }).done(function (response) { if (response.code === 1) load(); else alert(response.msg || '状态修改失败'); }); });
  $('#saveService').on('click', function () {
    var form = document.getElementById('serviceForm');
    if (!form.reportValidity()) return;
    var data = {};
    $(form).serializeArray().forEach(function (item) { data[item.name] = item.value; });
    api(endpoint + (state.editingId ? '/' + state.editingId : ''), state.editingId ? 'PUT' : 'POST', data).done(function (response) {
      if (response.code === 1) { $('#editorMask').prop('hidden', true); load(); } else alert(response.msg || '保存失败');
    });
  });

  loadOptions(load);
})(jQuery);
