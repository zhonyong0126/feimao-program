<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text>客户</text>
        <picker :range="customers" range-key="name" @change="selectCustomer">
          <view class="picker-value">{{ selectedCustomerName }}</view>
        </picker>
      </view>
      <view class="field"><text>起点</text><input v-model="form.startAddress" maxlength="255" placeholder="请输入装货地址" /></view>
      <view class="field"><text>终点</text><input v-model="form.endAddress" maxlength="255" placeholder="请输入卸货地址" /></view>
      <view class="field"><text>货物</text><input v-model="form.goodsName" maxlength="100" placeholder="请输入货物名称" /></view>
      <view class="field"><text>重量(吨)</text><input v-model="form.goodsWeight" type="digit" placeholder="选填" /></view>
      <view class="field"><text>体积(方)</text><input v-model="form.goodsVolume" type="digit" placeholder="选填" /></view>
      <view class="field"><text>距离(公里)</text><input v-model="form.distance" type="digit" placeholder="选填" /></view>
      <view class="field"><text>运费(元)</text><input v-model="form.freight" type="digit" placeholder="选填" /></view>
      <view class="field"><text>备注</text><input v-model="form.remark" maxlength="500" placeholder="选填" /></view>
    </view>
    <button class="submit" :disabled="submitting" @click="submit">
      {{ submitting ? '创建中...' : '创建订单' }}
    </button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      customers: [],
      selectedCustomerName: '请选择客户（选填）',
      submitting: false,
      form: {
        customerId: null,
        startAddress: '',
        endAddress: '',
        goodsName: '',
        goodsWeight: '',
        goodsVolume: '',
        distance: '',
        freight: '',
        remark: ''
      }
    }
  },
  onLoad() {
    this.loadCustomers()
  },
  methods: {
    async loadCustomers() {
      try {
        const result = await api.getCustomers({ page: 1, limit: 100 })
        this.customers = (result.data && result.data.records) || []
      } catch (error) {
        console.error(error)
      }
    },
    selectCustomer(event) {
      const customer = this.customers[Number(event.detail.value)]
      if (!customer) return
      this.form.customerId = customer.id
      this.selectedCustomerName = customer.name || customer.contact || `客户${customer.id}`
    },
    validDecimal(value) {
      return value === ''
        || (/^\d+(\.\d{1,2})?$/.test(String(value)) && Number(value) <= 99999999.99)
    },
    async submit() {
      if (this.submitting) return
      if (!this.form.startAddress.trim()
        || !this.form.endAddress.trim()
        || !this.form.goodsName.trim()) {
        uni.showToast({ title: '请完整填写必填项', icon: 'none' })
        return
      }
      const decimals = ['goodsWeight', 'goodsVolume', 'distance', 'freight']
      if (decimals.some(key => !this.validDecimal(this.form[key]))) {
        uni.showToast({ title: '数字须为非负且最多两位小数', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const result = await api.createOrder(this.form)
        uni.showToast({ title: '创建成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/order/detail?id=${result.data.id}` })
        }, 1000)
      } catch (error) {
        console.error(error)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f5f5f5; box-sizing: border-box; }
.form { padding: 0 28rpx; background: #fff; border-radius: 14rpx; }
.field { display: flex; align-items: center; padding: 28rpx 0; border-bottom: 1rpx solid #eee; }
.field text { width: 190rpx; font-size: 27rpx; }
.field input, .field picker { flex: 1; text-align: right; }
.picker-value { color: #666; font-size: 27rpx; }
.submit { margin-top: 36rpx; color: #fff; background: #1890ff; }
</style>
