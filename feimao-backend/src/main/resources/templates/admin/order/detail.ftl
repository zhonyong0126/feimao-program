<#import "../layout.ftl" as tpl>
<@tpl.layout active="order" pageTitle="订单详情">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">订单详情</h4>
    <a href="/admin/order/list" class="btn btn-secondary">
        <i class="fas fa-arrow-left mr-1"></i>返回列表
    </a>
    <#if order.status == 0 || order.status == 1>
    <button type="button" class="btn btn-danger ml-2" onclick="cancelOrder(${order.id})">
        <i class="fas fa-times mr-1"></i>取消订单
    </button>
    </#if>
</div>

<div class="row">
    <div class="col-lg-8">
        <div class="card mb-4">
            <div class="card-header">
                <i class="fas fa-info-circle mr-2"></i>基本信息
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">订单号</div>
                        <div class="detail-value">${order.orderNo!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">状态</div>
                        <div class="detail-value">
                            <#if order.status == 0>
                            <span class="badge badge-warning">待派单</span>
                            <#elseif order.status == 1>
                            <span class="badge badge-info">待接单</span>
                            <#elseif order.status == 2>
                            <span class="badge badge-primary">已接单</span>
                            <#elseif order.status == 3>
                            <span class="badge badge-info">运输中</span>
                            <#elseif order.status == 4>
                            <span class="badge badge-success">已完成</span>
                            <#else>
                            <span class="badge badge-secondary">已取消</span>
                            </#if>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">货物名称</div>
                        <div class="detail-value">${order.goodsName!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">货物重量</div>
                        <div class="detail-value">${order.goodsWeight!'0'} 吨</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">运费金额</div>
                        <div class="detail-value text-success">¥<#if order.actualFreight??>${order.actualFreight}<#else>${order.freight!'0'}</#if></div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">支付状态</div>
                        <div class="detail-value">
                            <#if order.payStatus == 1>
                            <span class="badge badge-success">已支付</span>
                            <#else>
                            <span class="badge badge-secondary">未支付</span>
                            </#if>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">创建时间</div>
                        <div class="detail-value">${order.createTime!'-'}</div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <i class="fas fa-map-marker-alt mr-2"></i>地址信息
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">发货地址</div>
                        <div class="detail-value">${order.startAddress!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">收货地址</div>
                        <div class="detail-value">${order.endAddress!''}</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">货物体积</div>
                        <div class="detail-value">${order.goodsVolume!'0'} 方</div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <div class="detail-label">运输距离</div>
                        <div class="detail-value">${order.distance!'0'} 公里</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mt-4">
            <div class="card-header">
                <i class="fas fa-clock mr-2"></i>履约时间线
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3"><div class="detail-label">接单时间</div><div class="detail-value">${order.acceptTime!'-'}</div></div>
                    <div class="col-md-6 mb-3"><div class="detail-label">开始运输</div><div class="detail-value">${order.startTime!'-'}</div></div>
                    <div class="col-md-6 mb-3"><div class="detail-label">确认到达</div><div class="detail-value">${order.arriveTime!'-'}</div></div>
                    <div class="col-md-6 mb-3"><div class="detail-label">完成时间</div><div class="detail-value">${order.completeTime!'-'}</div></div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header">
                <i class="fas fa-user mr-2"></i>客户信息
            </div>
            <div class="card-body">
                <#if customer??>
                <div class="detail-label">客户名称</div>
                <div class="detail-value">${customer.name!'-'}</div>
                <div class="detail-label mt-3">联系人</div>
                <div class="detail-value">${customer.contact!'-'}</div>
                <div class="detail-label mt-3">联系电话</div>
                <div class="detail-value">${customer.mobile!'-'}</div>
                <#else>
                <div class="text-muted">客户记录不存在（ID：${order.customerId!'-'}）</div>
                </#if>
            </div>
        </div>
        
        <#if order.driverId??>
        <div class="card mt-3">
            <div class="card-header">
                <i class="fas fa-truck mr-2"></i>司机信息
            </div>
            <div class="card-body">
                <#if driver??>
                <div class="detail-label">司机姓名</div>
                <div class="detail-value"><a href="/admin/driver/detail?id=${driver.id}">${driver.name!'-'}</a></div>
                <div class="detail-label mt-3">联系电话</div>
                <div class="detail-value">${driver.mobile!'-'}</div>
                <div class="detail-label mt-3">车牌号</div>
                <div class="detail-value">${driver.plateNumber!'-'}</div>
                <#else>
                <div class="text-muted">司机记录不存在（ID：${order.driverId}）</div>
                </#if>
            </div>
        </div>
        </#if>

        <#if order.staffId??>
        <div class="card mt-3">
            <div class="card-header">
                <i class="fas fa-headset mr-2"></i>派单员工
            </div>
            <div class="card-body">
                <#if dispatchStaff??>
                <div class="detail-label">员工</div>
                <div class="detail-value">${dispatchStaff.name!'-'}（${dispatchStaff.staffNo!'-'}）</div>
                <#else>
                <div class="text-muted">员工记录不存在（ID：${order.staffId}）</div>
                </#if>
            </div>
        </div>
        </#if>
        
        <#if order.remark??>
        <div class="card mt-3">
            <div class="card-header">
                <i class="fas fa-comment mr-2"></i>备注
            </div>
            <div class="card-body">
                ${order.remark!''}
            </div>
        </div>
        </#if>
    </div>
</div>

<script>
    function cancelOrder(id) {
        if (!confirm('确认取消该订单吗？取消后不能恢复。')) {
            return;
        }
        api('/api/admin/order/cancel/' + id, 'POST').then(function (result) {
            if (result.code === 200) {
                location.href = '/admin/order/list';
                return;
            }
            alert(result.msg || '订单取消失败');
        });
    }
</script>

</@tpl.layout>
