<template>
  <view class="container">
    <!-- 头部信息 -->
    <view class="header">
      <view class="user-info">
        <image class="avatar" :src="driver.avatar || '/static/images/default-avatar.png'" mode="aspectFill"></image>
        <view class="info">
          <text class="name">{{ driver.name || '未认证司机' }}</text>
          <text class="status" :class="{'online': driver.onlineStatus === 1}">
            {{ getStatusText(driver.onlineStatus) }}
          </text>
        </view>
      </view>
      <view class="online-btn" @click="toggleOnline">
        <text>{{ driver.onlineStatus === 1 ? '下线' : '上线' }}</text>
      </view>
    </view>

    <!-- 今日数据 -->
    <view class="stats-card">
      <view class="stats-title">今日数据</view>
      <view class="stats-grid">
        <view class="stat-item" @click="goToOrderList(2)">
          <text class="stat-num">{{ stats.pendingOrders || 0 }}</text>
          <text class="stat-label">待接单</text>
        </view>
        <view class="stat-item" @click="goToOrderList(3)">
          <text class="stat-num">{{ stats.inProgress || 0 }}</text>
          <text class="stat-label">运输中</text>
        </view>
        <view class="stat-item" @click="goToOrderList(4)">
          <text class="stat-num">{{ stats.completed || 0 }}</text>
          <text class="stat-label">已完成</text>
        </view>
        <view class="stat-item" @click="goToIncome">
          <text class="stat-num income">{{ stats.todayIncome || '0' }}</text>
          <text class="stat-label">今日收入</text>
        </view>
      </view>
    </view>

    <!-- 快捷功能 -->
    <view class="quick-actions">
      <view class="action-item" @click="goToGrab">
        <image class="action-icon" src="/static/images/icon-grab.png" mode="aspectFit"></image>
        <text class="action-text">抢单大厅</text>
      </view>
      <view class="action-item" @click="goToOrderList(0)">
        <image class="action-icon" src="/static/images/icon-order.png" mode="aspectFit"></image>
        <text class="action-text">我的订单</text>
      </view>
      <view class="action-item" @click="goToIncome">
        <image class="action-icon" src="/static/images/icon-income.png" mode="aspectFit"></image>
        <text class="action-text">我的收入</text>
      </view>
      <view class="action-item" @click="scanQRCode">
        <image class="action-icon" src="/static/images/icon-scan.png" mode="aspectFit"></image>
        <text class="action-text">扫码接单</text>
      </view>
    </view>

    <!-- 最新订单 -->
    <view class="recent-orders">
      <view class="section-header">
        <text class="section-title">最新订单</text>
        <text class="section-more" @click="goToGrab">查看更多</text>
      </view>
      <view class="order-list">
        <view class="order-card" v-for="order in recentOrders" :key="order.id" @click="goToOrderDetail(order.id)">
          <view class="order-header">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-price">¥{{ order.freight }}</text>
          </view>
          <view class="order-route">
            <view class="route-point">
              <view class="point-dot start"></view>
              <text class="point-text">{{ order.startAddress }}</text>
            </view>
            <view class="route-line"></view>
            <view class="route-point">
              <view class="point-dot end"></view>
              <text class="point-text">{{ order.endAddress }}</text>
            </view>
          </view>
          <view class="order-footer">
            <text class="order-goods">{{ order.goodsName }}</text>
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
          </view>
        </view>
        <view class="empty-state" v-if="recentOrders.length === 0">
          <text>暂无可接订单</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      driver: {},
      stats: {},
      recentOrders: []
    }
  },
  onLoad() {
    this.loadDriverInfo()
    this.loadTodayStats()
    this.loadRecentOrders()
  },
  onShow() {
    this.loadTodayStats()
  },
  onPullDownRefresh() {
    this.loadDriverInfo()
    this.loadTodayStats()
    this.loadRecentOrders()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 500)
  },
  methods: {
    async loadDriverInfo() {
      try {
        const res = await api.getDriverInfo()
        this.driver = res.data
        uni.setStorageSync('userInfo', res.data)
      } catch (e) {
        console.error(e)
      }
    },
    async loadTodayStats() {
      try {
        const res = await api.getTodayStats()
        this.stats = res.data
      } catch (e) {
        console.error(e)
      }
    },
    async loadRecentOrders() {
      try {
        const res = await api.getGrabOrders({ page: 1, limit: 5 })
        this.recentOrders = res.data.records || []
      } catch (e) {
        console.error(e)
      }
    },
    getStatusText(status) {
      const map = {
        0: '离线',
        1: '在线空闲',
        2: '运输中'
      }
      return map[status] || '离线'
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    async toggleOnline() {
      if (this.driver.onlineStatus === 2) {
        uni.showToast({ title: '运输中不能下线', icon: 'none' })
        return
      }
      const newStatus = this.driver.onlineStatus === 1 ? 0 : 1
      uni.showLoading({ title: '请稍候' })
      try {
        await api.updateOnlineStatus(newStatus)
        this.driver.onlineStatus = newStatus
        uni.setStorageSync('userInfo', this.driver)
        uni.hideLoading()
        uni.showToast({
          title: newStatus === 1 ? '已上线' : '已下线',
          icon: 'success'
        })
      } catch (e) {
        uni.hideLoading()
      }
    },
    goToGrab() {
      uni.switchTab({ url: '/pages/order/grab' })
    },
    goToOrderList(status) {
      uni.navigateTo({ url: `/pages/order/list?status=${status}` })
    },
    goToOrderDetail(id) {
      uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
    },
    goToIncome() {
      uni.switchTab({ url: '/pages/income/index' })
    },
    scanQRCode() {
      uni.scanCode({
        success: (res) => {
          // 解析二维码内容，跳转到订单详情
          const orderId = res.result
          uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #1890ff, #096dd9);
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.info {
  display: flex;
  flex-direction: column;
}

.name {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.status {
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
  margin-top: 8rpx;
}

.status.online {
  color: #52c41a;
}

.online-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 16rpx 32rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.stats-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.stats-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.stats-grid {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}

.stat-num.income {
  color: #1890ff;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}

.quick-actions {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-around;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 12rpx;
}

.action-text {
  font-size: 24rpx;
  color: #333;
}

.recent-orders {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.section-more {
  font-size: 24rpx;
  color: #1890ff;
}

.order-card {
  border: 1rpx solid #eee;
  border-radius: 8rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 24rpx;
  color: #666;
}

.order-price {
  font-size: 32rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.order-route {
  padding: 16rpx 0;
}

.route-point {
  display: flex;
  align-items: center;
}

.point-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.point-dot.start {
  background: #52c41a;
}

.point-dot.end {
  background: #ff4d4f;
}

.point-text {
  font-size: 26rpx;
  color: #333;
}

.route-line {
  width: 2rpx;
  height: 20rpx;
  background: #ddd;
  margin-left: 7rpx;
  margin-top: 8rpx;
  margin-bottom: 8rpx;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-goods {
  font-size: 24rpx;
  color: #666;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 26rpx;
}
</style>
