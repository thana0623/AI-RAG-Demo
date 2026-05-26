import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getCurrentUser } from '@/services/auth'
import { getToken, setToken } from '@/utils/auth'
import type { UserProfile } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const profile = ref<UserProfile | null>(null)

  const isLoggedIn = computed(() => Boolean(token.value))
  const username = computed(() => profile.value?.username || '')
  const email = computed(() => profile.value?.email || '')

  function setAuthToken(value: string) {
    token.value = value
    setToken(value)
  }

  async function fetchProfile() {
    try {
      const res = await getCurrentUser()
      profile.value = res.data
    } catch (error) {
      setAuthToken('')
      profile.value = null
      throw error
    }
  }

  async function signIn(identifier: string, password: string) {
    const res = await login({ identifier, password })
    setAuthToken(res.data)
    await fetchProfile()
    return res
  }

  function signOut() {
    setAuthToken('')
    profile.value = null
  }

  async function init() {
    if (token.value) {
      await fetchProfile()
    }
  }

  return {
    token,
    profile,
    isLoggedIn,
    username,
    email,
    signIn,
    signOut,
    fetchProfile,
    init
  }
})
