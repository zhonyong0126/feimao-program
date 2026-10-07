<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=resourceTitle>
<div class="page-header">
    <div>
        <h4>${resourceTitle}</h4>
        <p class="text-muted">${resourceTitle} 管理功能</p>
    </div>
    <div>
        <button class="btn btn-secondary" id="refreshButton"><i class="fas fa-refresh"></i> 刷新</button>
        <#if resourceMutable>
            <#if resource == 'brand' || resource == 'platform-car'>
                <button class="btn btn-info" id="bulkRestoreButton" hidden disabled><i class="fas fa-undo"></i> 还原</button>
                <button class="btn btn-danger" id="bulkDestroyButton" hidden disabled><i class="fas fa-times"></i> 销毁</button>
                <button class="btn btn-success" id="restoreAllButton" hidden><i class="fas fa-undo"></i> 还原全部</button>
                <button class="btn btn-danger" id="destroyAllButton" hidden><i class="fas fa-times"></i> 清空回收站</button>
            </#if>
            <button class="btn btn-danger" id="bulkDeleteButton" hidden disabled><i class="fas fa-trash"></i> 批量删除</button>
            <#if resource == 'maintain'><button class="btn btn-secondary" id="recycleButton"><i class="fas fa-recycle"></i> 回收站</button></#if>
            <button class="btn btn-primary" id="addButton"><i class="fas fa-plus"></i> 新增</button>
        </#if>
    </div>
</div>

<div class="card mb-4" id="resourceFilterCard">
    <div class="card-body resource-toolbar">
        <input id="keyword" class="form-control" placeholder="输入关键词搜索">
        <div id="resourceFilters" class="resource-toolbar"></div>
        <button class="btn btn-primary" id="searchButton"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-secondary" id="resetButton">重置</button>
    </div>
</div>

<div class="card">
    <div class="card-body p-0 table-responsive">
        <table class="table">
            <thead><tr id="tableHead"></tr></thead>
            <tbody id="tableBody"><tr><td class="empty-state">正在加载...</td></tr></tbody>
        </table>
    </div>
    <div class="card-footer resource-pagination">
        <span id="totalText">共 0 条记录</span>
        <div>
            <button class="btn btn-secondary btn-sm" id="prevButton">上一页</button>
            <span id="pageText">1 / 1</span>
            <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button>
        </div>
    </div>
</div>

<#if resourceMutable>
<div class="resource-mask" id="editorMask" hidden>
    <div class="resource-editor card">
        <div class="card-header"><span id="editorTitle">新增${resourceTitle}</span><button id="closeEditor" class="editor-close">×</button></div>
        <div class="card-body"><form id="editorForm"></form></div>
        <div class="card-footer editor-actions">
            <button class="btn btn-secondary" id="cancelEditor">取消</button>
            <button class="btn btn-primary" id="saveEditor">保存</button>
        </div>
    </div>
</div>
</#if>

<script>window.businessResourcePage = { resource: '${resource?js_string}', title: '${resourceTitle?js_string}', mutable: ${resourceMutable?c} };</script>
<script src="/admin/js/resource.js?v=20261006-3"></script>
</@tpl.layout>
