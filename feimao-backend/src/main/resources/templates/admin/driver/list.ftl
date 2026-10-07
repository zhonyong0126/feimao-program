<#import "../layout.ftl" as tpl>
<@tpl.layout active="driver" pageTitle="司机管理">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">司机管理</h4>
</div>

<!-- 搜索栏 -->
<div class="card mb-4">
    <div class="card-body">
        <form method="get" class="row">
            <div class="col-md-3 mb-2">
                <input type="text" name="keyword" class="form-control" 
                       placeholder="姓名/手机号/车牌号" value="${(keyword)!''}">
            </div>
            <div class="col-md-2 mb-2">
                <select name="status" class="form-control">
                    <option value="">全部状态</option>
                    <option value="1" ${(status?? && status==1)?then('selected','')}>启用</option>
                    <option value="0" ${(status?? && status==0)?then('selected','')}>禁用</option>
                </select>
            </div>
            <div class="col-md-2 mb-2">
                <select name="authStatus" class="form-control">
                    <option value="">认证状态</option>
                    <option value="0" ${(authStatus?? && authStatus==0)?then('selected','')}>未认证</option>
                    <option value="1" ${(authStatus?? && authStatus==1)?then('selected','')}>审核中</option>
                    <option value="2" ${(authStatus?? && authStatus==2)?then('selected','')}>已认证</option>
                    <option value="3" ${(authStatus?? && authStatus==3)?then('selected','')}>认证失败</option>
                </select>
            </div>
            <div class="col-md-2 mb-2">
                <select name="onlineStatus" class="form-control">
                    <option value="">全部在线状态</option>
                    <option value="0" ${(onlineStatus?? && onlineStatus==0)?then('selected','')}>离线</option>
                    <option value="1" ${(onlineStatus?? && onlineStatus==1)?then('selected','')}>在线空闲</option>
                    <option value="2" ${(onlineStatus?? && onlineStatus==2)?then('selected','')}>运输中</option>
                </select>
            </div>
            <div class="col-md-2 mb-2">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search mr-1"></i>搜索
                </button>
                <a href="/admin/driver/list" class="btn btn-secondary ml-2">重置</a>
            </div>
        </form>
    </div>
</div>

<!-- 司机列表 -->
<div class="card">
    <div class="card-body p-0">
        <table class="table">
            <thead>
                <tr>
                    <th>姓名</th>
                    <th>手机号</th>
                    <th>车牌号</th>
                    <th>车型</th>
                    <th>认证状态</th>
                    <th>在线状态</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <#list page.records as driver>
                <tr>
                    <td>${driver.name!''}</td>
                    <td>${driver.mobile!''}</td>
                    <td>${driver.plateNumber!''}</td>
                    <td>${driver.vehicleType!'-'}</td>
                    <td>
                        <#if driver.authStatus == 0>
                        <span class="badge badge-secondary">未认证</span>
                        <#elseif driver.authStatus == 1>
                        <span class="badge badge-warning">审核中</span>
                        <#elseif driver.authStatus == 2>
                        <span class="badge badge-success">已认证</span>
                        <#else>
                        <span class="badge badge-danger">认证失败</span>
                        </#if>
                    </td>
                    <td>
                        <#if driver.onlineStatus == 0>
                        <span class="badge badge-secondary">离线</span>
                        <#elseif driver.onlineStatus == 1>
                        <span class="badge badge-success">空闲</span>
                        <#else>
                        <span class="badge badge-info">运输中</span>
                        </#if>
                    </td>
                    <td>
                        <#if driver.status == 1>
                        <span class="badge badge-success">启用</span>
                        <#else>
                        <span class="badge badge-secondary">禁用</span>
                        </#if>
                    </td>
                    <td>
                        <a href="/admin/driver/detail?id=${driver.id}" class="btn btn-info btn-sm">
                            <i class="fas fa-eye"></i>
                        </a>
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
                <li class="page-item"><a class="page-link" href="?page=${page.current-1}&status=${(status!'')}&authStatus=${(authStatus!'')}&onlineStatus=${(onlineStatus!'')}&keyword=${(keyword!'')}">上一页</a></li>
                </#if>
                <li class="page-item active"><a class="page-link">${page.current}/${page.pages}</a></li>
                <#if page.current &lt; page.pages>
                <li class="page-item"><a class="page-link" href="?page=${page.current+1}&status=${(status!'')}&authStatus=${(authStatus!'')}&onlineStatus=${(onlineStatus!'')}&keyword=${(keyword!'')}">下一页</a></li>
                </#if>
            </ul>
        </nav>
    </div>
</div>

</@tpl.layout>
