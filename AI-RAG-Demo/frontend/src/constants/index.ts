export const STORAGE_TOKEN_KEY = 'token'

export const DOC_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SUCCESS: 'SUCCESS',
  FAILED: 'FAILED',
  NOT_FOUND: 'NOT_FOUND'
} as const

export const DOC_STATUS_LABELS: Record<string, string> = {
  PENDING: '排队中',
  PROCESSING: '处理中',
  SUCCESS: '已完成',
  FAILED: '失败',
  NOT_FOUND: '未找到'
}
