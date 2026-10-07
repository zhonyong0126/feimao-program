<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=systemTitle>
<div class="page-header">
    <div><h4>${systemTitle}</h4><p class="text-muted"><#if section == 'config'>按旧后台分组维护系统与小程序配置。<#else>系统基础资料与认证记录管理。</#if></p></div>
    <div>
        <#if section == 'attachments'>
            <button class="btn btn-secondary" id="refreshAttachments"><i class="fas fa-sync-alt"></i> 刷新</button>
            <button class="btn btn-primary" id="openAttachmentUpload"><i class="fas fa-upload"></i> 上传到本地</button>
            <button class="btn btn-secondary" id="bulkAttachmentClassify" disabled><i class="fas fa-folder"></i> 批量归类</button>
            <button class="btn btn-danger" id="bulkAttachmentDelete" disabled><i class="fas fa-trash"></i> 批量删除</button>
        </#if>
        <#if section == 'addresses'><button class="btn btn-danger" id="bulkAddressDelete" disabled><i class="fas fa-trash"></i> 删除</button></#if>
        <button class="btn btn-primary" id="addButton" hidden><i class="fas fa-plus"></i> 新增地址</button>
    </div>
</div>
<#if section == 'config'>
<style>
    #configCard .card-body{padding:0}
    #configForm .config-row{display:grid;grid-template-columns:minmax(130px,18%) minmax(0,1fr);gap:20px;align-items:start;margin:0;padding:18px 22px;border-bottom:1px solid var(--c-border)}
    #configForm .config-row>label{margin:9px 0 0;font-weight:600;color:var(--c-text)}
    #configForm .config-row[hidden]{display:none}
    #configForm .config-control{max-width:900px;min-width:0}
    #configForm .config-choice-list{display:flex;flex-wrap:wrap;gap:10px 22px;padding:8px 0}
    #configForm .config-choice-list label{display:inline-flex;align-items:center;gap:6px;margin:0;font-weight:400}
    #configForm select[multiple]{min-height:150px}
    #configForm .config-array-head,#configForm .config-array-row{display:grid;grid-template-columns:minmax(120px,1fr) minmax(160px,2fr) 72px;gap:8px;margin-bottom:8px;align-items:center}
    #configForm .config-array-head{font-size:13px;color:var(--c-text-secondary);padding:0 2px}
    #configForm .config-array-add{margin-top:4px}
    #configForm .config-dynamic-search{display:flex;gap:8px;margin-bottom:8px}
    #configForm .config-dynamic-search .form-control{min-width:0}
    @media(max-width:700px){#configForm .config-row{grid-template-columns:1fr;gap:8px;padding:15px}#configForm .config-row>label{margin:0}#configForm .config-array-head{display:none}#configForm .config-array-row{grid-template-columns:1fr 1fr}.config-array-row .config-array-remove{grid-column:1/-1;justify-self:end}}
</style>
<div class="card" id="configCard">
    <div class="card-header"><div class="access-tabs mb-0" id="configGroupTabs"></div></div>
    <div class="card-body"><form id="configForm"><div class="empty-state">正在加载...</div></form></div>
    <div class="card-footer editor-actions"><button class="btn btn-secondary" id="resetConfig" type="button">重置</button><button class="btn btn-primary" id="saveConfig" type="button">确定</button></div>
</div>
<#else>
<div class="card mb-4"><div class="card-body resource-toolbar" id="systemFilters"></div></div>
<div class="card"><div class="card-body p-0 table-responsive"><table class="table"><thead><tr id="systemHead"></tr></thead><tbody id="systemBody"><tr><td class="empty-state">正在加载...</td></tr></tbody></table></div><div class="card-footer resource-pagination"><span id="totalText">共 0 条记录</span><div><button class="btn btn-secondary btn-sm" id="prevButton">上一页</button> <span id="pageText">1 / 1</span> <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button></div></div></div>
<div class="resource-mask" id="editorMask" hidden><div class="resource-editor card"><div class="card-header"><span id="editorTitle">编辑</span><button class="editor-close close-editor">×</button></div><div class="card-body"><form id="systemForm"></form></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-editor">取消</button><button class="btn btn-primary" id="saveSystem">保存</button></div></div></div>
</#if>
<#if section == 'attachments'>
<div class="resource-mask" id="attachmentCategoryMask" hidden><div class="resource-editor card"><div class="card-header"><span>附件归类</span><button class="editor-close close-category">×</button></div><div class="card-body"><div class="form-group"><label for="attachmentCategorySelect">目标分类</label><select class="form-control" id="attachmentCategorySelect"></select></div><p class="text-muted" id="attachmentCategoryCount"></p></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-category">取消</button><button class="btn btn-primary" id="saveAttachmentCategory">确定归类</button></div></div></div>
<div class="resource-mask" id="attachmentUploadMask" hidden><div class="resource-editor card"><div class="card-header"><span>上传到本地</span><button class="editor-close close-upload">×</button></div><div class="card-body"><div class="form-group"><label for="attachmentUploadCategory">类别</label><select class="form-control" id="attachmentUploadCategory"><option value="">请选择一个类别</option></select></div><div class="form-group"><label for="attachmentUploadFiles">选择文件（支持多选）</label><input class="form-control" id="attachmentUploadFiles" type="file" multiple accept=".jpg,.png,.bmp,.jpeg,.gif,.webp,.zip,.rar,.wav,.mp4,.mp3,.webm"></div><p class="text-muted">单个文件最大 10MB，支持图片、压缩包、音频和视频等旧上传配置中的文件类型。</p><div id="attachmentUploadResults" aria-live="polite"></div></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-upload">取消</button><button class="btn btn-primary" id="startAttachmentUpload"><i class="fas fa-upload"></i> 开始上传</button></div></div></div>
</#if>
<#if section != 'config'><div class="resource-mask" id="detailMask" hidden><div class="resource-editor card"><div class="card-header">详情<button class="editor-close close-detail">×</button></div><div class="card-body"><table class="table"><tbody id="detailBody"></tbody></table></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-detail">关闭</button></div></div></div></#if>
<script>window.businessSystemPage={section:'${section?js_string}',title:'${systemTitle?js_string}'};</script>
<script src="/admin/js/system.js?v=20261006-13"></script>
</@tpl.layout>
