<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=partnerTitle>
<div class="page-header">
    <div><h4>${partnerTitle}</h4><p class="text-muted">管理已入驻合作方的账号、联系方式、业务配置和启用状态。</p></div>
    <div><#if role == 'shop'><button class="btn btn-danger" id="bulkDeleteButton" disabled><i class="fas fa-trash"></i> 删除</button> </#if><button class="btn btn-primary" id="addButton"><i class="fas fa-plus"></i> 新增</button></div>
</div>
<div class="card mb-4"><div class="card-body resource-toolbar">
    <input id="keyword" class="form-control" placeholder="名称/联系人/手机号/账号">
    <select id="statusFilter" class="form-select"><option value="">全部状态</option><option value="1">启用</option><option value="2">禁用</option></select>
    <select id="examineStatusFilter" class="form-select"><option value="">全部审核状态</option><option value="1">待审核</option><option value="2">已通过</option><option value="3">已拒绝</option></select>
    <button class="btn btn-primary" id="searchButton">查询</button><button class="btn btn-secondary" id="resetButton">重置</button>
</div></div>
<div class="card">
    <div class="card-body p-0 table-responsive"><table class="table">
        <thead><tr id="partnerHead"></tr></thead>
        <tbody id="partnerBody"><tr><td colspan="8" class="empty-state">正在加载...</td></tr></tbody>
    </table></div>
    <div class="card-footer resource-pagination"><span id="totalText">共 0 条记录</span><div><button class="btn btn-secondary btn-sm" id="prevButton">上一页</button> <span id="pageText">1 / 1</span> <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button></div></div>
</div>

<div class="resource-mask" id="editorMask" hidden><div class="resource-editor card">
    <div class="card-header"><span id="editorTitle">新增${partnerTitle}</span><button class="editor-close close-editor">×</button></div>
    <div class="card-body"><form id="partnerForm"></form></div>
    <div class="card-footer editor-actions"><button class="btn btn-secondary close-editor">取消</button><button class="btn btn-primary" id="savePartner">保存</button></div>
</div></div>
<div class="resource-mask" id="detailMask" hidden><div class="resource-editor card">
    <div class="card-header">资料详情<button class="editor-close close-detail">×</button></div>
    <div class="card-body"><table class="table"><tbody id="detailBody"></tbody></table></div>
    <div class="card-footer editor-actions"><button class="btn btn-secondary close-detail">关闭</button></div>
</div></div>
<div class="resource-mask" id="examineMask" hidden><div class="resource-editor card">
    <div class="card-header"><span id="examineTitle">审核</span><button class="editor-close close-examine">×</button></div>
    <div class="card-body">
        <input type="hidden" id="examineId"><input type="hidden" id="examineType">
        <div id="approveFields">
            <div class="form-group"><label>登录账号</label><input id="loginUsername" class="form-control" autocomplete="off"></div>
            <div class="form-group"><label>初始密码</label><input id="loginPassword" type="password" class="form-control" autocomplete="new-password"></div>
            <#if role == 'merchant'>
            <div class="form-row-3">
                <div class="form-group"><label>普通订单佣金(%)</label><input class="form-control approval-setting" data-key="commission_normal_rate" type="number" min="0" value="0"></div>
                <div class="form-group"><label>商户扫码佣金(%)</label><input class="form-control approval-setting" data-key="commission_merchant_rate" type="number" min="0" value="0"></div>
                <div class="form-group"><label>门店扫码佣金(%)</label><input class="form-control approval-setting" data-key="commission_shop_rate" type="number" min="0" value="0"></div>
            </div>
            <div class="form-row-4"><#list 1..4 as index><div class="form-group"><label>距离${index}(公里)</label><input class="form-control approval-setting" data-key="service_distance${index}" type="number" min="1" value="${index * 3}"></div></#list></div>
            <div class="form-row-5"><#list 1..5 as index><div class="form-group"><label>费用${index}(元)</label><input class="form-control approval-setting" data-key="service_money${index}" type="number" min="0" value="0"></div></#list></div>
            <#else><div class="form-group"><label>门店佣金比例(%)</label><input class="form-control approval-setting" data-key="commission_shop_rate" type="number" min="0" value="10"></div></#if>
        </div>
        <div id="rejectFields"><div class="form-group"><label>拒绝原因</label><textarea id="examineNote" class="form-control" rows="4"></textarea></div></div>
    </div>
    <div class="card-footer editor-actions"><button class="btn btn-secondary close-examine">取消</button><button class="btn btn-primary" id="submitExamine">确认提交</button></div>
</div></div>
<script>window.businessPartnerPage = { role: '${role?js_string}', title: '${partnerTitle?js_string}' };</script>
<#if role == 'merchant'><script charset="utf-8" src="https://map.qq.com/api/gljs?v=1.exp&libraries=service&key=HEYBZ-OD6KG-2WLQC-QRO7J-BSMZF-X2BDJ"></script></#if>
<script src="/admin/js/partner.js?v=20261006-4"></script>
</@tpl.layout>
