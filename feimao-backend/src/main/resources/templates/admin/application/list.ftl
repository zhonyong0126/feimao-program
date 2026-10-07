<#import "../layout.ftl" as tpl>
<@tpl.layout active=active pageTitle=applicationTitle>
<div class="page-header">
    <div><h4>${applicationTitle}</h4><p class="text-muted">审核用户提交的入驻资料，通过时为其设置后台登录账号。</p></div>
</div>

<div class="card mb-4">
    <div class="card-body resource-toolbar">
        <select id="statusFilter" class="form-select">
            <option value="">全部状态</option>
            <option value="1">待审核</option>
            <option value="2">已通过</option>
            <option value="3">已拒绝</option>
        </select>
        <button class="btn btn-primary" id="searchButton"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-secondary" id="resetButton">重置</button>
    </div>
</div>

<div class="card">
    <div class="card-body p-0 table-responsive">
        <table class="table">
            <thead><tr><th>ID</th><th>名称</th><th>联系人</th><th>手机号</th><th>地址/营业时间</th><th>审核状态</th><th>申请时间</th><th>操作</th></tr></thead>
            <tbody id="applicationBody"><tr><td colspan="8" class="empty-state">正在加载...</td></tr></tbody>
        </table>
    </div>
    <div class="card-footer resource-pagination">
        <span id="totalText">共 0 条记录</span>
        <div><button class="btn btn-secondary btn-sm" id="prevButton">上一页</button> <span id="pageText">1 / 1</span> <button class="btn btn-secondary btn-sm" id="nextButton">下一页</button></div>
    </div>
</div>

<div class="resource-mask" id="detailMask" hidden>
    <div class="resource-editor card">
        <div class="card-header">申请资料<button class="editor-close close-mask">×</button></div>
        <div class="card-body"><table class="table"><tbody id="detailBody"></tbody></table></div>
        <div class="card-footer editor-actions"><button class="btn btn-secondary close-mask">关闭</button></div>
    </div>
</div>

<div class="resource-mask" id="examineMask" hidden>
    <div class="resource-editor card">
        <div class="card-header"><span id="examineTitle">审核</span><button class="editor-close close-examine">×</button></div>
        <div class="card-body">
            <input type="hidden" id="examineId">
            <input type="hidden" id="examineType">
            <div id="approveFields">
                <div class="form-group"><label>登录账号</label><input id="loginUsername" class="form-control" autocomplete="off"></div>
                <div class="form-group"><label>初始密码</label><input id="loginPassword" type="password" class="form-control" autocomplete="new-password"></div>
                <#if role == 'merchant'>
                <div class="form-row-3">
                    <div class="form-group"><label>普通订单佣金(%)</label><input class="form-control approval-setting" data-key="commission_normal_rate" type="number" min="0" value="0"></div>
                    <div class="form-group"><label>商户扫码佣金(%)</label><input class="form-control approval-setting" data-key="commission_merchant_rate" type="number" min="0" value="0"></div>
                    <div class="form-group"><label>门店扫码佣金(%)</label><input class="form-control approval-setting" data-key="commission_shop_rate" type="number" min="0" value="0"></div>
                </div>
                <div class="form-row-4">
                    <#list 1..4 as index><div class="form-group"><label>距离${index}(公里)</label><input class="form-control approval-setting" data-key="service_distance${index}" type="number" min="1" value="${index * 3}"></div></#list>
                </div>
                <div class="form-row-5">
                    <#list 1..5 as index><div class="form-group"><label>费用${index}(元)</label><input class="form-control approval-setting" data-key="service_money${index}" type="number" min="0" value="0"></div></#list>
                </div>
                <#else>
                <div class="form-group"><label>门店佣金比例(%)</label><input class="form-control approval-setting" data-key="commission_shop_rate" type="number" min="0" value="10"></div>
                </#if>
            </div>
            <div id="rejectFields"><div class="form-group"><label>拒绝原因</label><textarea id="examineNote" class="form-control" rows="4"></textarea></div></div>
        </div>
        <div class="card-footer editor-actions"><button class="btn btn-secondary close-examine">取消</button><button class="btn btn-primary" id="submitExamine">确认提交</button></div>
    </div>
</div>

<script>window.businessApplicationPage = { role: '${role?js_string}', title: '${applicationTitle?js_string}' };</script>
<script src="/admin/js/application.js"></script>
</@tpl.layout>
