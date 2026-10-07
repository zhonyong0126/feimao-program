// API 基础配置
const ENV_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''
const API_ORIGIN = ENV_BASE_URL || (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : '')
const BASE_URL = API_ORIGIN.replace(/\/$/, '') + '/api'

// 请求拦截
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
            uni.showToast({
              title: res.data.msg || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 导出请求方法
export default {
  get(url, data) {
    return request({ url, data, method: 'GET' })
  },
  post(url, data) {
    return request({ url, data, method: 'POST' })
  }
}

// API 接口定义
export const api = {
  // 司机端
  driverLogin: (data) => request({ url: '/driver/login/wx', data, method: 'POST' }),
  logout: () => request({ url: '/driver/logout', method: 'POST' }),
  getDriverInfo: () => request({ url: '/driver/info' }),
  updateDriverInfo: (data) => request({ url: '/driver/update', data, method: 'POST' }),
  submitDriverAuth: (data) => request({ url: '/driver/auth/submit', data, method: 'POST' }),
  updateVehicle: (data) => request({ url: '/driver/vehicle/update', data, method: 'POST' }),
  updateOnlineStatus: (onlineStatus) => request({
    url: '/driver/online-status',
    data: { onlineStatus },
    method: 'POST'
  }),
  
  // 订单相关
  getGrabOrders: (data) => request({ url: '/driver/orders/grab', data }),
  getMyOrders: (data) => request({ url: '/driver/orders/my', data }),
  getOrderDetail: (id) => request({ url: `/driver/order/${id}` }),
  grabOrder: (id) => request({ url: `/driver/order/grab/${id}`, method: 'POST' }),
  startTransport: (id) => request({ url: `/driver/order/start/${id}`, method: 'POST' }),
  arriveConfirm: (id) => request({ url: `/driver/order/arrive/${id}`, method: 'POST' }),
  completeOrder: (id) => request({ url: `/driver/order/complete/${id}`, method: 'POST' }),
  
  // 收入相关
  getTodayStats: () => request({ url: '/driver/stats/today' }),
  getIncomeList: (data) => request({ url: '/driver/income/list', data }),
  withdraw: (data) => request({ url: '/driver/income/withdraw', data, method: 'POST' })
}
