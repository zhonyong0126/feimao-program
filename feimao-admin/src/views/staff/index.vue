<template>
  <div>
    <el-card class="filter-card">
      <el-button type="primary" icon="el-icon-plus" @click="showAddDialog">添加员工</el-button>
    </el-card>

    <el-card>
      <el-table :data="staffList" v-loading="loading">
        <el-table-column prop="staffNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 2 ? 'danger' : 'info'" size="small">
              {{ scope.row.role === 2 ? '管理员' : '调度员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="editStaff(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="deleteStaff(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="staffNo">
          <el-input v-model="form.staffNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option label="调度员" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { api } from '@/api'

export default {
  data() {
    return {
      staffList: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '添加员工',
      form: { staffNo: '', name: '', mobile: '', password: '', role: 1 },
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadStaffs()
  },
  methods: {
    async loadStaffs() {
      this.loading = true
      try {
        const res = await api.getStaffs({ page: 1, limit: 100 })
        this.staffList = res.data.records || []
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = '添加员工'
      this.form = { staffNo: '', name: '', mobile: '', password: '', role: 1 }
      this.dialogVisible = true
    },
    editStaff(row) {
      this.dialogTitle = '编辑员工'
      this.form = { ...row, password: '' }
      this.dialogVisible = true
    },
    async deleteStaff(row) {
      this.$confirm('确定要删除该员工吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await api.deleteStaff(row.id)
          this.$message.success('已删除')
          this.loadStaffs()
        } catch (e) {
          console.error(e)
        }
      })
    },
    submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        try {
          if (this.form.id) {
            await api.updateStaff(this.form)
          } else {
            await api.addStaff(this.form)
          }
          this.$message.success('操作成功')
          this.dialogVisible = false
          this.loadStaffs()
        } catch (e) {
          console.error(e)
        }
      })
    }
  }
}
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
</style>
