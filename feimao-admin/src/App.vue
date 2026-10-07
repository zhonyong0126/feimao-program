<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span>🐱</span>
        <text>飞猫后台管理</text>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home"></i>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <i class="el-icon-s-order"></i>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/driver">
          <i class="el-icon-truck"></i>
          <span>司机管理</span>
        </el-menu-item>
        <el-menu-item index="/staff">
          <i class="el-icon-user"></i>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/customer">
          <i class="el-icon-office-building"></i>
          <span>客户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <i class="el-icon-user-solid"></i>
              {{ username }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      username: '管理员'
    }
  },
  created() {
    const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
    this.username = user.name || '管理员'
  },
  methods: {
    handleCommand(cmd) {
      if (cmd === 'logout') {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.layout {
  height: 100vh;
}

.sidebar {
  background: #001529;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #002140;
}

.logo span {
  font-size: 24px;
}

.el-menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
