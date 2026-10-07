<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 飞猫后台管理</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.1/css/all.min.css">
    <style>
        :root {
            --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            --card-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            --input-focus: #667eea;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            min-height: 100vh;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        
        .login-container {
            display: flex;
            align-items: center;
            gap: 0;
            max-width: 1000px;
            width: 90%;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: var(--card-shadow);
        }
        
        .login-banner {
            flex: 1;
            background: var(--primary-gradient);
            padding: 60px 50px;
            color: white;
            display: flex;
            flex-direction: column;
            justify-content: center;
            min-height: 500px;
        }
        
        .login-banner h1 {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 20px;
        }
        
        .login-banner p {
            font-size: 1.1rem;
            opacity: 0.9;
            line-height: 1.8;
        }
        
        .login-banner .features {
            margin-top: 30px;
        }
        
        .login-banner .feature-item {
            display: flex;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .login-banner .feature-item i {
            width: 30px;
            height: 30px;
            background: rgba(255,255,255,0.2);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            font-size: 0.9rem;
        }
        
        .login-form-container {
            flex: 1;
            background: white;
            padding: 60px 50px;
            min-height: 500px;
        }
        
        .login-form-container h2 {
            font-size: 1.8rem;
            font-weight: 600;
            color: #1a1a2e;
            margin-bottom: 10px;
        }
        
        .login-form-container .subtitle {
            color: #666;
            margin-bottom: 40px;
        }
        
        .form-group {
            margin-bottom: 25px;
        }
        
        .form-group label {
            font-weight: 500;
            color: #333;
            margin-bottom: 8px;
            display: block;
        }
        
        .input-group {
            position: relative;
        }
        
        .input-group i {
            position: absolute;
            left: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: #999;
            z-index: 10;
        }
        
        .form-control {
            height: 50px;
            padding-left: 45px;
            border: 2px solid #e1e5eb;
            border-radius: 10px;
            font-size: 1rem;
            transition: all 0.3s;
        }
        
        .form-control:focus {
            border-color: var(--input-focus);
            box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
        }
        
        .btn-login {
            width: 100%;
            height: 50px;
            background: var(--primary-gradient);
            border: none;
            border-radius: 10px;
            color: white;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            margin-top: 10px;
        }
        
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
        }
        
        .btn-login:active {
            transform: translateY(0);
        }
        
        .error-message {
            background: #fee;
            color: #c00;
            padding: 12px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
            font-size: 0.9rem;
        }
        
        .error-message.show {
            display: block;
        }
        
        @media (max-width: 768px) {
            .login-banner {
                display: none;
            }
            .login-form-container {
                padding: 40px 30px;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-banner">
            <h1><i class="fas fa-cat mr-3"></i>飞猫配送</h1>
            <p>专业的城市配送管理平台<br>让每一次配送都高效可靠</p>
            <div class="features">
                <div class="feature-item">
                    <i class="fas fa-check"></i>
                    <span>实时订单追踪</span>
                </div>
                <div class="feature-item">
                    <i class="fas fa-check"></i>
                    <span>智能派单调度</span>
                </div>
                <div class="feature-item">
                    <i class="fas fa-check"></i>
                    <span>数据统计分析</span>
                </div>
            </div>
        </div>
        
        <div class="login-form-container">
            <h2>欢迎回来</h2>
            <p class="subtitle">请登录您的管理员账号</p>
            
            <div class="error-message" id="errorMsg">
                <i class="fas fa-exclamation-circle mr-2"></i>
                <span id="errorText"></span>
            </div>
            
            <form id="loginForm">
                <div class="form-group">
                    <label>用户名</label>
                    <div class="input-group">
                        <i class="fas fa-user"></i>
                        <input type="text" class="form-control" id="username" 
                               placeholder="请输入用户名" autocomplete="off">
                    </div>
                </div>
                
                <div class="form-group">
                    <label>密码</label>
                    <div class="input-group">
                        <i class="fas fa-lock"></i>
                        <input type="password" class="form-control" id="password" 
                               placeholder="请输入密码">
                    </div>
                </div>
                
                <button type="submit" class="btn-login">
                    <i class="fas fa-sign-in-alt mr-2"></i>登 录
                </button>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
    <script>
        $('#loginForm').on('submit', function(e) {
            e.preventDefault();
            
            const username = $('#username').val().trim();
            const password = $('#password').val().trim();
            
            if (!username || !password) {
                showError('请输入用户名和密码');
                return;
            }
            
            $('.btn-login').prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-2"></i>登录中...');
            
            $.ajax({
                url: '/api/admin/business/login',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ username: username, password: password }),
                success: function(res) {
                    if (res.code === 1) {
                        localStorage.setItem('token', res.data.token);
                        localStorage.setItem('staff', JSON.stringify(res.data));
                        document.cookie = 'admin_token=' + encodeURIComponent(res.data.token)
                            + '; Path=/; SameSite=Lax';
                        window.location.href = '/admin/';
                    } else {
                        showError(res.msg || '登录失败');
                        $('.btn-login').prop('disabled', false).html('<i class="fas fa-sign-in-alt mr-2"></i>登 录');
                    }
                },
                error: function() {
                    showError('网络错误，请稍后重试');
                    $('.btn-login').prop('disabled', false).html('<i class="fas fa-sign-in-alt mr-2"></i>登 录');
                }
            });
        });
        
        function showError(msg) {
            $('#errorText').text(msg);
            $('#errorMsg').addClass('show');
            setTimeout(() => $('#errorMsg').removeClass('show'), 3000);
        }
    </script>
</body>
</html>
