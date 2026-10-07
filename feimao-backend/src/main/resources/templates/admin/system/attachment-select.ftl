<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle="选择附件">
<div class="page-header">
    <div><h4>选择附件</h4><p class="text-muted" id="attachmentSelectorMode">请选择一个附件。</p></div>
    <div><button class="btn btn-primary" id="confirmAttachmentSelection" hidden><i class="fas fa-check"></i> 确认选择</button> <a class="btn btn-secondary" href="/admin/system/attachments"><i class="fas fa-arrow-left"></i> 返回附件管理</a></div>
</div>
<div class="card mb-4"><div class="card-body resource-toolbar">
    <input class="form-control" id="attachmentSelectorKeyword" placeholder="记录编号/文件名/物理路径">
    <select class="form-control" id="attachmentSelectorCategory"><option value="">全部类别</option></select>
    <input class="form-control" id="attachmentSelectorMimetype" placeholder="文件类型">
    <button class="btn btn-primary" id="searchAttachments"><i class="fas fa-search"></i> 查询</button>
    <button class="btn btn-success" id="uploadSelectorAttachments"><i class="fas fa-upload"></i> 上传到本地</button>
    <input id="selectorAttachmentFiles" type="file" multiple hidden accept=".jpg,.png,.bmp,.jpeg,.gif,.webp,.zip,.rar,.wav,.mp4,.mp3,.webm">
    <span class="text-muted" id="selectorUploadStatus"></span>
</div></div>
<div class="card mb-4"><div class="card-body p-0 table-responsive"><table class="table"><thead><tr id="attachmentSelectorHead"></tr></thead><tbody id="attachmentSelectorBody"><tr><td class="empty-state">正在加载...</td></tr></tbody></table></div><div class="card-footer resource-pagination"><span id="attachmentSelectorTotal">共 0 条记录</span><div><button class="btn btn-secondary btn-sm" id="attachmentSelectorPrev">上一页</button> <span id="attachmentSelectorPage">1 / 1</span> <button class="btn btn-secondary btn-sm" id="attachmentSelectorNext">下一页</button></div></div></div>
<div class="card" id="attachmentSelectorResult" hidden><div class="card-header">已选择的附件地址</div><div class="card-body"><textarea class="form-control" id="attachmentSelectorUrls" rows="3" readonly></textarea></div><div class="card-footer editor-actions"><span class="text-muted">当前页面独立打开，附件地址已显示在上方。</span><button class="btn btn-primary" id="copyAttachmentUrls"><i class="fas fa-copy"></i> 复制地址</button></div></div>
<script src="/admin/js/attachment-select.js?v=20261006-2"></script>
</@tpl.layout>
