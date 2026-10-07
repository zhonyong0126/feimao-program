const ENV_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''
const API_ORIGIN = ENV_BASE_URL || (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : '')
const BASE_URL = API_ORIGIN.replace(/\/$/, '') + '/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/my/index' })
          reject(res.data || res)
          return
        }
        if (res.statusCode === 200) {
          if (res.data.code === 200 || res.data.code === 1) {
            resolve(res.data)
          } else {
            uni.showToast({ title: res.data.msg || '请求失败', icon: 'none' })
            reject(res.data)
          }
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
        }
      },
      fail: reject
    })
  })
}

export default {
  get(url, data) { return request({ url, data }) },
  post(url, data) { return request({ url, data, method: 'POST' }) }
}

export const api = {
  // 员工登录
  login: (data) => request({ url: '/staff/login', data, method: 'POST' }),
  logout: () => request({ url: '/staff/logout', method: 'POST' }),
  
  // 订单管理
  getOrders: (data) => request({ url: '/staff/orders', data }),
  getOrderDetail: (id) => request({ url: `/staff/order/${id}` }),
  createOrder: (data) => request({ url: '/staff/order/create', data, method: 'POST' }),
  dispatchOrder: (data) => request({ url: '/staff/order/dispatch', data, method: 'POST' }),
  cancelOrder: (id) => request({ url: `/staff/order/cancel/${id}`, method: 'POST' }),
  
  // 司机列表
  getDrivers: (data) => request({ url: '/staff/drivers', data }),
  getOnlineDrivers: () => request({ url: '/staff/drivers/online' }),
  
  // 客户管理
  getCustomers: (data) => request({ url: '/staff/customers', data }),
  createCustomer: (data) => request({ url: '/staff/customer/create', data, method: 'POST' }),
  updateCustomer: (data) => request({ url: '/staff/customer/update', data, method: 'POST' }),
  
  // 统计
  getStats: () => request({ url: '/staff/stats/today' })
}
