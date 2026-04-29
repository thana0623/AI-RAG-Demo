import axios from 'axios'
import type { ApiResult } from '@/types/api'
import { getToken, setToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult<unknown>
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      setToken('')
      window.location.href = '/login'
    }
    return Promise.reject(new Error(res.message || 'Request failed'))
  },
  (error) => {
    if (error?.response?.status === 401) {
      setToken('')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
