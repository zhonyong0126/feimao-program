
<#macro layout active="" pageTitle="">
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><#if pageTitle??>${pageTitle} - </#if>飞猫后台管理</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.1/css/all.min.css">
    <link rel="stylesheet" href="/css/style.css?v=20261006-9">
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
    <script>
        function api(url, method, data) {
            return $.ajax({
                url: url,
                type: method || 'GET',
                contentType: 'application/json',
                data: data ? JSON.stringify(data) : undefined,
                headers: { token: localStorage.getItem('token') }
            });
        }
        function logout() {
            var token = localStorage.getItem('token');
            if (token) api('/api/admin/business/logout', 'POST').always(function() {
                localStorage.removeItem('token');
                localStorage.removeItem('staff');
                localStorage.removeItem('userInfo');
                document.cookie = 'admin_token=; Max-Age=0; Path=/; SameSite=Lax';
                window.location.href = '/admin/login';
            });
        }
        function uploadInto(target, append) {
            var input = document.createElement('input');
            input.type = 'file';
            input.accept = '.jpg,.png,.bmp,.jpeg,.gif,.webp,.zip,.rar,.wav,.mp4,.mp3,.webm';
            input.onchange = function() {
                if (!input.files || !input.files[0]) return;
                var form = new FormData();
                form.append('file', input.files[0]);
                form.append('token', localStorage.getItem('token') || '');
                $.ajax({url:'/api/common/upload',type:'POST',data:form,processData:false,contentType:false,headers:{token:localStorage.getItem('token')}}).done(function(response) {
                    if (response.code !== 1) return alert(response.msg || '上传失败');
                    var field = $(target), url = response.data.url;
                    field.val(append && field.val() ? field.val() + ',' + url : url).trigger('change');
                }).fail(function() { alert('上传失败，请检查文件大小和登录状态'); });
            };
            input.click();
        }
        var adminDetailLabels = {
            id:'记录编号', name:'名称', title:'标题', username:'用户名', nickname:'昵称', mobile:'手机号', email:'邮箱', avatar:'头像',
            status:'状态', state:'状态', createtime:'创建时间', updatetime:'更新时间', logintime:'登录时间', jointime:'加入时间', loginip:'登录网络地址',
            user_id:'用户', user_nickname:'用户昵称', user_mobile:'用户手机号', user_avatar:'用户头像', admin_id:'管理员编号',
            merchant_id:'门店', merchant_name:'门店名称', merchant_mobile:'门店手机号', merchant_car_id:'门店车辆编号',
            shop_id:'酒店编号', group_id:'分组编号', groups:'所属角色组', group:'分组', member_count:'成员数量',
            gender:'性别', birthday:'生日', bio:'个人简介', score:'积分', money:'余额', group_name:'会员组', authentication:'实名认证状态', staff_authentication:'员工认证状态',
            truename:'真实姓名', shop_status:'酒店状态', merchant_status:'门店状态', level:'等级', openid:'微信用户标识',
            pid:'上级编号', rules:'权限规则', rule_ids:'权限规则编号', group_ids:'分组编号',
            weigh:'权重', icon:'图标', menutype:'菜单打开方式', py:'拼音首字母', remark:'备注', type:'类型', url:'地址',
            extend:'扩展配置', condition:'条件', pinyin:'拼音', ismenu:'是否菜单', children:'下级数据', content:'内容',
            admin_name:'管理员', useragent:'浏览器信息', ip:'网络地址',
            pick_type:'取车方式', pick_point:'取车点', pick_single:'送车上门类型', pick_state:'还车状态', pick_price:'送车费用',
            return_type:'还车车辆状况', num:'数量', number:'订单编号', order_number:'订单编号', child_number:'分期单号', order_name:'订单标题', order_id:'订单记录编号',
            starttime:'开始时间', endtime:'结束时间', paytime:'支付时间', backtime:'还车时间', take_back_time:'期望取还时间', deletetime:'删除时间',
            coupon_id:'优惠券编号', price:'订单金额', pay_price:'实付金额', discounts_price:'优惠金额', reduceprice:'减免金额', insure_money:'保险费用',
            lease_type:'租赁方式', lease_duration:'租赁时长', lease_price:'租赁单价', lease_money:'租赁金额', last_all_money:'最终金额',
            source_type:'订单来源', source_id:'来源编号', service_name:'服务名称', service_image:'服务图片',
            address_id:'地址编号', address_name:'地址联系人', address_phone:'地址联系电话', idcard:'身份证号',
            cash_state:'押金状态', cash_price:'押金金额', refund_cash_price:'退还押金金额', refund_number:'押金退还单号',
            back_image:'还车图片', back_content:'还车留言', transaction_id:'微信支付编号',
            cancel_id:'取消人编号', cancel_time:'取消订单时间', cancel_refund_price:'取消订单退款金额', cancel_refund_number:'取消订单退款单号',
            is_delete:'删除状态', is_insure:'投保状态', guests:'乘坐人', installments:'分期记录',
            num_id:'序号', biz_order_sn:'保险业务单号', amount:'金额', user_type:'用户类型', log_note:'流水说明', memo:'说明',
            out_trade_no:'提现单号', owner_name:'所属方', owner_mobile:'所属方手机号',
            pick_address:'取车地址', address:'地址', administrative:'行政区域', lat:'纬度', lng:'经度', intro:'简介', image:'图片', images:'图片集',
            car_num:'车辆数量', min_price:'最低价格', business:'营业时间', qrimage:'小程序码', examine_status:'审核状态', examine_id:'审核人编号',
            commission_money:'佣金', commission_normal_rate:'普通订单分佣比例', commission_merchant_rate:'门店扫码分佣比例', commission_shop_rate:'酒店扫码分佣比例',
            service_distance1:'配送距离一', service_distance2:'配送距离二', service_distance3:'配送距离三', service_distance4:'配送距离四',
            service_money1:'配送费用一', service_money2:'配送费用二', service_money3:'配送费用三', service_money4:'配送费用四', service_money5:'配送费用五',
            authentication_name:'实名姓名', authentication_mobile:'实名手机号', authentication_card:'实名身份证号',
            staff_name:'员工姓名', staff_mobile:'员工手机号', staff_work:'工作证明',
            visible:'是否可见', rule:'验证规则', tip:'提示', value:'配置值', setting:'配置设置', category:'分类',
            extparam:'扩展参数', imagetype:'图片类型', imagewidth:'图片宽度', imageheight:'图片高度', imageframes:'图片帧数',
            filesize:'文件大小', storage:'存储方式', sha1:'文件校验值', filename:'文件名', mimetype:'文件类型', uploadtime:'上传时间'
        };
        function adminDetailLabel(key) {
            return adminDetailLabels[key] || '其他信息';
        }
        function adminDetailValue(key, value) {
            if (value == null || value === '') return '-';
            if (key === 'status' && value === 'normal') return '正常';
            if (key === 'status' && value === 'hidden') return '禁用';
            if (key === 'type' && value === 'file') return '菜单或权限';
            if (key === 'is_delete') return Number(value) === 1 ? '正常' : '已删除';
            if (key === 'ismenu' || key === 'visible') return Number(value) === 1 ? '是' : '否';
            if (/time$/.test(key) && /^\d{10}$/.test(String(value))) {
                var date = new Date(Number(value) * 1000);
                var pad = function(number) { return String(number).padStart(2, '0'); };
                return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' '
                    + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
            }
            return typeof value === 'object' ? JSON.stringify(value) : value;
        }
    </script>
