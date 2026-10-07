<template>
  <view v-if="order.id" class="page">
    <view class="state">
      {{ statusText(order.status) }}
      <text>{{ order.orderNo }}</text>
    </view>
    <view class="card">
      <view class="row"><text>起点</text><text>{{ order.startAddress || '-' }}</text></view>
      <view class="row"><text>终点</text><text>{{ order.endAddress || '-' }}</text></view>
      <view class="row"><text>货物</text><text>{{ order.goodsName || '-' }}</text></view>
      <view class="row"><text>重量</text><text>{{ order.goodsWeight || 0 }} 吨</text></view>
      <view class="row"><text>体积</text><text>{{ order.goodsVolume || 0 }} 方</text></view>
      <view class="row"><text>距离</text><text>{{ order.distance || 0 }} 公里</text></view>
      <view class="row"><text>运费</text><text class="price">¥{{ money(order.actualFreight || order.freight) }}</text></view>
      <view class="row"><text>备注</text><text>{{ order.remark || '-' }}</text></view>
    </view>
    <view class="card">
      <view class="row"><text>创建时间</text><text>{{ time(order.createTime) }}</text></view>
      <view class="row"><text>接单时间</text><text>{{ time(order.acceptTime) }}</text></view>
      <view class="row"><text>开始运输</text><text>{{ time(order.startTime) }}</text></view>
      <view class="row"><text>确认到达</text><text>{{ time(order.arriveTime) }}</text></view>
      <view class="row"><text>完成时间</text><text>{{ time(order.completeTime) }}</text></view>
    </view>
    <view v-if="canManage" class="actions">
      <button class="action" @click="dispatch">去派单</button>
      <button class="cancel" :disabled="cancelling" @click="cancelOrder">
        {{ cancelling ? '取消中...' : '取消订单' }}
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return { id: null, order: {}, cancelling: false }
  },
  computed: {
    canManage() {
      return Number(this.order.status) === 0 || Number(this.order.status) === 1
    }
  },
  onLoad(options) {
    this.id = Number(options.id)
  },
  onShow() {
    this.load()
  },
  methods: {
    async load() {
      try {
        const result = await api.getOrderDetail(this.id)
        this.order = result.data || {}
      } catch (error) {
        console.error(error)
      }
    },
    dispatch() {
      uni.navigateTo({ url: `/pages/order/dispatch?id=${this.id}` })
    },
    async cancelOrder() {
      if (this.cancelling) return
      const confirmation = await uni.showModal({ title: '提示', content: '确认取消该订单吗？' })
      if (!confirmation.confirm) return
      this.cancelling = true
      try {
        await api.cancelOrder(this.id)
        uni.showToast({ title: '已取消', icon: 'success' })
        await this.load()
      } catch (error) {
        console.error(error)
      } finally {
        this.cancelling = false
      }
    },
    statusText(status) {
      return ['待派单', '待接单', '已接单', '运输中', '已完成', '已取消'][status] || '未知'
    },
    money(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    time(value) {
      return value ? String(value).replace('T', ' ').substring(0, 19) : '-'
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.state { padding: 36rpx; color: #fff; font-size: 36rpx; font-weight: 700; border-radius: 14rpx; background: #1890ff; }
.state text { display: block; margin-top: 12rpx; font-size: 23rpx; font-weight: 400; }
.card { margin-top: 20rpx; padding: 20rpx 28rpx; background: #fff; border-radius: 14rpx; }
.row { display: flex; justify-content: space-between; padding: 22rpx 0; border-bottom: 1rpx solid #eee; font-size: 26rpx; }
.row text:last-child { max-width: 70%; text-align: right; }
.price { color: #f5222d; font-weight: 600; }
.actions { display: flex; gap: 20rpx; margin-top: 36rpx; }
.actions button { flex: 1; }
.action { color: #fff; background: #1890ff; }
.cancel { color: #f5222d; background: #fff; }
</style>
