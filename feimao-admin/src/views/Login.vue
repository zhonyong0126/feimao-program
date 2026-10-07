<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <span class="logo">🐱</span>
        <h2>飞猫后台管理</h2>
      </div>
      
      <el-form ref="form" :model="form" :rules="rules">
        <el-form-item prop="mobile">
          <el-input 
            v-model="form.mobile" 
            prefix-icon="el-icon-user" 
            placeholder="请输入手机号"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password"
            prefix-icon="el-icon-lock" 
            placeholder="请输入密码"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { api } from '@/api'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        mobile: '',
        password: ''
      },
      rules: {
        mobile: [
          { required: true, message: '请输入手机号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        
        this.loading = true
        try {
          const res = await api.login(this.form)
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          this.$router.push('/')
        } catch (e) {
          console.error(e)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
}

.login-header h2 {
  margin-top: 10px;
  color: #333;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
</style>
