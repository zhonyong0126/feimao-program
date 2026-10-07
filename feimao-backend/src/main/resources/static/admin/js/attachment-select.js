(function ($) {
  'use strict';
  var endpoint = '/api/admin/business/system/attachments';
  var params = new URLSearchParams(window.location.search);
  var multiple = params.get('multiple') === 'true';
  var state = {rows: [], page: 1, last: 1, total: 0, selected: {}};
  var categories = [{category: 'unclassed', label: '未归类'}];

  function escapeHtml(value) { return $('<div>').text(value == null || value === '' ? '-' : String(value)).html(); }
  function escapeAttribute(value) { return String(value == null ? '' : value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
  function categoryLabel(value) {
    var key = value == null || value === '' ? 'unclassed' : String(value);
    for (var i = 0; i < categories.length; i++) if (categories[i].category === key) return categories[i].label;
    return key;
  }
  function normalizeCategories(data) {
    if (typeof data === 'string') {
      try { data = JSON.parse(data); } catch (ignore) { data = data ? [data] : []; }
    }
    if (data && !Array.isArray(data)) data = data.categories || data.categoryList || data.rows || data.data || data;
    var result = [], seen = {};
    function add(category, label) {
      var key = category == null || category === '' ? 'unclassed' : String(category);
      if (seen[key]) return;
      seen[key] = true;
      result.push({category: key, label: String(label == null || label === '' ? key : label)});
    }
    if (Array.isArray(data)) data.forEach(function (item) {
      if (typeof item === 'string') return add(item, item);
      if (!item || typeof item !== 'object') return;
      var key = item.category != null ? item.category : (item.key != null ? item.key : item.value);
      add(key, item.label != null ? item.label : (item.title != null ? item.title : (item.name != null ? item.name : key)));
    });
    else if (data && typeof data === 'object') Object.keys(data).forEach(function (key) {
      var value = data[key];
      if (value && typeof value === 'object') add(value.category != null ? value.category : key, value.label != null ? value.label : (value.title != null ? value.title : (value.name != null ? value.name : key)));
      else add(key, value);
    });
    add('unclassed', '未归类');
    return result;
  }
  function mediaUrl(value) {
    var url = String(value || '').trim();
    return url.indexOf('/uploads/') === 0 || /^https?:\/\//i.test(url) ? url : '';
  }
  function preview(row) {
    var url = mediaUrl(row.fullurl || row.url);
    if (!url) return '-';
    if (/^image\//i.test(String(row.mimetype || ''))) return '<a href="' + escapeAttribute(url) + '" target="_blank" rel="noopener"><img class="resource-thumb" src="' + escapeAttribute(url) + '" alt="附件预览"></a>';
    return '<a href="' + escapeAttribute(url) + '" target="_blank" rel="noopener"><i class="fas fa-file"></i> 查看文件</a>';
  }
  function formatTime(value) {
    if (!value) return '-';
    var date = /^\d{10}$/.test(String(value)) ? new Date(Number(value) * 1000) : new Date(value);
    if (isNaN(date.getTime())) return escapeHtml(value);
    var pad = function (number) { return String(number).padStart(2, '0'); };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  }
  function rowUrl(row) { return String(row.url || row.fullurl || ''); }
  function setSelected(row, selected) {
    if (selected) state.selected[row.id] = rowUrl(row);
    else delete state.selected[row.id];
  }
  function render() {
    var selectHead = multiple ? '<th><input type="checkbox" id="selectAllAttachments" aria-label="全选当前页"></th>' : '';
    $('#attachmentSelectorHead').html(selectHead + '<th>记录编号</th><th>类别</th><th>预览</th><th>文件名</th><th>宽度</th><th>高度</th><th>文件类型</th><th>创建日期</th><th>操作</th>');
    var columns = multiple ? 10 : 9;
    $('#attachmentSelectorBody').html(state.rows.length ? state.rows.map(function (row) {
      var selected = !!state.selected[row.id];
      var checkbox = multiple ? '<td><input class="attachment-selector-check" type="checkbox" value="' + escapeAttribute(row.id) + '"' + (selected ? ' checked' : '') + ' aria-label="选择附件"></td>' : '';
      var action = multiple ? '<button class="btn-link toggle-attachment" data-id="' + escapeAttribute(row.id) + '">' + (selected ? '取消' : '选择') + '</button>' : '<button class="btn-link choose-attachment" data-id="' + escapeAttribute(row.id) + '">选择</button>';
      return '<tr>' + checkbox + '<td>' + escapeHtml(row.id) + '</td><td>' + escapeHtml(categoryLabel(row.category)) + '</td><td>' + preview(row) + '</td><td>' + escapeHtml(row.filename) + '</td><td>' + escapeHtml(row.imagewidth) + '</td><td>' + escapeHtml(row.imageheight) + '</td><td>' + escapeHtml(row.mimetype) + '</td><td>' + formatTime(row.createtime) + '</td><td>' + action + '</td></tr>';
    }).join('') : '<tr><td colspan="' + columns + '" class="empty-state">暂无附件</td></tr>');
    $('#attachmentSelectorTotal').text('共 ' + state.total + ' 条记录');
    $('#attachmentSelectorPage').text(state.page + ' / ' + state.last);
    $('#attachmentSelectorPrev').prop('disabled', state.page <= 1);
    $('#attachmentSelectorNext').prop('disabled', state.page >= state.last);
    $('#confirmAttachmentSelection').prop('disabled', Object.keys(state.selected).length === 0);
    if (multiple) $('#selectAllAttachments').prop('checked', state.rows.length > 0 && state.rows.every(function (row) { return !!state.selected[row.id]; }));
  }
  function load() {
    var query = ['page=' + state.page, 'limit=10'];
    var keyword = $('#attachmentSelectorKeyword').val();
    var category = $('#attachmentSelectorCategory').val();
    var mimetype = $('#attachmentSelectorMimetype').val();
    if (keyword) query.push('keyword=' + encodeURIComponent(keyword));
    if (category) query.push('category=' + encodeURIComponent(category));
    if (mimetype) query.push('mimetype=' + encodeURIComponent(mimetype));
    api(endpoint + '/select?' + query.join('&'), 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg || '附件加载失败');
      var data = response.data || {};
      state.rows = Array.isArray(data) ? data : (data.rows || data.data || []);
      state.total = Number(Array.isArray(data) ? data.length : (data.total == null ? state.rows.length : data.total));
      state.page = Number(data.page || data.current_page || 1);
      state.last = Number(data.last_page || Math.max(1, Math.ceil(state.total / 10)));
      render();
    }).fail(function () { alert('网络错误，附件加载失败'); });
  }
  function returnUrls(urls) {
    var value = urls.join(',');
    var message = {type: 'feimao-attachment-selected', url: value, urls: urls, multiple: multiple};
    $('#attachmentSelectorUrls').val(value);
    $('#attachmentSelectorResult').prop('hidden', false);
    var delivered = false;
    if (window.opener && !window.opener.closed) {
      window.opener.postMessage(message, window.location.origin);
      delivered = true;
    } else if (window.parent && window.parent !== window) {
      window.parent.postMessage(message, window.location.origin);
      delivered = true;
    }
    if (delivered) window.setTimeout(function () { window.close(); }, 100);
  }
  function uploadSelectedFiles(files) {
    var token = localStorage.getItem('token') || '';
    var category = $('#attachmentSelectorCategory').val() || '';
    if (category === 'unclassed') category = '';
    var succeeded = 0, failed = 0;
    $('#uploadSelectorAttachments').prop('disabled', true);
    function next(index) {
      if (index >= files.length) {
        $('#selectorUploadStatus').text('上传完成：成功 ' + succeeded + ' 个，失败 ' + failed + ' 个');
        $('#uploadSelectorAttachments').prop('disabled', false);
        $('#selectorAttachmentFiles').val('');
        state.page = 1;
        load();
        return;
      }
      $('#selectorUploadStatus').text('正在上传 ' + (index + 1) + ' / ' + files.length + '：' + files[index].name);
      var form = new FormData();
      form.append('file', files[index]);
      form.append('category', category);
      form.append('token', token);
      $.ajax({url: '/api/common/upload', type: 'POST', data: form, processData: false, contentType: false, headers: {token: token}})
        .done(function (response) { if (response && response.code === 1) succeeded++; else failed++; })
        .fail(function () { failed++; })
        .always(function () { next(index + 1); });
    }
    next(0);
  }

  $('#attachmentSelectorMode').text(multiple ? '可选择多个附件，选完后点击“确认选择”。' : '请选择一个附件。');
  $('#confirmAttachmentSelection').prop('hidden', !multiple);
  $('#attachmentSelectorKeyword').val(params.get('keyword') || '');
  $('#attachmentSelectorMimetype').val(params.get('mimetype') || '');
  $('#searchAttachments').on('click', function () { state.page = 1; load(); });
  $('#uploadSelectorAttachments').on('click', function () { $('#selectorAttachmentFiles').trigger('click'); });
  $('#selectorAttachmentFiles').on('change', function () { var files = Array.prototype.slice.call(this.files || []); if (files.length) uploadSelectedFiles(files); });
  $('#attachmentSelectorKeyword,#attachmentSelectorMimetype').on('keydown', function (event) { if (event.key === 'Enter') { event.preventDefault(); state.page = 1; load(); } });
  $('#attachmentSelectorPrev').on('click', function () { if (state.page > 1) { state.page--; load(); } });
  $('#attachmentSelectorNext').on('click', function () { if (state.page < state.last) { state.page++; load(); } });
  $('#attachmentSelectorBody').on('change', '.attachment-selector-check', function () { var id = Number(this.value), row = state.rows.filter(function (item) { return Number(item.id) === id; })[0]; if (row) setSelected(row, this.checked); render(); });
  $('#attachmentSelectorHead').on('change', '#selectAllAttachments', function () { var selected = this.checked; state.rows.forEach(function (row) { setSelected(row, selected); }); render(); });
  $('#attachmentSelectorBody').on('click', '.toggle-attachment', function () { var id = Number($(this).data('id')), row = state.rows.filter(function (item) { return Number(item.id) === id; })[0]; if (row) { setSelected(row, !state.selected[row.id]); render(); } });
  $('#attachmentSelectorBody').on('click', '.choose-attachment', function () { var id = Number($(this).data('id')), row = state.rows.filter(function (item) { return Number(item.id) === id; })[0]; if (row) returnUrls([rowUrl(row)]); });
  $('#confirmAttachmentSelection').on('click', function () { var urls = Object.keys(state.selected).map(function (id) { return state.selected[id]; }).filter(Boolean); if (!urls.length) return alert('请至少选择一个附件'); returnUrls(urls); });
  $('#copyAttachmentUrls').on('click', function () { var text = $('#attachmentSelectorUrls').val(); if (!text) return; if (navigator.clipboard) navigator.clipboard.writeText(text); else { $('#attachmentSelectorUrls').trigger('select'); document.execCommand('copy'); } });

  api(endpoint + '/categories', 'GET').done(function (response) {
    if (response.code === 1) categories = normalizeCategories(response.data);
  }).always(function () {
    var selectedCategory = params.get('category') || '';
    var options = '<option value="">全部类别</option>';
    categories.forEach(function (item) { options += '<option value="' + escapeAttribute(item.category) + '">' + escapeHtml(item.label) + '</option>'; });
    $('#attachmentSelectorCategory').html(options).val(selectedCategory);
    load();
  });
})(jQuery);
