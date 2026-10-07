<template>
  <div class="order-page">
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="订单状态">
          <el-select v-model="filters.status" clearable placeholder="全部">
            <el-option label="待派单" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="已接单" :value="2" />
            <el-option label="运输中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="订单号/货物名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadOrders">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card>
      <el-table :data="orderList" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="startAddress" label="起点地址" show-overflow-tooltip />
        <el-table-column prop="endAddress" label="终点地址" show-overflow-tooltip />
        <el-table-column prop="goodsName" label="货物名称" width="120" />
        <el-table-column prop="freight" label="运费" width="100">
          <template slot-scope="scope">
            <span class="freight">¥{{ scope.row.freight }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="distance" label="距离" width="80">
          <template slot-scope="scope">{{ scope.row.distance }}km</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 0" 
              type="text" 
              size="small"
              @click="changeStatus(scope.row)"
            >派单</el-button>
            <el-button 
              v-if="scope.row.status < 4" 
              type="text" 
              size="small"
              style="color: #f56c6c"
              @click="cancelOrder(scope.row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
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
  name: 'OrderList',
  data() {
    return {
      filters: {
        status: null,
        keyword: ''
      },
      orderList: [],
      page: 1,
      limit: 10,
      total: 0,
      loading: false
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await api.getOrders({
          page: this.page,
          limit: this.limit,
          ...this.filters
        })
        this.orderList = res.data.records || []
        this.total = res.data.total || 0
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = { status: null, keyword: '' }
      this.page = 1
      this.loadOrders()
    },
    getStatusText(status) {
      const map = ['待派单', '待接单', '已接单', '运输中', '已完成', '已取消']
      return map[status] || '未知'
    },
    getStatusType(status) {
      const map = ['warning', 'info', '', 'primary', 'success', 'danger']
      return map[status] || ''
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    viewDetail(row) {
      this.$router.push(`/order/detail/${row.id}`)
    },
    async changeStatus(row) {
      this.$prompt('请输入司机ID', '派单', {
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入有效的司机ID'
      }).then(async ({ value }) => {
        try {
          await api.updateOrderStatus(row.id, 1)
          this.$message.success('操作成功')
          this.loadOrders()
        } catch (e) {
          console.error(e)
        }
      })
    },
    async cancelOrder(row) {
      this.$confirm('确定要取消此订单吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await api.updateOrderStatus(row.id, 5)
          this.$message.success('订单已取消')
          this.loadOrders()
        } catch (e) {
          console.error(e)
        }
      })
    },
    pageChange(page) {
      this.page = page
      this.loadOrders()
    },
    sizeChange(size) {
      this.limit = size
      this.page = 1
      this.loadOrders()
    }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
}

.freight {
  color: #ff4d4f;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
