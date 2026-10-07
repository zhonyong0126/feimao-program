<template>
  <view class="page" v-if="order.id">
    <view class="state-card">
      <text class="state">{{ statusText(order.status) }}</text>
      <text class="number">订单号：{{ order.orderNo }}</text>
    </view>
    <view class="card route-card">
      <view class="route"><text class="dot start"></text><text>{{ order.startAddress }}</text></view>
      <view class="route-line"></view>
      <view class="route"><text class="dot end"></text><text>{{ order.endAddress }}</text></view>
    </view>
    <view class="card">
      <view class="row"><text>货物名称</text><text>{{ order.goodsName || '-' }}</text></view>
      <view class="row"><text>重量</text><text>{{ order.goodsWeight || 0 }} 吨</text></view>
      <view class="row"><text>体积</text><text>{{ order.goodsVolume || 0 }} 方</text></view>
      <view class="row"><text>运输距离</text><text>{{ order.distance || 0 }} 公里</text></view>
      <view class="row"><text>运费</text><text class="price">¥{{ money(order.actualFreight || order.freight) }}</text></view>
      <view class="row" v-if="order.remark"><text>备注</text><text>{{ order.remark }}</text></view>
    </view>
    <view class="card">
      <view class="row"><text>接单时间</text><text>{{ time(order.acceptTime) }}</text></view>
      <view class="row"><text>开始运输</text><text>{{ time(order.startTime) }}</text></view>
      <view class="row"><text>确认到达</text><text>{{ time(order.arriveTime) }}</text></view>
      <view class="row"><text>完成时间</text><text>{{ time(order.completeTime) }}</text></view>
    </view>
    <view class="footer" v-if="actionText">
      <button class="action" :disabled="submitting" @click="act">{{ submitting ? '处理中...' : actionText }}</button>
    </view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() { return { id: null, order: {}, submitting: false } },
  computed: {
    actionText() {
      if (Number(this.order.status) === 2) return '开始运输'
      if (Number(this.order.status) === 3 && !this.order.arriveTime) return '确认到达'
      if (Number(this.order.status) === 3 && this.order.arriveTime) return '完成订单'
      return ''
    }
  },
  onLoad(options) { this.id = Number(options.id); this.load() },
  onShow() { if (this.id) this.load() },
  methods: {
    async load() {
      try { const res = await api.getOrderDetail(this.id); this.order = res.data || {} }
      catch (e) { console.error(e) }
    },
    async act() {
      this.submitting = true
      try {
        if (Number(this.order.status) === 2) await api.startTransport(this.id)
        else if (!this.order.arriveTime) await api.arriveConfirm(this.id)
        else await api.completeOrder(this.id)
        uni.showToast({ title: '操作成功', icon: 'success' })
        await this.load()
      } catch (e) { console.error(e) } finally { this.submitting = false }
    },
    statusText(value) { return { 1: '待接单', 2: '已接单', 3: '运输中', 4: '已完成', 5: '已取消' }[value] || '待派单' },
    money(value) { const n = Number(value || 0); return Number.isFinite(n) ? n.toFixed(2) : '0.00' },
    time(value) { return value ? String(value).replace('T', ' ').substring(0, 19) : '-' }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 20rpx 20rpx 150rpx; background: #f5f5f5; box-sizing: border-box; }
.state-card { padding: 38rpx 30rpx; border-radius: 16rpx; color: #fff; background: linear-gradient(135deg, #1890ff, #096dd9); }
.state, .number { display: block; }
.state { font-size: 38rpx; font-weight: 700; }
.number { margin-top: 14rpx; font-size: 24rpx; opacity: .85; }
.card { padding: 28rpx; margin-top: 20rpx; border-radius: 14rpx; background: #fff; }
.route { display: flex; align-items: center; color: #333; font-size: 28rpx; line-height: 1.5; }
.dot { flex: none; width: 18rpx; height: 18rpx; margin-right: 20rpx; border-radius: 50%; background: #52c41a; }
.dot.end { background: #ff4d4f; }
.route-line { height: 42rpx; margin-left: 8rpx; border-left: 2rpx dashed #ddd; }
.row { display: flex; justify-content: space-between; padding: 17rpx 0; color: #666; font-size: 26rpx; border-bottom: 1rpx solid #f2f2f2; }
.row:last-child { border-bottom: 0; }
.row text:last-child { max-width: 65%; color: #333; text-align: right; }
.row .price { color: #f5222d; font-weight: 600; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; padding: 18rpx 24rpx; background: #fff; }
.action { color: #fff; background: #1890ff; }
</style>
