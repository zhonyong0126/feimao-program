<#import "../layout.ftl" as tpl>
<@tpl.layout active="staff" pageTitle="员工管理">

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="mb-0">员工管理</h4>
    <button class="btn btn-primary" onclick="openAdd()">
        <i class="fas fa-plus mr-2"></i>添加员工
    </button>
</div>

<!-- 搜索栏 -->
<div class="card mb-4">
    <div class="card-body">
        <form method="get" class="row">
            <div class="col-md-3 mb-2">
                <input type="text" name="keyword" class="form-control" 
                       placeholder="姓名/手机号" value="${(keyword)!''}">
            </div>
            <div class="col-md-2 mb-2">
                <select name="status" class="form-control">
                    <option value="">全部状态</option>
                    <option value="1" ${(status?? && status==1)?then('selected','')}>启用</option>
                    <option value="0" ${(status?? && status==0)?then('selected','')}>禁用</option>
                </select>
            </div>
            <div class="col-md-2 mb-2">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search mr-1"></i>搜索
                </button>
                <a href="/admin/staff/list" class="btn btn-secondary ml-2">重置</a>
            </div>
        </form>
    </div>
</div>

<!-- 员工列表 -->
<div class="card">
    <div class="card-body p-0">
        <table class="table">
            <thead>
                <tr>
                    <th>员工编号</th>
                    <th>姓名</th>
                    <th>手机号</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <#list page.records as staff>
                <tr>
                    <td>${staff.staffNo!''}</td>
                    <td>${staff.name!''}</td>
                    <td>${staff.mobile!''}</td>
                    <td>
                        <#if staff.role == 1>
                        <span class="badge badge-info">调度员</span>
                        <#else>
                        <span class="badge badge-primary">管理员</span>
                        </#if>
                    </td>
                    <td>
                        <#if staff.status == 1>
                        <span class="badge badge-success">启用</span>
                        <#else>
                        <span class="badge badge-secondary">禁用</span>
                        </#if>
                    </td>
                    <td>${staff.createTime!'-'}</td>
                    <td>
                        <button class="btn btn-warning btn-sm"
                                data-id="${staff.id}"
                                data-staff-no="${(staff.staffNo!'')?html}"
                                data-name="${(staff.name!'')?html}"
                                data-mobile="${(staff.mobile!'')?html}"
                                data-role="${staff.role!1}"
                                data-status="${staff.status!1}"
                                onclick="openEdit(this)">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger btn-sm ml-1" onclick="deleteStaff(${staff.id})">
                            <i class="fas fa-trash"></i>
                        </button>
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
                <li class="page-item"><a class="page-link" href="?page=${page.current-1}&status=${(status!'')}&keyword=${(keyword!'')}">上一页</a></li>
                </#if>
                <li class="page-item active"><a class="page-link">${page.current}/${page.pages}</a></li>
                <#if page.current &lt; page.pages>
                <li class="page-item"><a class="page-link" href="?page=${page.current+1}&status=${(status!'')}&keyword=${(keyword!'')}">下一页</a></li>
                </#if>
            </ul>
        </nav>
    </div>
</div>

<!-- 添加/编辑模态框 -->
<div class="modal fade" id="staffModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalTitle">添加员工</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <form id="staffForm">
                    <input type="hidden" id="staffId">
                    <div class="form-group">
                        <label>员工编号</label>
                        <input type="text" class="form-control" id="staffNo" required>
                    </div>
                    <div class="form-group">
                        <label>姓名</label>
                        <input type="text" class="form-control" id="staffName" required>
                    </div>
                    <div class="form-group">
                        <label>登录密码 <small class="text-muted">新增必填，编辑留空表示不修改</small></label>
                        <input type="password" class="form-control" id="staffPassword" minlength="8">
                    </div>
                    <div class="form-group">
                        <label>手机号</label>
                        <input type="text" class="form-control" id="staffMobile" required>
                    </div>
                    <div class="form-group">
                        <label>角色</label>
                        <select class="form-control" id="staffRole">
                            <option value="1">调度员</option>
                            <option value="2">管理员</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>状态</label>
                        <select class="form-control" id="staffStatus">
                            <option value="1">启用</option>
                            <option value="0">禁用</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" onclick="saveStaff()">保存</button>
            </div>
        </div>
    </div>
</div>

<script>
function openAdd() {
    $('#modalTitle').text('添加员工');
    $('#staffId').val('');
    $('#staffNo').val('');
    $('#staffName').val('');
    $('#staffMobile').val('');
    $('#staffRole').val('1');
    $('#staffPassword').val('');
    $('#staffStatus').val('1');
    $('#staffModal').modal('show');
}

function openEdit(button) {
    const data = button.dataset;
    $('#modalTitle').text('编辑员工');
    $('#staffId').val(data.id);
    $('#staffNo').val(data.staffNo);
    $('#staffName').val(data.name);
    $('#staffMobile').val(data.mobile);
    $('#staffRole').val(data.role);
    $('#staffPassword').val('');
    $('#staffStatus').val(data.status);
    $('#staffModal').modal('show');
}

function saveStaff() {
    const form = document.getElementById('staffForm');
    if (!form.reportValidity()) {
        return;
    }
    const data = {
        id: $('#staffId').val() || null,
        staffNo: $('#staffNo').val(),
        name: $('#staffName').val(),
        mobile: $('#staffMobile').val(),
        role: $('#staffRole').val(),
        status: $('#staffStatus').val()
    };
    if (!/^1\d{10}$/.test(data.mobile.trim())) {
        alert('请输入正确的11位手机号');
        return;
    }
    const password = $('#staffPassword').val();
    if (!data.id && password.length < 8) { alert('新增员工密码不能少于8位'); return; }
    if (password) data.password = password;
    const url = data.id ? '/api/admin/staff/update' : '/api/admin/staff/add';
    api(url, 'POST', data).then(function(res) {
        if (res.code === 200) {
            $('#staffModal').modal('hide');
            location.reload();
        } else {
            alert(res.msg || '保存失败');
        }
    });
}

function deleteStaff(id) {
    if (confirm('确定删除该员工？')) {
        api('/api/admin/staff/delete/' + id, 'POST').then(function(res) {
            if (res.code === 200) {
                location.reload();
            } else {
                alert(res.msg || '删除失败');
            }
        });
    }
}
</script>

</@tpl.layout>
