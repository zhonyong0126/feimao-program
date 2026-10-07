(function ($) {
  'use strict';
  var page = window.businessApplicationPage;
  if (!page) return;
  var endpoint = '/api/admin/business/applications/' + page.role;
  var state = { current: 1, limit: 10, total: 0, last: 1, rows: [] };
  var labels = { name: '名称', nickname: '联系人', mobile: '手机号', address: '地址', business: '营业时间', intro: '简介', image: '环境图片', avatar: '头像', examine_note: '审核备注' };

  function text(value) { return $('<div>').text(value == null || value === '' ? '-' : String(value)).html(); }
  function status(value) {
    var states = { 1: ['待审核', 'badge-warning'], 2: ['已通过', 'badge-success'], 3: ['已拒绝', 'badge-danger'] };
    var item = states[Number(value)] || ['未知', 'badge-secondary'];
    return '<span class="badge ' + item[1] + '">' + item[0] + '</span>';
  }
  function load() {
    var selected = $('#statusFilter').val();
    var url = endpoint + '?page=' + state.current + '&limit=' + state.limit + (selected ? '&status=' + selected : '');
    api(url, 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg || '加载失败');
      var data = response.data || {};
      state.rows = data.data || []; state.total = Number(data.total || 0); state.current = Number(data.current_page || 1); state.last = Number(data.last_page || 1);
      render();
    }).fail(function () { alert('加载失败，请重新登录后重试'); });
  }
  function render() {
    if (!state.rows.length) $('#applicationBody').html('<tr><td colspan="8" class="empty-state">暂无申请记录</td></tr>');
    else $('#applicationBody').html(state.rows.map(function (row, index) {
      var address = page.role === 'merchant' ? row.business : row.address;
      var actions = '<button class="btn-link detail" data-index="' + index + '">详情</button>';
      if (Number(row.examine_status) === 1) actions += ' <button class="btn-link approve" data-id="' + row.id + '">通过</button> <button class="btn-link danger reject" data-id="' + row.id + '">拒绝</button>';
      return '<tr><td>' + text(row.id) + '</td><td>' + text(row.name) + '</td><td>' + text(row.nickname) + '</td><td>' + text(row.mobile) + '</td><td>' + text(address) + '</td><td>' + status(row.examine_status) + '</td><td>' + text(row.createtime) + '</td><td>' + actions + '</td></tr>';
    }).join(''));
    $('#totalText').text('共 ' + state.total + ' 条记录'); $('#pageText').text(state.current + ' / ' + state.last); $('#prevButton').prop('disabled', state.current <= 1); $('#nextButton').prop('disabled', state.current >= state.last);
  }
  function openExamine(id, type) {
    $('#examineId').val(id); $('#examineType').val(type); $('#approveFields').toggle(type === 2); $('#rejectFields').toggle(type === 3);
    $('#examineTitle').text(type === 2 ? '通过审核' : '拒绝审核'); $('#loginUsername,#loginPassword,#examineNote').val(''); $('#examineMask').prop('hidden', false);
  }
  $('#searchButton').on('click', function () { state.current = 1; load(); });
  $('#resetButton').on('click', function () { $('#statusFilter').val(''); state.current = 1; load(); });
  $('#prevButton').on('click', function () { if (state.current > 1) { state.current--; load(); } });
  $('#nextButton').on('click', function () { if (state.current < state.last) { state.current++; load(); } });
  $('#applicationBody').on('click', '.detail', function () {
    var row = state.rows[Number($(this).data('index'))];
    $('#detailBody').html(Object.keys(labels).filter(function (key) { return row[key] != null && row[key] !== ''; }).map(function (key) { return '<tr><th style="width:120px">' + labels[key] + '</th><td>' + text(row[key]) + '</td></tr>'; }).join(''));
    $('#detailMask').prop('hidden', false);
  }).on('click', '.approve', function () { openExamine($(this).data('id'), 2); }).on('click', '.reject', function () { openExamine($(this).data('id'), 3); });
  $('.close-mask').on('click', function () { $('#detailMask').prop('hidden', true); });
  $('.close-examine').on('click', function () { $('#examineMask').prop('hidden', true); });
  $('#submitExamine').on('click', function () {
    var type = Number($('#examineType').val());
    var payload = { id: Number($('#examineId').val()), type: type, username: $('#loginUsername').val().trim(), password: $('#loginPassword').val(), note: $('#examineNote').val().trim() };
    if (type === 2 && (!payload.username || !payload.password)) return alert('通过审核时必须设置登录账号和密码');
    if (type === 2) $('.approval-setting').each(function () { payload[$(this).data('key')] = Number($(this).val()); });
    if (!confirm(type === 2 ? '确认通过该申请吗？' : '确认拒绝该申请吗？')) return;
    api(endpoint + '/examine', 'POST', payload).done(function (response) { if (response.code === 1) { $('#examineMask').prop('hidden', true); load(); } else alert(response.msg || '审核失败'); });
  });
  load();
})(jQuery);
