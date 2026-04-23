import request from './request'

/**
 * 上传文档（异步向量化）
 * @param {string} content - 文档内容
 * @returns {Promise}
 */
export function uploadDocument(content) {
  return request.post('/rag/document', { content })
}

/**
 * 查询文档处理状态
 * @param {string} docId - 文档 ID
 * @returns {Promise}
 */
export function getDocumentStatus(docId) {
  return request.get(`/rag/status/${docId}`)
}

/**
 * RAG 问答
 * @param {string} question - 问题
 * @returns {Promise}
 */
export function askQuestion(question) {
  return request.post('/rag/ask', { question })
}
