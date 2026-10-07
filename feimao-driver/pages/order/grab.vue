<template>
  <view class="container">
    <!-- 筛选条件 -->
    <view class="filter-bar">
      <picker mode="selector" :range="areaList" @change="onAreaChange">
        <view class="filter-item">
          <text>{{ currentArea || '区域' }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
      <picker mode="selector" :range="distanceList" @change="onDistanceChange">
        <view class="filter-item">
          <text>{{ currentDistance || '距离' }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
      <view class="filter-item" @click="refreshList">
        <text>刷新</text>
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
        <view class="order-card" v-for="order in orderList" :key="order.id" @click="showOrderDetail(order)">
          <!-- 订单头部 -->
          <view class="order-header">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-price">¥{{ order.freight }}</text>
          </view>
          
          <!-- 路线信息 -->
          <view class="route-info">
            <view class="route-row">
              <view class="dot start"></view>
              <view class="route-content">
                <text class="address">{{ order.startAddress }}</text>
                <text class="distance" v-if="order.distance">{{ order.distance }}公里</text>
              </view>
            </view>
            <view class="route-line"></view>
            <view class="route-row">
              <view class="dot end"></view>
              <view class="route-content">
                <text class="address">{{ order.endAddress }}</text>
              </view>
            </view>
          </view>

          <!-- 货物信息 -->
          <view class="goods-info">
            <text class="goods-name">{{ order.goodsName }}</text>
            <text class="goods-weight" v-if="order.goodsWeight">{{ order.goodsWeight }}吨</text>
            <text class="goods-volume" v-if="order.goodsVolume">{{ order.goodsVolume }}方</text>
          </view>

          <!-- 订单底部 -->
          <view class="order-footer">
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
            <view class="grab-btn" @click.stop="grabOrder(order)">
              <text>立即抢单</text>
            </view>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="loading-state" v-if="isLoading">
          <text>加载中...</text>
        </view>
        <view class="empty-state" v-if="!isLoading && orderList.length === 0">
          <image src="/static/images/empty-order.png" mode="aspectFit"></image>
          <text>暂无可接订单</text>
        </view>
        <view class="no-more" v-if="!hasMore && orderList.length > 0">
          <text>没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 订单详情弹窗 -->
    <view class="modal" v-if="showModal" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">订单详情</text>
          <text class="close-btn" @click="hideModal">×</text>
        </view>
        <view class="modal-body" v-if="selectedOrder">
          <view class="detail-row">
            <text class="label">订单编号</text>
            <text class="value">{{ selectedOrder.orderNo }}</text>
          </view>
          <view class="detail-row">
            <text class="label">运费</text>
            <text class="value price">¥{{ selectedOrder.freight }}</text>
          </view>
          <view class="detail-row">
            <text class="label">距离</text>
            <text class="value">{{ selectedOrder.distance }}公里</text>
          </view>
          <view class="detail-row">
            <text class="label">货物名称</text>
            <text class="value">{{ selectedOrder.goodsName }}</text>
          </view>
          <view class="detail-row" v-if="selectedOrder.goodsWeight">
            <text class="label">货物重量</text>
            <text class="value">{{ selectedOrder.goodsWeight }}吨</text>
          </view>
          <view class="detail-row" v-if="selectedOrder.goodsVolume">
            <text class="label">货物体积</text>
            <text class="value">{{ selectedOrder.goodsVolume }}方</text>
          </view>
          <view class="detail-row">
            <text class="label">起点地址</text>
            <text class="value">{{ selectedOrder.startAddress }}</text>
          </view>
          <view class="detail-row">
            <text class="label">终点地址</text>
            <text class="value">{{ selectedOrder.endAddress }}</text>
          </view>
          <view class="detail-row" v-if="selectedOrder.remark">
            <text class="label">备注</text>
            <text class="value">{{ selectedOrder.remark }}</text>
          </view>
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="hideModal">取消</button>
          <button class="confirm-btn" @click="confirmGrab">确认抢单</button>
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
      orderList: [],
      page: 1,
      limit: 10,
      hasMore: true,
      isLoading: false,
      isRefreshing: false,
      showModal: false,
      selectedOrder: null,
      areaList: ['全部', '朝阳区', '海淀区', '东城区', '西城区'],
      distanceList: ['全部', '5公里内', '10公里内', '20公里内', '50公里内'],
      currentArea: '全部',
      currentDistance: '全部'
    }
  },
  onLoad() {
    this.loadOrders()
  },
  onShow() {
    this.refreshList()
  },
  methods: {
    async loadOrders() {
      if (this.isLoading) return
      this.isLoading = true
      
      try {
        const res = await api.getGrabOrders({
          page: this.page,
          limit: this.limit
        })
        
        if (this.page === 1) {
          this.orderList = res.data.records || []
        } else {
          this.orderList = [...this.orderList, ...(res.data.records || [])]
        }
        
        this.hasMore = (res.data.records || []).length >= this.limit
      } catch (e) {
        console.error(e)
      } finally {
        this.isLoading = false
        this.isRefreshing = false
      }
    },
    loadMore() {
      if (!this.hasMore || this.isLoading) return
      this.page++
      this.loadOrders()
    },
    onRefresh() {
      this.isRefreshing = true
      this.refreshList()
    },
    refreshList() {
      this.page = 1
      this.hasMore = true
      this.loadOrders()
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return `${date.getMonth() + 1}-${date.getDate()}`
    },
    showOrderDetail(order) {
      this.selectedOrder = order
      this.showModal = true
    },
    hideModal() {
      this.showModal = false
      this.selectedOrder = null
    },
    grabOrder(order) {
      this.selectedOrder = order
      this.showModal = true
    },
    async confirmGrab() {
      if (!this.selectedOrder) return
      
      uni.showLoading({ title: '抢单中...' })
      
      try {
        await api.grabOrder(this.selectedOrder.id)
        uni.hideLoading()
        uni.showToast({
          title: '抢单成功',
          icon: 'success'
        })
        this.hideModal()
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/order/list?status=2' })
        }, 1500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({
          title: e.msg || '抢单失败',
          icon: 'none'
        })
      }
    },
    onAreaChange(e) {
      this.currentArea = this.areaList[e.detail.value]
      this.refreshList()
    },
    onDistanceChange(e) {
      this.currentDistance = this.distanceList[e.detail.value]
      this.refreshList()
    }
  }
}
</script>

