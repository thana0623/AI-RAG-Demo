import request from './request'
import type { ApiResult } from '@/types/api'

export interface UploadResponse {
  docId: string
  statusUrl: string
}

export interface StatusResponse {
  docId: string
  status: string
}

export interface AskResponse {
  question: string
  answer: string
}

export function uploadDocument(content: string) {
  return request.post<unknown, ApiResult<UploadResponse>>('/rag/document', { content })
}

export function getDocumentStatus(docId: string) {
  return request.get<unknown, ApiResult<StatusResponse>>(`/rag/status/${docId}`)
}

export function askQuestion(question: string) {
  return request.post<unknown, ApiResult<AskResponse>>('/rag/ask', { question })
}
