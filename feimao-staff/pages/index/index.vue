<template>
  <view class="container">
    <!-- 头部 -->
    <view class="header">
      <view class="welcome">
        <text class="greeting">你好，{{ staff.name || '调度员' }}</text>
        <text class="date">{{ todayDate }}</text>
      </view>
    </view>

    <!-- 今日数据 -->
    <view class="stats-card">
      <view class="stats-title">今日数据</view>
      <view class="stats-grid">
        <view class="stat-item" @click="goToOrders(null)">
          <text class="stat-num">{{ stats.totalOrders || 0 }}</text>
          <text class="stat-label">总订单</text>
        </view>
        <view class="stat-item" @click="goToOrders(0)">
          <text class="stat-num warning">{{ stats.pendingOrders || 0 }}</text>
          <text class="stat-label">待派单</text>
        </view>
        <view class="stat-item" @click="goToOrders(3)">
          <text class="stat-num primary">{{ stats.inProgress || 0 }}</text>
          <text class="stat-label">运输中</text>
        </view>
        <view class="stat-item" @click="goToOrders(4)">
          <text class="stat-num success">{{ stats.completed || 0 }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="createOrder">
        <view class="action-icon create">📝</view>
        <text class="action-text">创建订单</text>
      </view>
      <view class="action-card" @click="goToOrders(null)">
        <view class="action-icon order">📋</view>
        <text class="action-text">订单管理</text>
      </view>
      <view class="action-card" @click="viewDrivers">
        <view class="action-icon driver">🚗</view>
        <text class="action-text">司机列表</text>
      </view>
      <view class="action-card" @click="viewCustomers">
        <view class="action-icon customer">👥</view>
        <text class="action-text">客户管理</text>
      </view>
    </view>

    <!-- 待处理订单 -->
    <view class="pending-section">
      <view class="section-header">
        <text class="section-title">待派单订单</text>
        <text class="section-more" @click="goToOrders(0)">查看全部</text>
      </view>
      <view class="order-list">
        <view class="order-card" v-for="order in pendingOrders" :key="order.id" @click="dispatchOrder(order)">
          <view class="order-header">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-freight">¥{{ order.freight }}</text>
          </view>
          <view class="order-route">
            <text class="route-text">{{ order.startAddress }} → {{ order.endAddress }}</text>
          </view>
          <view class="order-footer">
            <text class="order-goods">{{ order.goodsName }}</text>
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
          </view>
        </view>
        <view class="empty-state" v-if="pendingOrders.length === 0">
          <text>暂无待派订单</text>
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
      staff: {},
      stats: {},
      pendingOrders: []
    }
  },
  computed: {
    todayDate() {
      const d = new Date()
      return `${d.getMonth() + 1}月${d.getDate()}日`
    }
  },
  onLoad() {
    this.staff = uni.getStorageSync('userInfo') || {}
  },
  onShow() {
    this.loadStats()
    this.loadPendingOrders()
  },
  onPullDownRefresh() {
    this.loadStats()
    this.loadPendingOrders()
    setTimeout(() => uni.stopPullDownRefresh(), 500)
  },
  methods: {
    async loadStats() {
      try {
        const res = await api.getStats()
        this.stats = res.data
      } catch (e) {
        console.error(e)
      }
    },
    async loadPendingOrders() {
      try {
        const res = await api.getOrders({ page: 1, limit: 5, status: 0 })
        this.pendingOrders = res.data.records || []
      } catch (e) {
        console.error(e)
      }
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    createOrder() {
      uni.navigateTo({ url: '/pages/order/create' })
    },
    goToOrders(status) {
      if (status === null || status === undefined) {
        uni.setStorageSync('staffOrderStatusFilter', 'all')
      } else {
        uni.setStorageSync('staffOrderStatusFilter', status)
      }
      uni.switchTab({ url: '/pages/order/list' })
    },
    viewDrivers() {
      uni.navigateTo({ url: '/pages/driver/list' })
    },
    viewCustomers() {
      uni.navigateTo({ url: '/pages/customer/list' })
    },
    dispatchOrder(order) {
      uni.navigateTo({ url: `/pages/order/dispatch?id=${order.id}` })
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
  color: #fff;
}

.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.greeting {
  font-size: 36rpx;
  font-weight: bold;
}

.date {
  font-size: 26rpx;
  opacity: 0.8;
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

.stat-num.primary { color: #1890ff; }
.stat-num.success { color: #52c41a; }
.stat-num.warning { color: #fa8c16; }

.stat-label {
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.action-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx 20rpx;
  text-align: center;
}

.action-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.action-text {
  font-size: 24rpx;
  color: #333;
}

.pending-section {
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
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.order-no {
  font-size: 24rpx;
  color: #666;
}

.order-freight {
  font-size: 28rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.route-text {
  font-size: 26rpx;
  color: #333;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
  padding-top: 12rpx;
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
  padding: 40rpx;
  color: #999;
}
</style>