<style scoped>
.container {
  background: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 20rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  color: #333;
}

.filter-item .arrow {
  margin-left: 8rpx;
  font-size: 20rpx;
  color: #999;
}

.order-scroll {
  flex: 1;
  height: calc(100vh - 100rpx);
}

.order-list {
  padding: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.order-no {
  font-size: 24rpx;
  color: #666;
}

.order-price {
  font-size: 36rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.route-info {
  padding: 16rpx 0;
}

.route-row {
  display: flex;
  align-items: flex-start;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  margin-top: 8rpx;
  flex-shrink: 0;
}

.dot.start {
  background: #52c41a;
}

.dot.end {
  background: #ff4d4f;
}

.route-content {
  flex: 1;
}

.address {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.distance {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.route-line {
  width: 2rpx;
  height: 24rpx;
  background: #e8e8e8;
  margin-left: 7rpx;
  margin: 8rpx 0 8rpx 7rpx;
}

.goods-info {
  padding: 16rpx 0;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  gap: 20rpx;
}

.goods-name {
  font-size: 26rpx;
  color: #333;
}

.goods-weight, .goods-volume {
  font-size: 24rpx;
  color: #666;
  padding: 4rpx 12rpx;
  background: #f5f5f5;
  border-radius: 4rpx;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.grab-btn {
  background: #1890ff;
  color: #fff;
  padding: 12rpx 32rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.loading-state, .empty-state, .no-more {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 26rpx;
}

.empty-state image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 20rpx;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 30rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-row {
  display: flex;
  margin-bottom: 20rpx;
}

.detail-row .label {
  width: 160rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.detail-row .value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.detail-row .price {
  color: #ff4d4f;
  font-weight: bold;
}

.modal-footer {
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: #1890ff;
  color: #fff;
}
</style>
