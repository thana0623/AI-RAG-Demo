import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const email = computed(() => user.value?.email || '')

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  async function login(identifier, password) {
    const res = await loginApi(identifier, password)
    setToken(res.data)
    // 登录成功后获取用户信息
    await fetchUser()
    return res
  }

  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = res.data
    } catch (e) {
      // Token 无效时清除
      setToken('')
      user.value = null
    }
  }

  function logout() {
    setToken('')
    user.value = null
  }

  // 初始化：如果有 Token 则获取用户信息
  async function init() {
    if (token.value) {
      await fetchUser()
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    username,
    email,
    login,
    logout,
    fetchUser,
    setToken,
    init,
  }
})
