<template>
  <view class="page">
    <scroll-view scroll-x class="tabs">
      <view v-for="item in tabs" :key="item.value" class="tab"
        :class="{ active: status === item.value }" @click="changeStatus(item.value)">
        {{ item.label }}
      </view>
    </scroll-view>
    <view v-for="order in orders" :key="order.id" class="card" @click="open(order.id)">
      <view class="head">
        <text>{{ order.orderNo }}</text>
        <text class="status">{{ statusText(order.status) }}</text>
      </view>
      <view class="route"><text class="dot start"></text><text>{{ order.startAddress }}</text></view>
      <view class="line"></view>
      <view class="route"><text class="dot end"></text><text>{{ order.endAddress }}</text></view>
      <view class="foot">
        <text>{{ order.goodsName || '货物运输' }}</text>
        <text class="price">¥{{ money(order.actualFreight || order.freight) }}</text>
      </view>
    </view>
    <view v-if="!loading && orders.length === 0" class="empty">暂无订单</view>
    <button v-if="orders.length < total" class="more" @click="loadMore">加载更多</button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      status: null, page: 1, limit: 10, total: 0, orders: [], loading: false,
      tabs: [
        { label: '全部', value: null }, { label: '已接单', value: 2 },
        { label: '运输中', value: 3 }, { label: '已完成', value: 4 }
      ]
    }
  },
  onLoad(options) {
    if (options.status !== undefined && options.status !== '') this.status = Number(options.status)
  },
  onShow() { this.refresh() },
  onPullDownRefresh() { this.refresh().finally(() => uni.stopPullDownRefresh()) },
  methods: {
    async refresh() { this.page = 1; this.orders = []; return this.load() },
    async load() {
      if (this.loading) return
      this.loading = true
      try {
        const params = { page: this.page, limit: this.limit }
        if (this.status !== null) params.status = this.status
        const res = await api.getMyOrders(params)
        const data = res.data || {}
        this.total = data.total || 0
        this.orders = this.page === 1 ? (data.records || []) : this.orders.concat(data.records || [])
      } catch (e) { console.error(e) } finally { this.loading = false }
    },
    changeStatus(value) { if (this.status !== value) { this.status = value; this.refresh() } },
    loadMore() { if (!this.loading && this.orders.length < this.total) { this.page += 1; this.load() } },
    open(id) { uni.navigateTo({ url: `/pages/order/detail?id=${id}` }) },
    statusText(value) { return { 1: '待接单', 2: '已接单', 3: '运输中', 4: '已完成', 5: '已取消' }[value] || '待派单' },
    money(value) { const n = Number(value || 0); return Number.isFinite(n) ? n.toFixed(2) : '0.00' }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 110rpx 20rpx 30rpx; background: #f5f5f5; box-sizing: border-box; }
.tabs { position: fixed; top: 0; left: 0; right: 0; z-index: 10; height: 90rpx; white-space: nowrap; background: #fff; }
.tab { display: inline-flex; align-items: center; justify-content: center; width: 25%; height: 90rpx; color: #666; font-size: 27rpx; }
.tab.active { color: #1890ff; border-bottom: 4rpx solid #1890ff; box-sizing: border-box; }
.card { padding: 26rpx; margin-bottom: 18rpx; border-radius: 14rpx; background: #fff; }
.head, .foot { display: flex; justify-content: space-between; color: #555; font-size: 25rpx; }
.status, .price { color: #1890ff; font-weight: 600; }
.route { display: flex; align-items: center; margin: 24rpx 0; color: #333; font-size: 27rpx; }
.dot { width: 16rpx; height: 16rpx; margin-right: 18rpx; border-radius: 50%; background: #52c41a; }
.dot.end { background: #ff4d4f; }
.line { height: 28rpx; margin: -20rpx 0 -20rpx 7rpx; border-left: 2rpx dashed #ddd; }
.foot { padding-top: 20rpx; border-top: 1rpx solid #eee; }
.empty { padding: 100rpx 0; color: #999; text-align: center; }
.more { color: #666; background: #fff; font-size: 25rpx; }
</style>
