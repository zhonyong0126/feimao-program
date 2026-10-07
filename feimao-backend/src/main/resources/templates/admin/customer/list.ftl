<#import "../layout.ftl" as tpl>
<@tpl.layout active="customer" pageTitle="客户管理">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">客户管理</h4>
</div>

<!-- 搜索栏 -->
<div class="card mb-4">
    <div class="card-body">
        <form method="get" class="row">
            <div class="col-md-4 mb-2">
                <input type="text" name="keyword" class="form-control" 
                       placeholder="公司名称/联系人/手机号" value="${(keyword)!''}">
            </div>
            <div class="col-md-2 mb-2">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search mr-1"></i>搜索
                </button>
                <a href="/admin/customer/list" class="btn btn-secondary ml-2">重置</a>
            </div>
        </form>
    </div>
</div>

<!-- 客户列表 -->
<div class="card">
    <div class="card-body p-0">
        <table class="table">
            <thead>
                <tr>
                    <th>公司名称</th>
                    <th>联系人</th>
                    <th>手机号</th>
                    <th>地址</th>
                    <th>创建时间</th>
                </tr>
            </thead>
            <tbody>
                <#list page.records as customer>
                <tr>
                    <td><strong>${customer.name!''}</strong></td>
                    <td>${customer.contact!'-'}</td>
                    <td>${customer.mobile!''}</td>
                    <td><#assign _addr = customer.address!''><#if _addr?length gt 20>${_addr?substring(0, 20)}...<#else>${_addr}</#if></td>
                    <td>${customer.createTime!'-'}</td>
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
                <li class="page-item"><a class="page-link" href="?page=${page.current-1}&keyword=${(keyword!'')}">上一页</a></li>
                </#if>
                <li class="page-item active"><a class="page-link">${page.current}/${page.pages}</a></li>
                <#if page.current &lt; page.pages>
                <li class="page-item"><a class="page-link" href="?page=${page.current+1}&keyword=${(keyword!'')}">下一页</a></li>
                </#if>
            </ul>
        </nav>
    </div>
</div>

</@tpl.layout>
