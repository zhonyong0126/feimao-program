<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=accessTitle>
<div class="page-header">
  <div>
    <h4>${accessTitle}</h4>
    <p class="text-muted"><#if section == 'users'>会员资料与账号状态管理。<#else>账号与权限配置受当前管理员旧权限节点约束。</#if></p>
  </div>
  <#if section == 'users'>
    <div>
      <button class="btn btn-secondary" id="refreshMembers"><i class="fas fa-rotate"></i> 刷新</button>
      <button class="btn btn-danger" id="bulkMemberDelete" disabled><i class="fas fa-trash"></i> 删除</button>
      <button class="btn btn-primary" id="bulkMemberNormal" disabled><i class="fas fa-eye"></i> 设为正常</button>
      <button class="btn btn-secondary" id="bulkMemberHidden" disabled><i class="fas fa-eye-slash"></i> 设为隐藏</button>
    </div>
  <#elseif section == 'admins' || section == 'admin-groups' || section == 'admin-rules'>
    <div>
      <button class="btn btn-secondary" id="refreshAccess"><i class="fas fa-rotate"></i> 刷新</button>
      <button class="btn btn-primary" id="addButton" data-permission="add" style="display:none"><i class="fas fa-plus"></i> 新增</button>
      <button class="btn btn-danger" id="bulkRbacDelete" data-permission="delete" style="display:none" disabled><i class="fas fa-trash"></i> 删除</button>
      <#if section == 'admin-rules'><button class="btn btn-secondary" id="toggleAllRules"><i class="fas fa-plus"></i> 显示全部</button></#if>
    </div>
  <#elseif section != 'logs'>
    <button class="btn btn-primary" id="addButton"><i class="fas fa-plus"></i> 新增</button>
  <#else>
    <button class="btn btn-danger" id="bulkLogDelete" disabled><i class="fas fa-trash"></i> 删除</button>
  </#if>
</div>
<div class="card mb-4"><div class="card-body resource-toolbar" id="accessFilters"></div></div>
<div class="card"><div class="card-body p-0 table-responsive"><table class="table"><thead><tr id="accessHead"></tr></thead><tbody id="accessBody"><tr><td class="empty-state">正在加载...</td></tr></tbody></table></div><div class="card-footer resource-pagination"><span id="totalText">共 0 条记录</span><div><button class="btn btn-secondary btn-sm" id="prevButton">上一页</button> <span id="pageText">1 / 1</span> <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button></div></div></div>
<div class="resource-mask" id="editorMask" hidden><div class="resource-editor card"><div class="card-header"><span id="editorTitle">编辑</span><button class="editor-close close-editor">×</button></div><div class="card-body"><form id="accessForm"></form></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-editor">取消</button><button class="btn btn-primary" id="saveAccess">保存</button></div></div></div>
<#if section != 'users' && section != 'admins' && section != 'admin-groups' && section != 'admin-rules'><div class="resource-mask" id="detailMask" hidden><div class="resource-editor card"><div class="card-header">详情<button class="editor-close close-detail">×</button></div><div class="card-body"><table class="table"><tbody id="detailBody"></tbody></table></div><div class="card-footer editor-actions"><button class="btn btn-secondary close-detail">关闭</button></div></div></div></#if>
<script>window.businessAccessPage={section:'${section?js_string}',title:'${accessTitle?js_string}'};</script><script src="/admin/js/access.js?v=20261006-5"></script>
</@tpl.layout>
