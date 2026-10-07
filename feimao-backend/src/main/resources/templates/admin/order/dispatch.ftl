<#import "../layout.ftl" as tpl>
<@tpl.layout active="order" pageTitle="派单管理">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">派单管理</h4>
    <a href="/admin/order/list" class="btn btn-secondary">
        <i class="fas fa-arrow-left mr-1"></i>返回列表
    </a>
</div>

<div class="row">
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header">
                <i class="fas fa-info-circle mr-2"></i>订单信息
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <div class="detail-label">订单号</div>
                    <div class="detail-value"><strong>${order.orderNo!''}</strong></div>
                </div>
                <div class="mb-3">
                    <div class="detail-label">货物名称</div>
                    <div class="detail-value">${order.goodsName!''}</div>
                </div>
                <div class="mb-3">
                    <div class="detail-label">发货地址</div>
                    <div class="detail-value">${order.startAddress!''}</div>
                </div>
                <div class="mb-3">
                    <div class="detail-label">收货地址</div>
                    <div class="detail-value">${order.endAddress!''}</div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header">
                <i class="fas fa-truck mr-2"></i>选择司机
            </div>
            <div class="card-body p-0">
                <#if drivers?? && drivers?size &gt; 0>
                <table class="table">
                    <thead>
                        <tr>
                            <th>姓名</th>
                            <th>手机号</th>
                            <th>车牌号</th>
                            <th>车型</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list drivers as driver>
                        <tr>
                            <td>${driver.name!''}</td>
                            <td>${driver.mobile!''}</td>
                            <td>${driver.plateNumber!''}</td>
                            <td>${driver.vehicleType!'-'}</td>
                            <td>
                                <button class="btn btn-success btn-sm" onclick="dispatch(${order.id}, ${driver.id})">
                                    <i class="fas fa-check mr-1"></i>派单
                                </button>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                <#else>
                <div class="empty-state">
                    <i class="fas fa-truck"></i>
                    <p>暂无空闲司机</p>
                </div>
                </#if>
            </div>
        </div>
    </div>
</div>

<script>
function dispatch(orderId, driverId) {
    if (!confirm('确定派单给该司机？')) return;
    
    api('/api/admin/order/dispatch', 'POST', { orderId: orderId, driverId: driverId })
        .then(function(res) {
            if (res.code === 200) {
                alert('派单成功');
                location.href = '/admin/order/list';
            } else {
                alert(res.msg || '派单失败');
            }
        });
}
</script>

</@tpl.layout>
