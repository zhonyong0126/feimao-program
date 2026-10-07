<template>
  <view class="page">
    <view class="notice">认证资料提交后由后台管理员审核，请确保信息真实有效。</view>
    <view class="form">
      <view class="field"><text>姓名</text><input v-model="form.name" placeholder="请输入真实姓名" /></view>
      <view class="field"><text>手机号</text><input v-model="form.mobile" type="number" maxlength="11" placeholder="请输入手机号" /></view>
      <view class="field"><text>身份证号</text><input v-model="form.idCard" maxlength="18" placeholder="请输入身份证号" /></view>
      <view class="field"><text>驾驶证号</text><input v-model="form.driverLicense" placeholder="请输入驾驶证号" /></view>
    </view>
    <button class="submit" :disabled="submitting" @click="submit">{{ submitting ? '提交中...' : '提交审核' }}</button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'
export default {
  data() { return { form: { name: '', mobile: '', idCard: '', driverLicense: '' }, submitting: false } },
  async onLoad() {
    try { const res = await api.getDriverInfo(); Object.keys(this.form).forEach(k => { this.form[k] = res.data[k] || '' }) }
    catch (e) { console.error(e) }
  },
  methods: {
    async submit() {
      if (Object.values(this.form).some(value => !String(value || '').trim())) {
        uni.showToast({ title: '请完整填写认证资料', icon: 'none' }); return
      }
      this.submitting = true
      try {
        await api.submitDriverAuth(this.form)
        uni.showToast({ title: '资料已提交', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1200)
      } catch (e) { console.error(e) } finally { this.submitting = false }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.notice { padding: 24rpx; color: #ad6800; font-size: 24rpx; line-height: 1.6; border-radius: 12rpx; background: #fffbe6; }
.form { margin-top: 22rpx; padding: 0 28rpx; border-radius: 14rpx; background: #fff; }
.field { display: flex; align-items: center; padding: 28rpx 0; border-bottom: 1rpx solid #eee; }
.field text { width: 160rpx; color: #333; font-size: 27rpx; }
.field input { flex: 1; text-align: right; font-size: 27rpx; }
.submit { margin-top: 40rpx; color: #fff; background: #1890ff; }
</style>
