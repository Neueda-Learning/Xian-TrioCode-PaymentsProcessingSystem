import axios from 'axios'
import { ElMessage } from 'element-plus'
import { normalizePaymentErrorMessage, normalizePaymentPayload } from './paymentError'

/**
 * Axios instance pre-configured for the TrioCode backend.
 * Base URL is read from Vite env variable VITE_API_BASE_URL (see .env files).
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

request.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => normalizePaymentPayload(response.data),
  (error) => {
    const responseData = normalizePaymentPayload(error?.response?.data || {})
    const requestUrl = error?.config?.url || ''
    const isPaymentNoHistoryLookup = /^\/v1\/payments\/no\/[^/]+\/histories$/.test(requestUrl)
    const message = normalizePaymentErrorMessage(responseData.message || error.message, responseData.code)
    error.normalizedMessage = message
    error.response = error.response ? { ...error.response, data: responseData } : error.response
    if (!isPaymentNoHistoryLookup) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export default request
