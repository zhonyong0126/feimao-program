<#import "layout.ftl" as tpl>
<@tpl.layout active="dashboard" pageTitle="首页概览">

<div class="row mb-4">
    <div class="col-12">
        <h4 class="mb-0">首页概览</h4>
        <small class="text-muted">欢迎回来，今天是 ${( .now?string('yyyy年MM月dd日 EEEE'))!''}</small>
    </div>
</div>

<div class="page-header mt-4"><div><h4>安鑫出行控制台</h4><p class="text-muted">会员活跃与新增趋势。</p></div></div>
<div class="row mb-4" id="legacyDashboard">
    <div class="col-lg-6 mb-3"><div class="stat-card"><div class="stat-label">会员总数</div><div class="stat-value" data-key="totaluser">-</div></div></div>
    <div class="col-lg-6 mb-3"><div class="stat-card success"><div class="stat-label">管理员总数</div><div class="stat-value" data-key="totaladmin">-</div></div></div>
</div>
<div class="row mb-4">
    <div class="col-lg-8 mb-3"><div class="card h-100"><div class="card-header"><i class="fas fa-chart-column mr-2 text-primary"></i>近7日新增会员</div><div class="card-body"><div id="signupTrend" class="d-flex align-items-end justify-content-around" style="height:180px"></div></div></div></div>
    <div class="col-lg-4 mb-3"><div class="card h-100"><div class="card-header"><i class="fas fa-user-clock mr-2 text-info"></i>会员统计</div><div class="card-body"><p>今日新增会员：<strong data-key="todayusersignup">-</strong></p><p>今日登录会员：<strong data-key="todayuserlogin">-</strong></p><p>三日新增会员：<strong data-key="threednu">-</strong></p><p>七日新增会员：<strong data-key="sevendnu">-</strong></p><p>七日活跃会员：<strong data-key="sevendau">-</strong></p><p>三十日活跃会员：<strong data-key="thirtydau">-</strong></p></div></div></div>
</div>

<div class="page-header mt-4"><div><h4>飞猫配送</h4><p class="text-muted">配送订单、司机、员工和客户统计。</p></div></div>

<!-- 统计卡片 -->
<div class="row mb-4">
    <div class="col-lg-3 col-md-6 mb-3">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">待派单</div>
                    <div class="stat-value">${(dashboard.pendingOrders)!0}</div>
                </div>
                <i class="fas fa-clock stat-icon"></i>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6 mb-3">
        <div class="stat-card success">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">总订单</div>
                    <div class="stat-value">${(dashboard.totalOrders)!0}</div>
                </div>
                <i class="fas fa-shopping-cart stat-icon"></i>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6 mb-3">
        <div class="stat-card info">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">活跃司机</div>
                    <div class="stat-value">${(dashboard.totalDrivers)!0}</div>
                </div>
                <i class="fas fa-truck stat-icon"></i>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6 mb-3">
        <div class="stat-card warning">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">员工总数</div>
                    <div class="stat-value">${(dashboard.totalStaff)!0}</div>
                </div>
                <i class="fas fa-users stat-icon"></i>
            </div>
        </div>
    </div>
</div>

<!-- 快捷操作 -->
<div class="row mb-4">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <i class="fas fa-bolt mr-2 text-warning"></i>快捷操作
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3 col-sm-6 mb-2">
                        <a href="/admin/order/list?status=0" class="btn btn-primary btn-block">
                            <i class="fas fa-clock mr-2"></i>待派单订单
                        </a>
                    </div>
                    <div class="col-md-3 col-sm-6 mb-2">
                        <a href="/admin/order/list?status=2" class="btn btn-info btn-block">
                            <i class="fas fa-shipping-fast mr-2"></i>配送中订单
                        </a>
                    </div>
                    <div class="col-md-3 col-sm-6 mb-2">
                        <a href="/admin/driver/list?onlineStatus=1" class="btn btn-success btn-block">
                            <i class="fas fa-truck mr-2"></i>在线司机
                        </a>
                    </div>
                    <div class="col-md-3 col-sm-6 mb-2">
                        <a href="/admin/customer/list" class="btn btn-warning btn-block">
                            <i class="fas fa-building mr-2"></i>客户列表
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 系统信息 -->
<div class="row">
    <div class="col-lg-6 mb-4">
        <div class="card h-100">
            <div class="card-header">
                <i class="fas fa-chart-pie mr-2 text-info"></i>订单状态分布
            </div>
            <div class="card-body">
                <div class="d-flex justify-content-around text-center">
                    <div>
                        <div class="text-warning display-4 font-weight-bold">${(dashboard.pendingOrders)!0}</div>
                        <div class="text-muted small">待处理</div>
                    </div>
                    <div>
                        <div class="text-info display-4 font-weight-bold">${(dashboard.totalOrders)!0}</div>
                        <div class="text-muted small">总订单</div>
                    </div>
                    <div>
                        <div class="text-success display-4 font-weight-bold">${(dashboard.totalCustomers)!0}</div>
                        <div class="text-muted small">客户数</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-lg-6 mb-4">
        <div class="card h-100">
            <div class="card-header">
                <i class="fas fa-info-circle mr-2 text-primary"></i>系统信息
            </div>
            <div class="card-body">
                <table class="table table-sm">
                    <tr>
                        <td class="border-0 text-muted">系统名称</td>
                        <td class="border-0">飞猫配送管理系统</td>
                    </tr>
                    <tr>
                        <td class="text-muted">版本号</td>
                        <td>v1.0.0</td>
                    </tr>
                    <tr>
                        <td class="text-muted">技术栈</td>
                        <td>Spring Boot + MyBatis Plus + Redis</td>
                    </tr>
                    <tr>
                        <td class="text-muted">服务器时间</td>
                        <td>${(.now?string('yyyy-MM-dd HH:mm:ss'))!''}</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>

<script src="/admin/js/dashboard.js?v=20261006-1"></script>

</@tpl.layout>
