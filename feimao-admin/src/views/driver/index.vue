<template>
  <div class="driver-page">
    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="认证状态">
          <el-select v-model="filters.authStatus" clearable placeholder="全部">
            <el-option label="未认证" :value="0" />
            <el-option label="审核中" :value="1" />
            <el-option label="已认证" :value="2" />
            <el-option label="认证失败" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="禁用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="姓名/手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadDrivers">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 司机列表 -->
    <el-card>
      <el-table :data="driverList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="vehicleType" label="车型" width="100" />
        <el-table-column prop="authStatus" label="认证状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getAuthType(scope.row.authStatus)" size="small">
              {{ getAuthText(scope.row.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="100">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="toggleStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="onlineStatus" label="在线状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOnlineType(scope.row.onlineStatus)" size="small" effect="dark">
              {{ getOnlineText(scope.row.onlineStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100">
          <template slot-scope="scope">¥{{ scope.row.balance || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.authStatus === 1" 
              type="text" 
              size="small"
              @click="authDriver(scope.row, 2)"
            >通过</el-button>
            <el-button 
              v-if="scope.row.authStatus === 1" 
              type="text" 
              size="small"
              style="color: #f56c6c"
              @click="authDriver(scope.row, 3)"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="limit"
        :total="total"
        @current-change="pageChange"
        @size-change="sizeChange"
      />
    </el-card>
  </div>
</template>

<script>
import { api } from '@/api'

export default {
  name: 'DriverList',
  data() {
    return {
      filters: {
        authStatus: null,
        status: null,
        keyword: ''
      },
      driverList: [],
      page: 1,
      limit: 10,
      total: 0,
      loading: false
    }
  },
  created() {
    this.loadDrivers()
  },
  methods: {
    async loadDrivers() {
      this.loading = true
      try {
        const res = await api.getDrivers({
          page: this.page,
          limit: this.limit,
          ...this.filters
        })
        this.driverList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    getAuthText(status) {
      const map = ['未认证', '审核中', '已认证', '认证失败']
      return map[status] || '未知'
    },
    getAuthType(status) {
      const map = ['info', 'warning', 'success', 'danger']
      return map[status] || ''
    },
    getOnlineText(status) {
      const map = ['离线', '在线', '运输中']
      return map[status] || '离线'
    },
    getOnlineType(status) {
      const map = ['info', 'success', 'warning']
      return map[status] || 'info'
    },
    async toggleStatus(row) {
      try {
        await api.updateDriverStatus(row.id, row.status)
        this.$message.success('状态已更新')
      } catch (e) {
        row.status = row.status === 1 ? 0 : 1
      }
    },
    async authDriver(row, authStatus) {
      const msg = authStatus === 2 ? '确定通过审核？' : '确定拒绝认证？'
      this.$confirm(msg, '提示', { type: 'warning' }).then(async () => {
        try {
          await api.authDriver(row.id, authStatus)
          this.$message.success('操作成功')
          this.loadDrivers()
        } catch (e) {
          console.error(e)
        }
      })
    },
    viewDetail(row) {
      console.log('查看详情', row)
    },
    pageChange(page) {
      this.page = page
      this.loadDrivers()
    },
    sizeChange(size) {
      this.limit = size
      this.page = 1
      this.loadDrivers()
    }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
