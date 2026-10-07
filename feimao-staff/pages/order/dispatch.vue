<template>
  <view class="container">
    <!-- 订单信息 -->
    <view class="order-info card">
      <view class="card-title">订单信息</view>
      <view class="info-row">
        <text class="label">订单编号</text>
        <text class="value">{{ order.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="label">货物名称</text>
        <text class="value">{{ order.goodsName }}</text>
      </view>
      <view class="info-row">
        <text class="label">起点地址</text>
        <text class="value">{{ order.startAddress }}</text>
      </view>
      <view class="info-row">
        <text class="label">终点地址</text>
        <text class="value">{{ order.endAddress }}</text>
      </view>
      <view class="info-row">
        <text class="label">运费</text>
        <text class="value price">¥{{ order.freight }}</text>
      </view>
    </view>

    <!-- 选择司机 -->
    <view class="driver-section">
      <view class="section-title">选择司机</view>
      
      <!-- 筛选 -->
      <view class="filter-bar">
        <input 
          v-model="searchKeyword" 
          placeholder="搜索司机姓名/手机号/车牌号"
          class="search-input"
          @confirm="searchDrivers"
        />
      </view>

      <!-- 司机列表 -->
      <view class="driver-list">
        <view 
          v-for="driver in driverList" 
          :key="driver.id"
          class="driver-card"
          :class="{ selected: selectedDriver && selectedDriver.id === driver.id }"
          @click="selectDriver(driver)"
        >
          <view class="driver-info">
            <view class="driver-avatar">
              <text>{{ driver.name ? driver.name.charAt(0) : '司' }}</text>
            </view>
            <view class="driver-detail">
              <text class="driver-name">{{ driver.name || '未认证' }}</text>
              <text class="driver-mobile">{{ driver.mobile }}</text>
              <text class="driver-mobile">{{ driver.plateNumber || '未填车牌' }} · {{ driver.vehicleType || '未填车型' }}</text>
            </view>
          </view>
          <view class="driver-status">
            <text class="status-tag" :class="getOnlineClass(driver.onlineStatus)">
              {{ getOnlineText(driver.onlineStatus) }}
            </text>
          </view>
        </view>
        
        <view class="empty-state" v-if="driverList.length === 0">
          <text>暂无可用司机</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer">
      <button class="btn-cancel" @click="cancel">取消</button>
      <button class="btn-confirm" @click="confirmDispatch" :disabled="!selectedDriver || submitting">
        {{ submitting ? '派单中...' : '确认派单' }}
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      orderId: null,
      order: {},
      driverList: [],
      selectedDriver: null,
      searchKeyword: '',
      submitting: false
    }
  },
  onLoad(options) {
    this.orderId = options.id
    this.loadOrderDetail()
    this.loadDrivers()
  },
  methods: {
    async loadOrderDetail() {
      try {
        const res = await api.getOrderDetail(this.orderId)
        this.order = res.data
      } catch (e) {
        console.error(e)
      }
    },
    async loadDrivers() {
      try {
        const res = await api.getDrivers({
          page: 1,
          limit: 100,
          onlineStatus: 1,
          keyword: this.searchKeyword.trim()
        })
        this.driverList = res.data.records || []
        if (this.selectedDriver && !this.driverList.some(item => item.id === this.selectedDriver.id)) {
          this.selectedDriver = null
        }
      } catch (e) {
        console.error(e)
      }
    },
    searchDrivers() {
      this.loadDrivers()
    },
    selectDriver(driver) {
      this.selectedDriver = driver
    },
    getOnlineText(status) {
      const map = { 0: '离线', 1: '在线', 2: '运输中' }
      return map[status] || '离线'
    },
    getOnlineClass(status) {
      const map = { 0: 'offline', 1: 'online', 2: 'busy' }
      return map[status] || 'offline'
    },
    cancel() {
      uni.navigateBack()
    },
    async confirmDispatch() {
      if (!this.selectedDriver || this.submitting) {
        uni.showToast({ title: '请选择司机', icon: 'none' })
        return
      }

      this.submitting = true
      uni.showLoading({ title: '派单中...' })
      
      try {
        await api.dispatchOrder({
          orderId: this.orderId,
          driverId: this.selectedDriver.id
        })
        uni.hideLoading()
        uni.showToast({ title: '派单成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: e.msg || '派单失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  padding-bottom: 160rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.label {
  font-size: 26rpx;
  color: #666;
}

.value {
  font-size: 26rpx;
  color: #333;
}

.value.price {
  color: #ff4d4f;
  font-weight: bold;
}

.driver-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.filter-bar {
  margin-bottom: 20rpx;
}

.search-input {
  background: #f5f5f5;
  border-radius: 30rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
}

.driver-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border: 2rpx solid #eee;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}

.driver-card.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.driver-info {
  display: flex;
  align-items: center;
}

.driver-avatar {
  width: 80rpx;
  height: 80rpx;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  margin-right: 20rpx;
}

.driver-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.driver-mobile {
  font-size: 24rpx;
  color: #666;
  margin-top: 4rpx;
}

.status-tag {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
}

.status-tag.online { background: #f6ffed; color: #52c41a; }
.status-tag.busy { background: #fff7e6; color: #fa8c16; }
.status-tag.offline { background: #f5f5f5; color: #999; }

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  gap: 20rpx;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-confirm {
  background: #1890ff;
  color: #fff;
}

.btn-confirm[disabled] {
  opacity: 0.5;
}

.empty-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
}
</style>
