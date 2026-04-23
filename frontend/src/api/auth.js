import request from './request'

/**
 * 发送邮箱验证码
 * @param {string} email - 邮箱地址
 * @param {string} type - 业务类型：REGISTER | RESET
 * @returns {Promise}
 */
export function sendCode(email, type) {
  return request.post('/auth/send-code', { email, type })
}

/**
 * 用户注册
 * @param {object} params - { email, username, password, code }
 * @returns {Promise}
 */
export function register(params) {
  return request.post('/auth/register', params)
}

/**
 * 用户登录
 * @param {string} identifier - 邮箱或用户名
 * @param {string} password - 密码
 * @returns {Promise}
 */
export function login(identifier, password) {
  return request.post('/auth/login', { identifier, password })
}

/**
 * 重置密码
 * @param {object} params - { email, newPassword, code }
 * @returns {Promise}
 */
export function resetPassword(params) {
  return request.post('/auth/reset-password', params)
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request.get('/auth/current')
}
