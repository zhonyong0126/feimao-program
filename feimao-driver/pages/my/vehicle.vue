<template>
  <view class="page">
    <view class="form">
      <view class="field"><text>车牌号</text><input v-model="form.plateNumber" placeholder="如：京A12345" /></view>
      <view class="field"><text>车辆类型</text><input v-model="form.vehicleType" placeholder="如：厢式货车" /></view>
      <view class="field"><text>行驶证号</text><input v-model="form.vehicleLicense" placeholder="请输入行驶证号" /></view>
    </view>
    <button class="submit" :disabled="submitting" @click="submit">{{ submitting ? '保存中...' : '保存车辆信息' }}</button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'
export default {
  data() { return { form: { plateNumber: '', vehicleType: '', vehicleLicense: '' }, submitting: false } },
  async onLoad() {
    try { const res = await api.getDriverInfo(); Object.keys(this.form).forEach(k => { this.form[k] = res.data[k] || '' }) }
    catch (e) { console.error(e) }
  },
  methods: {
    async submit() {
      if (Object.values(this.form).some(value => !String(value || '').trim())) {
        uni.showToast({ title: '请完整填写车辆资料', icon: 'none' }); return
      }
      this.submitting = true
      try {
        await api.updateVehicle(this.form)
        uni.showToast({ title: '已提交审核', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1000)
      } catch (e) { console.error(e) } finally { this.submitting = false }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.form { padding: 0 28rpx; border-radius: 14rpx; background: #fff; }
.field { display: flex; align-items: center; padding: 30rpx 0; border-bottom: 1rpx solid #eee; }
.field text { width: 170rpx; color: #333; font-size: 27rpx; }
.field input { flex: 1; text-align: right; font-size: 27rpx; }
.submit { margin-top: 40rpx; color: #fff; background: #1890ff; }
</style>
