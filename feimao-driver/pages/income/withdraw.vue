<template>
  <view class="page">
    <view class="card">
      <text class="title">提现金额</text>
      <view class="amount-row">
        <text class="currency">¥</text>
        <input v-model="amount" class="amount-input" type="digit" placeholder="请输入提现金额" />
      </view>
      <view class="balance-row">
        <text>可提现余额 ¥{{ money(balance) }}</text>
        <text class="all" @click="withdrawAll">全部提现</text>
      </view>
    </view>
    <text class="tip">提交后进入待审核状态，处理结果请以后台审核为准。</text>
    <button class="submit" :disabled="submitting" @click="submit">
      {{ submitting ? '提交中...' : '确认提现' }}
    </button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return { balance: 0, amount: '', submitting: false }
  },
  onLoad(options) {
    this.balance = Number(options.balance || 0)
  },
  methods: {
    withdrawAll() {
      this.amount = this.money(this.balance)
    },
    async submit() {
      const amount = Number(this.amount)
      if (!Number.isFinite(amount) || amount <= 0) {
        uni.showToast({ title: '请输入正确的提现金额', icon: 'none' })
        return
      }
      if (amount > this.balance) {
        uni.showToast({ title: '提现金额不能超过余额', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        await api.withdraw({ amount: amount.toFixed(2) })
        uni.showToast({ title: '提现申请已提交', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1200)
      } catch (e) {
        console.error(e)
      } finally {
        this.submitting = false
      }
    },
    money(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 30rpx 24rpx; background: #f5f5f5; box-sizing: border-box; }
.card { padding: 36rpx 30rpx; border-radius: 16rpx; background: #fff; }
.title { color: #333; font-size: 30rpx; font-weight: 600; }
.amount-row { display: flex; align-items: center; padding: 36rpx 0 24rpx; border-bottom: 1rpx solid #eee; }
.currency { margin-right: 20rpx; color: #222; font-size: 52rpx; }
.amount-input { flex: 1; height: 80rpx; font-size: 52rpx; }
.balance-row { display: flex; justify-content: space-between; padding-top: 24rpx; color: #999; font-size: 25rpx; }
.all { color: #1890ff; }
.tip { display: block; padding: 24rpx 8rpx; color: #999; font-size: 24rpx; line-height: 1.6; }
.submit { margin-top: 20rpx; color: #fff; background: #1890ff; border-radius: 12rpx; }
.submit[disabled] { opacity: .6; }
</style>
