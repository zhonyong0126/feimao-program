<#import "../layout.ftl" as tpl>
<@tpl.layout active="driver-withdrawals" pageTitle="司机提现审核">

<div class="d-flex justify-content-between align-items-center mb-4">
    <div><h4 class="mb-1">司机提现审核</h4><div class="text-muted">通过后发起微信转账，拒绝后金额自动退回司机余额。</div></div>
</div>

<div class="card mb-4"><div class="card-body row">
    <div class="col-md-3"><select id="withdrawStatus" class="form-control"><option value="">全部状态</option><option value="0">待审核</option><option value="1">已转账</option><option value="2">已拒绝</option></select></div>
    <div class="col-md-2"><button id="searchWithdraw" class="btn btn-primary">查询</button></div>
</div></div>

<div class="card"><div class="card-body p-0 table-responsive"><table class="table">
    <thead><tr><th>ID</th><th>司机</th><th>手机号</th><th>车牌</th><th>金额</th><th>状态</th><th>申请时间</th><th>操作</th></tr></thead>
    <tbody id="withdrawBody"><tr><td colspan="8" class="text-center text-muted">正在加载...</td></tr></tbody>
</table></div><div class="card-footer d-flex justify-content-between"><span id="withdrawTotal">共 0 条</span><div><button id="withdrawPrev" class="btn btn-secondary btn-sm">上一页</button> <span id="withdrawPage">1 / 1</span> <button id="withdrawNext" class="btn btn-secondary btn-sm">下一页</button></div></div></div>

<script>
(function () {
    var state = {page: 1, last: 1};
    function esc(value) { return $('<div>').text(value == null || value === '' ? '-' : String(value)).html(); }
    function load() {
        var status = $('#withdrawStatus').val();
        api('/api/admin/driver-withdrawals?page=' + state.page + '&limit=10' + (status === '' ? '' : '&status=' + status), 'GET').then(function (result) {
            if (result.code !== 200) { alert(result.msg || '加载失败'); return; }
            var data = result.data || {}, rows = data.records || [];
            state.last = Math.max(1, Math.ceil(Number(data.total || 0) / Number(data.limit || 10)));
            $('#withdrawTotal').text('共 ' + Number(data.total || 0) + ' 条');
            $('#withdrawPage').text(state.page + ' / ' + state.last);
            $('#withdrawPrev').prop('disabled', state.page <= 1);
            $('#withdrawNext').prop('disabled', state.page >= state.last);
            $('#withdrawBody').html(rows.length ? rows.map(function (row) {
                var statusText = Number(row.status) === 0 ? '<span class="badge badge-warning">待审核</span>' : (Number(row.status) === 1 ? '<span class="badge badge-success">已转账</span>' : '<span class="badge badge-danger">已拒绝</span>');
                var actions = Number(row.status) === 0 ? '<button class="btn btn-success btn-sm approve" data-id="' + row.id + '">通过</button> <button class="btn btn-danger btn-sm reject" data-id="' + row.id + '">拒绝</button>' : esc(row.transfer_batch_id || row.reject_reason);
                return '<tr><td>' + row.id + '</td><td>' + esc(row.driver_name) + '</td><td>' + esc(row.mobile) + '</td><td>' + esc(row.plate_number) + '</td><td class="text-success">¥' + esc(row.amount) + '</td><td>' + statusText + '</td><td>' + esc(row.create_time) + '</td><td>' + actions + '</td></tr>';
            }).join('') : '<tr><td colspan="8" class="text-center text-muted">暂无提现记录</td></tr>');
        });
    }
    $('#searchWithdraw').on('click', function () { state.page = 1; load(); });
    $('#withdrawPrev').on('click', function () { if (state.page > 1) { state.page--; load(); } });
    $('#withdrawNext').on('click', function () { if (state.page < state.last) { state.page++; load(); } });
    $('#withdrawBody').on('click', '.approve', function () {
        var id = Number($(this).data('id')), note = prompt('请输入审核备注（可留空）', '');
        if (note === null || !confirm('通过后将发起真实微信转账，确认继续吗？')) return;
        api('/api/admin/driver-withdrawals/approve', 'POST', {id: id, note: note}).then(function (result) { if (result.code === 200) { alert('转账已提交，批次号：' + result.data.batchId); load(); } else alert(result.msg || '审核失败'); });
    }).on('click', '.reject', function () {
        var id = Number($(this).data('id')), reason = prompt('请输入拒绝原因');
        if (reason === null || !reason.trim()) return;
        api('/api/admin/driver-withdrawals/reject', 'POST', {id: id, reason: reason}).then(function (result) { if (result.code === 200) load(); else alert(result.msg || '拒绝失败'); });
    });
    load();
})();
</script>

</@tpl.layout>
