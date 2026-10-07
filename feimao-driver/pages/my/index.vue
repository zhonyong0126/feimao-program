<template>
  <view class="page" v-if="loggedIn">
    <view class="profile">
      <image class="avatar" :src="driver.avatar || '/static/images/default-avatar.png'" mode="aspectFill" />
      <view class="profile-text">
        <text class="name">{{ driver.name || '司机用户' }}</text>
        <text class="mobile">{{ driver.mobile || '请完善个人资料' }}</text>
      </view>
      <text class="auth" :class="{ passed: driver.authStatus === 2 }">{{ authText(driver.authStatus) }}</text>
    </view>
    <view class="menu">
      <view class="item" @click="go('/pages/my/auth')"><text>实名认证</text><text>›</text></view>
      <view class="item" @click="go('/pages/my/vehicle')"><text>车辆信息</text><text>›</text></view>
      <view class="item" @click="go('/pages/order/list')"><text>我的订单</text><text>›</text></view>
      <view class="item" @click="go('/pages/income/index')"><text>我的收入</text><text>›</text></view>
    </view>
    <button class="logout" @click="logout">退出登录</button>
  </view>
  <view class="login-page" v-else>
    <view class="login-card">
      <text class="login-title">飞猫司机</text>
      <text class="login-desc">登录后接单并管理运输任务</text>
      <button class="login-button" :disabled="loggingIn" @click="login">
        {{ loggingIn ? '登录中...' : '微信快捷登录' }}
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() { return { driver: {}, loggedIn: false, loggingIn: false } },
  onShow() {
    this.loggedIn = Boolean(uni.getStorageSync('token'))
    if (this.loggedIn) this.load()
  },
  methods: {
    async load() {
      try {
        const res = await api.getDriverInfo()
        this.driver = res.data || {}
        uni.setStorageSync('userInfo', this.driver)
      } catch (e) { console.error(e) }
    },
    authText(status) { return { 0: '未认证', 1: '审核中', 2: '已认证', 3: '未通过' }[status] || '未认证' },
    login() {
      this.loggingIn = true
      uni.login({
        provider: 'weixin',
        success: async result => {
          try {
            const res = await api.driverLogin({ code: result.code })
            uni.setStorageSync('token', res.data.token)
            uni.setStorageSync('userInfo', res.data.driver || {})
            this.driver = res.data.driver || {}
            this.loggedIn = true
          } catch (e) { console.error(e) } finally { this.loggingIn = false }
        },
        fail: () => {
          this.loggingIn = false
          uni.showToast({ title: '无法获取微信登录凭证', icon: 'none' })
        }
      })
    },
    go(url) { uni.navigateTo({ url }) },
    logout() {
      uni.showModal({ title: '提示', content: '确定退出登录吗？', success: res => {
        if (!res.confirm) return
        api.logout().catch(() => {}).finally(() => {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          this.driver = {}
          this.loggedIn = false
        })
      } })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.profile { display: flex; align-items: center; padding: 38rpx 30rpx; border-radius: 18rpx; color: #fff; background: linear-gradient(135deg, #1890ff, #096dd9); }
.avatar { width: 104rpx; height: 104rpx; border: 4rpx solid rgba(255,255,255,.4); border-radius: 50%; }
.profile-text { flex: 1; display: flex; flex-direction: column; margin-left: 24rpx; }
.name { font-size: 34rpx; font-weight: 700; }
.mobile { margin-top: 10rpx; font-size: 24rpx; opacity: .8; }
.auth { padding: 8rpx 14rpx; border-radius: 20rpx; font-size: 22rpx; background: rgba(255,255,255,.2); }
.auth.passed { background: #52c41a; }
.menu { margin-top: 24rpx; overflow: hidden; border-radius: 14rpx; background: #fff; }
.item { display: flex; justify-content: space-between; padding: 31rpx 28rpx; color: #333; font-size: 28rpx; border-bottom: 1rpx solid #eee; }
.logout { margin-top: 40rpx; color: #f5222d; background: #fff; }
.login-page { display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 40rpx; background: #f5f5f5; box-sizing: border-box; }
.login-card { width: 100%; padding: 70rpx 40rpx; border-radius: 22rpx; text-align: center; background: #fff; }
.login-title, .login-desc { display: block; }
.login-title { color: #222; font-size: 48rpx; font-weight: 700; }
.login-desc { margin: 24rpx 0 60rpx; color: #999; font-size: 26rpx; }
.login-button { color: #fff; background: #07c160; }
</style>
