<#import "../layout.ftl" as tpl>
<@tpl.layout active="order" pageTitle="订单管理">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">订单管理</h4>
</div>

<!-- 搜索栏 -->
<div class="card mb-4">
    <div class="card-body">
        <form method="get" class="row">
            <div class="col-md-3 mb-2">
                <input type="text" name="keyword" class="form-control" 
                       placeholder="订单号/货物名称" value="${(keyword)!''}">
            </div>
            <div class="col-md-2 mb-2">
                <select name="status" class="form-control">
                    <option value="">全部状态</option>
                    <option value="0" ${(status?? && status==0)?then('selected','')}>待派单</option>
                    <option value="1" ${(status?? && status==1)?then('selected','')}>待接单</option>
                    <option value="2" ${(status?? && status==2)?then('selected','')}>已接单</option>
                    <option value="3" ${(status?? && status==3)?then('selected','')}>运输中</option>
                    <option value="4" ${(status?? && status==4)?then('selected','')}>已完成</option>
                    <option value="5" ${(status?? && status==5)?then('selected','')}>已取消</option>
                </select>
            </div>
            <div class="col-md-2 mb-2">
                <input type="date" name="startDate" class="form-control" value="${(startDate)!''}" title="开始日期">
            </div>
            <div class="col-md-2 mb-2">
                <input type="date" name="endDate" class="form-control" value="${(endDate)!''}" title="结束日期">
            </div>
            <div class="col-md-2 mb-2">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search mr-1"></i>搜索
                </button>
                <a href="/admin/order/list" class="btn btn-secondary ml-2">重置</a>
            </div>
        </form>
    </div>
</div>

<!-- 订单列表 -->
<div class="card">
    <div class="card-body p-0">
        <table class="table">
            <thead>
                <tr>
                    <th>订单号</th>
                    <th>货物名称</th>
                    <th>发货地址</th>
                    <th>收货地址</th>
                    <th>金额</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <#list page.records as order>
                <tr>
                    <td><strong>${order.orderNo!''}</strong></td>
                    <td>${order.goodsName!''}</td>
                    <td><#assign _sa = order.startAddress!''><#if _sa?length gt 15>${_sa?substring(0, 15)}...<#else>${_sa}</#if></td>
                    <td><#assign _ea = order.endAddress!''><#if _ea?length gt 15>${_ea?substring(0, 15)}...<#else>${_ea}</#if></td>
                    <td class="text-success">¥<#if order.actualFreight??>${order.actualFreight}<#else>${order.freight!'0'}</#if></td>
                    <td>
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
                    </td>
                    <td>${order.createTime!'-'}</td>
                    <td>
                        <a href="/admin/order/detail?id=${order.id}" class="btn btn-info btn-sm">
                            <i class="fas fa-eye"></i>
                        </a>
                        <#if order.status == 0 || order.status == 1>
                        <a href="/admin/order/dispatch?id=${order.id}" class="btn btn-primary btn-sm ml-1">
                            <i class="fas fa-truck"></i>
                        </a>
                        <button type="button" class="btn btn-danger btn-sm ml-1" onclick="cancelOrder(${order.id})" title="取消订单">
                            <i class="fas fa-times"></i>
                        </button>
                        </#if>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
    <div class="card-footer d-flex justify-content-between align-items-center">
        <span class="text-muted">共 ${page.total} 条记录</span>
        <nav>
            <ul class="pagination">
                <#if page.current &gt; 1>
                <li class="page-item"><a class="page-link" href="?page=${page.current-1}&status=${(status!'')}&keyword=${(keyword!'')}&startDate=${(startDate!'')}&endDate=${(endDate!'')}">上一页</a></li>
                </#if>
                <li class="page-item active"><a class="page-link">${page.current}/${page.pages}</a></li>
                <#if page.current &lt; page.pages>
                <li class="page-item"><a class="page-link" href="?page=${page.current+1}&status=${(status!'')}&keyword=${(keyword!'')}&startDate=${(startDate!'')}&endDate=${(endDate!'')}">下一页</a></li>
                </#if>
            </ul>
        </nav>
    </div>
</div>

<script>
    function cancelOrder(id) {
        if (!confirm('确认取消该订单吗？取消后不能恢复。')) {
            return;
        }
        api('/api/admin/order/cancel/' + id, 'POST').then(function (result) {
            if (result.code === 200) {
                location.reload();
                return;
            }
            alert(result.msg || '订单取消失败');
        });
    }
</script>

</@tpl.layout>