</head>
<body>

    <!-- 侧边栏 -->
    <aside class="sidebar" id="sidebar">
        <div class="sidebar-brand">
            <i class="fas fa-cat"></i>
            <span class="brand-text">飞猫管理</span>
        </div>
        <nav class="sidebar-nav">
            <a class="nav-item <#if (active!) == 'dashboard'>active</#if>" href="/admin/">
                <i class="fas fa-th-large"></i><span>控制台</span>
            </a>

            <details class="nav-group" <#if ['order','driver','driver-withdrawals','staff','customer']?seq_contains(active!)>open</#if>>
                <summary><i class="fas fa-truck"></i><span>飞猫配送</span><i class="fas fa-chevron-down nav-arrow"></i></summary>
                <div class="nav-group-items">
                    <a class="nav-item <#if (active!) == 'order'>active</#if>" href="/admin/order/list"><i class="fas fa-clipboard-list"></i><span>配送订单</span></a>
                    <a class="nav-item <#if (active!) == 'driver'>active</#if>" href="/admin/driver/list"><i class="fas fa-truck"></i><span>司机管理</span></a>
                    <a class="nav-item <#if (active!) == 'driver-withdrawals'>active</#if>" href="/admin/driver/withdrawals"><i class="fas fa-wallet"></i><span>司机提现</span></a>
                    <a class="nav-item <#if (active!) == 'staff'>active</#if>" href="/admin/staff/list"><i class="fas fa-users-cog"></i><span>员工管理</span></a>
                    <a class="nav-item <#if (active!) == 'customer'>active</#if>" href="/admin/customer/list"><i class="fas fa-building"></i><span>配送客户</span></a>
                </div>
            </details>

            <details class="nav-group" <#if (active!)?starts_with('business-')>open</#if>>
                <summary><i class="fas fa-motorcycle"></i><span>安鑫出行</span><i class="fas fa-chevron-down nav-arrow"></i></summary>
                <div class="nav-group-items">
                    <div class="nav-subtitle">用户管理</div>
                    <a class="nav-item <#if (active!) == 'business-access-users'>active</#if>" href="/admin/access/users"><i class="fas fa-user-group"></i><span>会员管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-brand'>active</#if>" href="/admin/resources/brand"><i class="fas fa-tags"></i><span>品牌管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-platform-car'>active</#if>" href="/admin/resources/platform-car"><i class="fas fa-motorcycle"></i><span>平台车辆管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-partner-merchant'>active</#if>" href="/admin/partners/merchant"><i class="fas fa-store"></i><span>门店管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-merchant-cars'>active</#if>" href="/admin/merchant-services/cars"><i class="fas fa-warehouse"></i><span>门店车辆管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-partner-shop'>active</#if>" href="/admin/partners/shop"><i class="fas fa-building"></i><span>酒店管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-order-orders'>active</#if>" href="/admin/orders/orders"><i class="fas fa-file-invoice"></i><span>订单列表</span></a>
                    <a class="nav-item <#if (active!) == 'business-order-guests'>active</#if>" href="/admin/orders/guests"><i class="fas fa-people-group"></i><span>订单乘坐人管理</span></a>
                    <div class="nav-subtitle">财务管理</div>
                    <a class="nav-item <#if (active!) == 'business-fund-cash-merchant'>active</#if>" href="/admin/funds/cash-merchant"><i class="fas fa-money-check-dollar"></i><span>门店提现订单</span></a>
                    <a class="nav-item <#if (active!) == 'business-fund-cash-shop'>active</#if>" href="/admin/funds/cash-shop"><i class="fas fa-money-bill-transfer"></i><span>酒店提现订单</span></a>
                    <a class="nav-item <#if (active!) == 'business-fund-finance-platform'>active</#if>" href="/admin/funds/finance-platform"><i class="fas fa-chart-line"></i><span>平台流水管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-fund-finance-system'>active</#if>" href="/admin/funds/finance-system"><i class="fas fa-coins"></i><span>平台收益管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-banner'>active</#if>" href="/admin/resources/banner"><i class="fas fa-images"></i><span>轮播图管理</span></a>
                    <div class="nav-subtitle">常规管理</div>
                    <a class="nav-item <#if (active!) == 'business-system-config'>active</#if>" href="/admin/system/config"><i class="fas fa-sliders"></i><span>系统配置</span></a>
                    <a class="nav-item <#if (active!) == 'business-system-attachments'>active</#if>" href="/admin/system/attachments"><i class="fas fa-paperclip"></i><span>附件管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-profile'>active</#if>" href="/admin/profile"><i class="fas fa-address-card"></i><span>个人资料</span></a>
                    <div class="nav-subtitle">权限管理</div>
                    <a class="nav-item <#if (active!) == 'business-access-admins'>active</#if>" href="/admin/access/admins"><i class="fas fa-user-gear"></i><span>管理员管理</span></a>
                    <a class="nav-item <#if (active!) == 'business-access-admin-groups'>active</#if>" href="/admin/access/admin-groups"><i class="fas fa-people-roof"></i><span>角色组</span></a>
                    <a class="nav-item <#if (active!) == 'business-access-admin-rules'>active</#if>" href="/admin/access/admin-rules"><i class="fas fa-key"></i><span>菜单规则</span></a>
                    <a class="nav-item <#if (active!) == 'business-access-logs'>active</#if>" href="/admin/access/logs"><i class="fas fa-clock-rotate-left"></i><span>管理员日志</span></a>
                </div>
            </details>
        </nav>
        <div class="sidebar-footer">
            <span>v1.0</span>
        </div>
    </aside>

    <!-- 主区域 -->
    <div class="main-wrap">
        <!-- 顶栏 -->
        <header class="topbar">
            <button class="topbar-toggle" onclick="document.getElementById('sidebar').classList.toggle('collapsed')">
                <i class="fas fa-bars"></i>
            </button>
            <div class="topbar-breadcrumb">
                <span class="bc-home"><i class="fas fa-home"></i></span>
                <span class="bc-sep">/</span>
                <span class="bc-current">${pageTitle!}</span>
            </div>
            <div class="topbar-right">
                <a class="topbar-user" href="/admin/profile" title="个人资料">
                    <div class="user-avatar" id="userAvatar">管</div>
                    <span class="user-name" id="userName">管理员</span>
                </a>
                <button class="btn-logout" onclick="logout()">
                    <i class="fas fa-sign-out-alt"></i>
                </button>
            </div>
        </header>

        <!-- 内容 -->
        <main class="content">
            <#nested>
        </main>
    </div>

    <!-- 全局 JS -->
    <script>
        (function() {
            var token = localStorage.getItem('token');
            var staff = localStorage.getItem('staff') || localStorage.getItem('userInfo');
            if (!token || !staff) {
                window.location.href = '/admin/login';
                return;
            }
            try {
                var d = JSON.parse(staff);
                $('#userName').text(d.nickname || d.name || d.username || '管理员');
                $('#userAvatar').text((d.nickname || d.name || d.username || '管').charAt(0));
            } catch(e) {
                window.location.href = '/admin/login';
            }
        })();
    </script>
</body>
</html>
</#macro>
