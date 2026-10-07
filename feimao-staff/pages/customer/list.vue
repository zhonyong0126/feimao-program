<template>
  <view class="page">
    <view class="toolbar">
      <input v-model="keyword" class="search" placeholder="搜索客户、电话或联系人" @confirm="refresh" />
      <button class="add" @click="openCreate">新增</button>
    </view>

    <view v-if="editing" class="editor">
      <text class="editor-title">{{ form.id ? '编辑客户' : '新增客户' }}</text>
      <input v-model="form.name" maxlength="100" placeholder="客户或公司名称（必填）" />
      <input v-model="form.contact" maxlength="50" placeholder="联系人" />
      <input v-model="form.mobile" maxlength="20" placeholder="联系电话" />
      <input v-model="form.address" maxlength="255" placeholder="联系地址" />
      <view class="editor-actions">
        <button @click="editing = false">取消</button>
        <button class="primary" :disabled="saving" @click="save">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </view>
    </view>

    <view v-for="item in list" :key="item.id" class="card" @click="openEdit(item)">
      <view class="card-title"><text class="name">{{ item.name || '未命名客户' }}</text><text class="edit">编辑 ›</text></view>
      <text>联系人：{{ item.contact || '-' }}</text>
      <text>电话：{{ item.mobile || '-' }}</text>
      <text>地址：{{ item.address || '-' }}</text>
    </view>
    <view v-if="!loading && !list.length" class="empty">暂无客户</view>
    <button v-if="list.length < total" :disabled="loading" @click="more">
      {{ loading ? '加载中...' : '加载更多' }}
    </button>
  </view>
</template>

<script>
import { api } from '@/api/index.js'

export default {
  data() {
    return {
      keyword: '',
      list: [],
      page: 1,
      total: 0,
      loading: false,
      saving: false,
      editing: false,
      form: { id: null, name: '', contact: '', mobile: '', address: '' }
    }
  },
  onShow() {
    this.refresh()
  },
  methods: {
    refresh() {
      this.page = 1
      this.list = []
      this.load()
    },
    async load() {
      if (this.loading) return
      this.loading = true
      try {
        const result = await api.getCustomers({ page: this.page, limit: 20, keyword: this.keyword })
        this.total = result.data.total || 0
        this.list = this.list.concat(result.data.records || [])
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    more() {
      this.page++
      this.load()
    },
    openCreate() {
      this.form = { id: null, name: '', contact: '', mobile: '', address: '' }
      this.editing = true
    },
    openEdit(customer) {
      this.form = {
        id: customer.id,
        name: customer.name || '',
        contact: customer.contact || '',
        mobile: customer.mobile || '',
        address: customer.address || ''
      }
      this.editing = true
    },
    async save() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: '请输入客户名称', icon: 'none' })
        return
      }
      this.saving = true
      try {
        if (this.form.id) await api.updateCustomer(this.form)
        else await api.createCustomer(this.form)
        uni.showToast({ title: '保存成功', icon: 'success' })
        this.editing = false
        this.refresh()
      } catch (error) {
        console.error(error)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 20rpx; background: #f5f5f5; box-sizing: border-box; }
.toolbar { display: flex; gap: 16rpx; margin-bottom: 20rpx; }
.search { flex: 1; padding: 22rpx 26rpx; background: #fff; border-radius: 12rpx; }
.add { width: 150rpx; color: #fff; background: #1890ff; }
.editor { padding: 28rpx; margin-bottom: 22rpx; background: #fff; border-radius: 14rpx; }
.editor-title { display: block; margin-bottom: 20rpx; font-size: 30rpx; font-weight: 600; }
.editor input { padding: 22rpx; margin-top: 14rpx; background: #f7f7f7; border-radius: 10rpx; }
.editor-actions { display: flex; justify-content: flex-end; gap: 18rpx; margin-top: 24rpx; }
.editor-actions button { width: 180rpx; }
.editor-actions .primary { color: #fff; background: #1890ff; }
.card { display: flex; flex-direction: column; gap: 12rpx; padding: 26rpx; margin-bottom: 16rpx; color: #666; font-size: 25rpx; background: #fff; border-radius: 12rpx; }
.card-title { display: flex; justify-content: space-between; align-items: center; }
.name { color: #222; font-size: 30rpx; font-weight: 600; }
.edit { color: #1890ff; }
.empty { padding: 100rpx; text-align: center; color: #999; }
</style>
