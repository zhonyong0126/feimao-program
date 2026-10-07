<template>
  <div>
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="客户名称/联系人" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadCustomers">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="customerList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="客户名称" />
        <el-table-column prop="contact" label="联系人" width="120" />
        <el-table-column prop="mobile" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, prev, pager, next"
        :current-page="page"
        :page-size="limit"
        :total="total"
        @current-change="pageChange"
      />
    </el-card>
  </div>
</template>

<script>
import { api } from '@/api'

export default {
  data() {
    return {
      keyword: '',
      customerList: [],
      page: 1,
      limit: 10,
      total: 0,
      loading: false
    }
  },
  created() {
    this.loadCustomers()
  },
  methods: {
    async loadCustomers() {
      this.loading = true
      try {
        const res = await api.getCustomers({
          page: this.page,
          limit: this.limit,
          keyword: this.keyword
        })
        this.customerList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    pageChange(page) {
      this.page = page
      this.loadCustomers()
    }
  }
}
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.pagination { margin-top: 20px; text-align: right; }
</style>
