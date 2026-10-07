<#import "../layout.ftl" as tpl>
<@tpl.layout active="driver" pageTitle="司机详情">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">司机详情</h4>
    <a href="/admin/driver/list" class="btn btn-secondary">
        <i class="fas fa-arrow-left mr-1"></i>返回列表
    </a>
</div>

<div class="row">
    <div class="col-lg-6">
        <div class="card mb-4">
            <div class="card-header">
                <i class="fas fa-user mr-2"></i>基本信息
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">姓名</div>
                        <div class="detail-value">${d.name!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">手机号</div>
                        <div class="detail-value">${d.mobile!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">身份证号</div>
                        <div class="detail-value">${d.idCard!'-'}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">注册时间</div>
                        <div class="detail-value">${d.createTime!'-'}</div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <i class="fas fa-truck mr-2"></i>车辆信息
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">车牌号</div>
                        <div class="detail-value">${d.plateNumber!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">车型</div>
                        <div class="detail-value">${d.vehicleType!'-'}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">驾驶证号</div>
                        <div class="detail-value">${d.driverLicense!'-'}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">行驶证号</div>
                        <div class="detail-value">${d.vehicleLicense!'-'}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-lg-6">
        <div class="card mb-4">
            <div class="card-header">
                <i class="fas fa-check-circle mr-2"></i>状态信息
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">认证状态</div>
                        <div class="detail-value">
                            <#if d.authStatus == 0>
                            <span class="badge badge-secondary">未认证</span>
                            <#elseif d.authStatus == 1>
                            <span class="badge badge-warning">审核中</span>
                            <#elseif d.authStatus == 2>
                            <span class="badge badge-success">已认证</span>
                            <#else>
                            <span class="badge badge-danger">认证失败</span>
                            </#if>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">在线状态</div>
                        <div class="detail-value">
                            <#if d.onlineStatus == 0>
                            <span class="badge badge-secondary">离线</span>
                            <#elseif d.onlineStatus == 1>
                            <span class="badge badge-success">空闲</span>
                            <#else>
                            <span class="badge badge-info">运输中</span>
                            </#if>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">账号状态</div>
                        <div class="detail-value">
                            <#if d.status == 1>
                            <span class="badge badge-success">启用</span>
                            <#else>
                            <span class="badge badge-secondary">禁用</span>
                            </#if>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">账户余额</div>
                        <div class="detail-value text-success">¥${d.balance!'0'}</div>
                    </div>
                </div>
                <hr>
                <div class="d-flex flex-wrap">
                    <#if d.authStatus == 1>
                    <button type="button" class="btn btn-success mr-2 mb-2" onclick="updateAuth(2)">
                        <i class="fas fa-check mr-1"></i>审核通过
                    </button>
                    <button type="button" class="btn btn-danger mr-2 mb-2" onclick="updateAuth(3)">
                        <i class="fas fa-times mr-1"></i>审核拒绝
                    </button>
                    </#if>
                    <#if d.status == 1>
                    <button type="button" class="btn btn-outline-danger mb-2" onclick="updateStatus(0)">禁用账号</button>
                    <#else>
                    <button type="button" class="btn btn-outline-success mb-2" onclick="updateStatus(1)">启用账号</button>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function updateAuth(authStatus) {
        var message = authStatus === 2 ? '确认通过该司机的认证审核吗？' : '确认拒绝该司机的认证审核吗？';
        if (!confirm(message)) {
            return;
        }
        api('/api/admin/driver/auth', 'POST', {id: ${d.id}, authStatus: authStatus}).then(function (result) {
            if (result.code === 200) {
                location.reload();
                return;
            }
            alert(result.msg || '审核失败');
        });
    }

    function updateStatus(status) {
        var message = status === 1 ? '确认启用该司机账号吗？' : '确认禁用该司机账号吗？';
        if (!confirm(message)) {
            return;
        }
        api('/api/admin/driver/status', 'POST', {id: ${d.id}, status: status}).then(function (result) {
            if (result.code === 200) {
                location.reload();
                return;
            }
            alert(result.msg || '状态更新失败');
        });
    }
</script>

</@tpl.layout>
