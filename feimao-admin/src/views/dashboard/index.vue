<template>
  <div class="dashboard">
    <!-- 数据卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orders"><i class="el-icon-s-order"></i></div>
          <div class="stat-content">
            <div class="stat-num">{{ dashboard.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon pending"><i class="el-icon-time"></i></div>
          <div class="stat-content">
            <div class="stat-num">{{ dashboard.pendingOrders || 0 }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon drivers"><i class="el-icon-truck"></i></div>
          <div class="stat-content">
            <div class="stat-num">{{ dashboard.totalDrivers || 0 }}</div>
            <div class="stat-label">总司机数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon staffs"><i class="el-icon-user"></i></div>
          <div class="stat-content">
            <div class="stat-num">{{ dashboard.totalStaff || 0 }}</div>
            <div class="stat-label">总员工数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions">
      <div slot="header">
        <span>快捷操作</span>
      </div>
      <el-row>
        <el-col :span="6">
          <el-button type="primary" icon="el-icon-plus" @click="$router.push('/order')">新建订单</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" icon="el-icon-user" @click="$router.push('/driver')">司机管理</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" icon="el-icon-document" @click="$router.push('/order')">订单管理</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" icon="el-icon-office-building" @click="$router.push('/customer')">客户管理</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近订单 -->
    <el-card>
      <div slot="header">
        <span>最近订单</span>
        <el-button type="text" @click="$router.push('/order')">查看全部</el-button>
      </div>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="startAddress" label="起点" show-overflow-tooltip />
        <el-table-column prop="endAddress" label="终点" show-overflow-tooltip />
        <el-table-column prop="freight" label="运费" width="100">
          <template slot-scope="scope">¥{{ scope.row.freight }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { api } from '@/api'

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {},
      recentOrders: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const res = await api.getDashboard()
        this.dashboard = res.data
        
        // 加载最近订单
        const orderRes = await api.getOrders({ page: 1, limit: 5 })
        this.recentOrders = orderRes.data.records || []
      } catch (e) {
        console.error(e)
      }
    },
    getStatusText(status) {
      const map = ['待派单', '待接单', '已接单', '运输中', '已完成', '已取消']
      return map[status] || '未知'
    },
    getStatusType(status) {
      const map = ['warning', 'info', '', 'primary', 'success', 'danger']
      return map[status] || ''
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 16px;
}

.stat-icon.orders { background: #1890ff; }
.stat-icon.pending { background: #fa8c16; }
.stat-icon.drivers { background: #52c41a; }
.stat-icon.staffs { background: #722ed1; }

.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.quick-actions {
  margin: 20px 0;
}
</style>
