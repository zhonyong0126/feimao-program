<template>
  <view class="page">
    <view class="balance-card">
      <text class="label">可提现余额（元）</text>
      <text class="balance">{{ money(balance) }}</text>
      <button class="withdraw" @click="goWithdraw">申请提现</button>
    </view>

    <view class="section-title">余额明细</view>
    <view v-for="item in records" :key="item.id" class="record">
      <view class="record-main">
        <text class="remark">{{ item.remark || typeText(item.type) }}</text>
        <text class="time">{{ formatTime(item.create_time || item.createTime) }}</text>
      </view>
      <text class="amount" :class="Number(item.amount) >= 0 ? 'income' : 'expense'">
        {{ Number(item.amount) >= 0 ? '+' : '' }}{{ money(item.amount) }}
      </text>
    </view>
    <view v-if="!loading && records.length === 0" class="empty">暂无余额明细</view>
    <view v-if="loading" class="empty">加载中...</view>
    <button v-if="records.length < total" class="more" @click="loadMore">加载更多</button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return { balance: 0, records: [], page: 1, limit: 20, total: 0, loading: false }
  },
  onShow() {
    this.page = 1
    this.records = []
    this.loadIncome()
  },
  onPullDownRefresh() {
    this.page = 1
    this.records = []
    this.loadIncome().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    async loadIncome() {
      if (this.loading) return
      this.loading = true
      try {
        const res = await api.getIncomeList({ page: this.page, limit: this.limit })
        const data = res.data || {}
        this.balance = data.balance || 0
        this.total = data.total || 0
        this.records = this.page === 1
          ? (data.records || [])
          : this.records.concat(data.records || [])
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (this.loading || this.records.length >= this.total) return
      this.page += 1
      this.loadIncome()
    },
    goWithdraw() {
      uni.navigateTo({ url: `/pages/income/withdraw?balance=${this.balance}` })
    },
    typeText(type) {
      return Number(type) === 2 ? '提现' : '订单收入'
    },
    money(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatTime(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').substring(0, 19)
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.balance-card { padding: 42rpx 36rpx; border-radius: 20rpx; color: #fff; background: linear-gradient(135deg, #1890ff, #096dd9); }
.label { display: block; font-size: 26rpx; opacity: .85; }
.balance { display: block; margin: 18rpx 0 30rpx; font-size: 64rpx; font-weight: 700; }
.withdraw { width: 220rpx; margin: 0; color: #096dd9; background: #fff; border: 0; font-size: 28rpx; }
.section-title { padding: 34rpx 4rpx 18rpx; color: #333; font-size: 30rpx; font-weight: 600; }
.record { display: flex; align-items: center; justify-content: space-between; padding: 28rpx; margin-bottom: 2rpx; background: #fff; }
.record-main { display: flex; flex-direction: column; }
.remark { color: #333; font-size: 28rpx; }
.time { margin-top: 10rpx; color: #999; font-size: 22rpx; }
.amount { font-size: 32rpx; font-weight: 600; }
.income { color: #19a974; }
.expense { color: #f5222d; }
.empty { padding: 80rpx 0; color: #999; text-align: center; }
.more { margin: 24rpx auto; color: #666; background: #fff; font-size: 26rpx; }
</style>
