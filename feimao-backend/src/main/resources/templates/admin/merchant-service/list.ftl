<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=serviceTitle>
<div class="page-header">
    <div><h4>${serviceTitle}</h4><p class="text-muted"><#if service == 'cars'>管理商户车辆、租赁价格和可用库存。<#else>管理商户提供的维修服务及价格。</#if></p></div>
    <div><button class="btn btn-secondary" id="refreshButton"><i class="fas fa-refresh"></i> 刷新</button> <button class="btn btn-danger" id="bulkDeleteButton" hidden disabled><i class="fas fa-trash"></i> 批量删除</button> <button class="btn btn-primary" id="addButton"><i class="fas fa-plus"></i> 新增</button></div>
</div>
<div class="card mb-4"><div class="card-body resource-toolbar">
    <select id="merchantFilter" class="form-select"><option value="">全部商户</option></select>
    <#if service == 'cars'><select id="brandFilter" class="form-select"><option value="">全部品牌</option></select><select id="statusFilter" class="form-select"><option value="">全部状态</option><option value="1">上架</option><option value="2">下架</option></select><input id="createdFrom" class="form-control" type="date" title="创建开始日期"><span class="text-muted">至</span><input id="createdTo" class="form-control" type="date" title="创建结束日期"><input id="keyword" class="form-control" placeholder="商家、品牌或车辆名称"></#if>
    <button class="btn btn-primary" id="searchButton">查询</button><button class="btn btn-secondary" id="resetButton">重置</button>
</div></div>
<div class="card"><div class="card-body p-0 table-responsive"><table class="table"><thead><tr id="serviceHead"></tr></thead><tbody id="serviceBody"><tr><td class="empty-state">正在加载...</td></tr></tbody></table></div>
<div class="card-footer resource-pagination"><span id="totalText">共 0 条记录</span><div><button class="btn btn-secondary btn-sm" id="prevButton">上一页</button> <span id="pageText">1 / 1</span> <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button></div></div></div>
<div class="resource-mask" id="editorMask" hidden><div class="resource-editor card"><div class="card-header"><span id="editorTitle">新增${serviceTitle}</span><button class="editor-close close-editor">×</button></div><div class="card-body"><form id="serviceForm"></form></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-editor">取消</button><button class="btn btn-primary" id="saveService">保存</button></div></div></div>
<script>window.businessMerchantServicePage = { service: '${service?js_string}', title: '${serviceTitle?js_string}' };</script>
<script src="/admin/js/merchant-service.js?v=20261006-3"></script>
</@tpl.layout>
