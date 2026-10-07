import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

const BASE_URL = 'http://localhost:8080/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 10000
})

// 请求拦截
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200 || res.code === 1) {
      return res
    }
    
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error('登录已过期'))
    }
    
    Message.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    Message.error('网络错误')
    return Promise.reject(error)
  }
)

export default request

// API 接口
export const api = {
  // 登录
  login: (data) => request.post('/admin/login', data),
  
  // 首页数据
  getDashboard: () => request.get('/admin/dashboard'),
  
  // 订单
  getOrders: (params) => request.get('/admin/orders', { params }),
  getOrderDetail: (id) => request.get(`/admin/order/${id}`),
  updateOrderStatus: (id, status) => request.post('/admin/order/status', null, { params: { id, status } }),
  
  // 司机
  getDrivers: (params) => request.get('/admin/drivers', { params }),
  getDriverDetail: (id) => request.get(`/admin/driver/${id}`),
  updateDriverStatus: (id, status) => request.post('/admin/driver/status', null, { params: { id, status } }),
  authDriver: (id, authStatus) => request.post('/admin/driver/auth', null, { params: { id, authStatus } }),
  
  // 员工
  getStaffs: (params) => request.get('/admin/staffs', { params }),
  addStaff: (data) => request.post('/admin/staff/add', data),
  updateStaff: (data) => request.post('/admin/staff/update', data),
  deleteStaff: (id) => request.post(`/admin/staff/delete/${id}`),
  
  // 客户
  getCustomers: (params) => request.get('/admin/customers', { params })
}
