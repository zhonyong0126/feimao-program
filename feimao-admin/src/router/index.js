import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '首页概览' }
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('@/views/order/index.vue'),
    meta: { title: '订单管理' }
  },
  {
    path: '/driver',
    name: 'Driver',
    component: () => import('@/views/driver/index.vue'),
    meta: { title: '司机管理' }
  },
  {
    path: '/staff',
    name: 'Staff',
    component: () => import('@/views/staff/index.vue'),
    meta: { title: '员工管理' }
  },
  {
    path: '/customer',
    name: 'Customer',
    component: () => import('@/views/customer/index.vue'),
    meta: { title: '客户管理' }
  }
]

const router = new VueRouter({
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 飞猫后台` : '飞猫后台管理'
  
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
