import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/login/index.vue'),
    meta: { requiresAuth: false, layout: 'auth' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/auth/register/index.vue'),
    meta: { requiresAuth: false, layout: 'auth' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/pages/auth/forgot-password/index.vue'),
    meta: { requiresAuth: false, layout: 'auth' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/home/index.vue'),
    meta: { requiresAuth: true, layout: 'main' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (!to.meta.requiresAuth && userStore.isLoggedIn && to.name === 'Login') {
    next({ name: 'Home' })
    return
  }
  next()
})

export default router
