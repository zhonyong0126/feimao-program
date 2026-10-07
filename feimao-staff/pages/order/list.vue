<template>
  <view class="container">
    <!-- 筛选 -->
    <view class="filter-bar">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="filter-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text>{{ tab.name }}</text>
        <text class="badge" v-if="tab.count > 0">{{ tab.count }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view 
      scroll-y 
      class="order-scroll"
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="order-list">
        <view class="order-card" v-for="order in orderList" :key="order.id" @click="viewDetail(order)">
          <view class="order-header">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="status-tag" :class="'status-' + order.status">
              {{ getStatusText(order.status) }}
            </text>
          </view>
          
          <view class="route-info">
            <view class="route-row">
              <view class="dot start"></view>
              <text class="address">{{ order.startAddress }}</text>
            </view>
            <view class="route-row">
              <view class="dot end"></view>
              <text class="address">{{ order.endAddress }}</text>
            </view>
          </view>

          <view class="order-info">
            <text class="goods">{{ order.goodsName }}</text>
            <text class="freight">¥{{ order.freight }}</text>
          </view>

          <view class="order-footer">
            <text class="time">{{ formatTime(order.createTime) }}</text>
            <view class="actions">
              <button 
                v-if="order.status === 0 || order.status === 1"
                class="btn-dispatch" 
                @click.stop="dispatchOrder(order)"
              >派单</button>
              <button 
                v-if="order.status === 0 || order.status === 1"
                class="btn-cancel" 
                @click.stop="cancelOrder(order)"
              >取消</button>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="!isLoading && orderList.length === 0">
          <text>暂无订单</text>
        </view>
        <view class="loading" v-if="isLoading">
          <text>加载中...</text>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮按钮 -->
    <view class="fab" @click="createOrder">+</view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      tabs: [
        { name: '全部', status: null, count: 0 },
        { name: '待派单', status: 0, count: 0 },
        { name: '待接单', status: 1, count: 0 },
        { name: '已接单', status: 2, count: 0 },
        { name: '运输中', status: 3, count: 0 },
        { name: '已完成', status: 4, count: 0 },
        { name: '已取消', status: 5, count: 0 }
      ],
      currentTab: 0,
      orderList: [],
      page: 1,
      hasMore: true,
      isLoading: false,
      isRefreshing: false
    }
  },
  onLoad(options) {
    if (options.status !== undefined) {
      const idx = this.tabs.findIndex(t => t.status === parseInt(options.status))
      if (idx >= 0) this.currentTab = idx
    }
  },
  onShow() {
    const desiredStatus = uni.getStorageSync('staffOrderStatusFilter')
    if (desiredStatus !== '' && desiredStatus !== null && desiredStatus !== undefined) {
      if (desiredStatus === 'all') {
        this.currentTab = 0
      } else {
        const index = this.tabs.findIndex(tab => tab.status === Number(desiredStatus))
        if (index >= 0) this.currentTab = index
      }
      uni.removeStorageSync('staffOrderStatusFilter')
    }
    this.refreshList()
  },
  methods: {
    async loadOrders() {
      if (this.isLoading || !this.hasMore) return
      this.isLoading = true

      try {
        const status = this.tabs[this.currentTab].status
        const res = await api.getOrders({
          page: this.page,
          limit: 10,
          status: status
        })

        if (this.page === 1) {
          this.orderList = res.data.records || []
        } else {
          this.orderList = [...this.orderList, ...(res.data.records || [])]
        }
        this.hasMore = (res.data.records || []).length >= 10
      } catch (e) {
        console.error(e)
      } finally {
        this.isLoading = false
        this.isRefreshing = false
      }
    },
    switchTab(index) {
      this.currentTab = index
      this.refreshList()
    },
    refreshList() {
      this.page = 1
      this.hasMore = true
      this.loadOrders()
    },
    loadMore() {
      if (this.isLoading || !this.hasMore) return
      this.page++
      this.loadOrders()
    },
    onRefresh() {
      this.isRefreshing = true
      this.refreshList()
    },
    getStatusText(status) {
      const map = ['待派单', '待接单', '已接单', '运输中', '已完成', '已取消']
      return map[status] || '未知'
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    viewDetail(order) {
      uni.navigateTo({ url: `/pages/order/detail?id=${order.id}` })
    },
    dispatchOrder(order) {
      uni.navigateTo({ url: `/pages/order/dispatch?id=${order.id}` })
    },
    async cancelOrder(order) {
      const res = await uni.showModal({
        title: '提示',
        content: '确定要取消此订单吗？'
      })
      if (res.confirm) {
        try {
          await api.cancelOrder(order.id)
          uni.showToast({ title: '已取消', icon: 'success' })
          this.refreshList()
        } catch (e) {
          console.error(e)
        }
      }
    },
    createOrder() {
      uni.navigateTo({ url: '/pages/order/create' })
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 20rpx;
  overflow-x: auto;
}

.filter-item {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  margin-right: 20rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 30rpx;
  background: #f5f5f5;
}

.filter-item.active {
  background: #1890ff;
  color: #fff;
}

.badge {
  margin-left: 8rpx;
  padding: 2rpx 8rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  border-radius: 10rpx;
}

.order-scroll {
  flex: 1;
}

.order-list {
  padding: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
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

.status-tag {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
}

.route-info {
  padding: 16rpx 0;
}

.route-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.dot.start { background: #52c41a; }
.dot.end { background: #ff4d4f; }

.address {
  font-size: 26rpx;
  color: #333;
}

.order-info {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-top: 1rpx solid #f0f0f0;
}

.goods {
  font-size: 26rpx;
  color: #666;
}

.freight {
  font-size: 32rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.actions {
  display: flex;
  gap: 16rpx;
}

.btn-dispatch, .btn-cancel {
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
}

.btn-dispatch {
  background: #1890ff;
  color: #fff;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  background: #1890ff;
  color: #fff;
  border-radius: 50%;
  font-size: 60rpx;
  text-align: center;
  line-height: 90rpx;
  box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.4);
}

.empty-state, .loading {
  text-align: center;
  padding: 60rpx;
  color: #999;
}
</style>
