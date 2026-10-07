(function ($) {
  'use strict';
  var state = {page: 1, last: 1};
  function escapeHtml(value) { return $('<div>').text(value == null || value === '' ? '-' : String(value)).html(); }
  function showAvatar(value) {
    var avatar = value == null ? '' : String(value).trim();
    $('#profileAvatar').attr('src', avatar).toggle(avatar !== '');
  }
  function urlCell(value) {
    if (value == null || value === '') return '-';
    var url = String(value);
    return '<a href="' + escapeHtml(url) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(url) + '</a>';
  }
  function load() {
    api('/api/admin/business/system/profile?page=' + state.page + '&limit=10', 'GET').done(function (response) {
      if (response.code !== 1) return alert(response.msg);
      var data = response.data || {}, profile = data.profile || {}, logs = data.logs || {};
      $('#username').val(profile.username || '');
      ['nickname', 'avatar', 'email', 'mobile'].forEach(function (name) { $('[name="' + name + '"]').val(profile[name] || ''); });
      showAvatar(profile.avatar);
      var rows = logs.data || logs.rows || [];
      $('#profileLogs').html(rows.length ? rows.map(function (row) { return '<tr><td>' + escapeHtml(row.id) + '</td><td>' + escapeHtml(row.title) + '</td><td>' + urlCell(row.url) + '</td><td>' + escapeHtml(row.ip) + '</td><td>' + escapeHtml(adminDetailValue('createtime', row.createtime)) + '</td></tr>'; }).join('') : '<tr><td colspan="5" class="empty-state">暂无记录</td></tr>');
      var total = Number(logs.total || 0); state.page = Number(logs.current_page || logs.page || 1); state.last = Number(logs.last_page || Math.max(1, Math.ceil(total / 10)));
      $('#profileTotal').text('共 ' + total + ' 条记录'); $('#profilePage').text(state.page + ' / ' + state.last); $('#profilePrev').prop('disabled', state.page <= 1); $('#profileNext').prop('disabled', state.page >= state.last);
    });
  }
  $('#profileRefresh').on('click', load);
  $('#profilePrev').on('click', function () { if (state.page > 1) { state.page--; load(); } }); $('#profileNext').on('click', function () { if (state.page < state.last) { state.page++; load(); } });
  $('[name=avatar]').on('change input', function () { showAvatar(this.value); });
  $('#profileForm').on('submit', function (event) { event.preventDefault(); var body = {}, passwordChanged = $('[name=password]').val() !== ''; $(this).serializeArray().forEach(function (item) { if (item.value !== '') body[item.name] = item.value; }); api('/api/admin/business/system/profile', 'PUT', body).done(function (response) { if (response.code !== 1) return alert(response.msg); if (passwordChanged) { alert('密码修改成功，请重新登录'); localStorage.removeItem('token'); localStorage.removeItem('staff'); localStorage.removeItem('userInfo'); document.cookie = 'admin_token=; Max-Age=0; Path=/; SameSite=Lax'; window.location.href = '/admin/login'; return; } alert('保存成功'); load(); }); });
  load();
})(jQuery);
