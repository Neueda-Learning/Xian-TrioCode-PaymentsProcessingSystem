import axios from 'axios'
import { ElMessage } from 'element-plus'

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
  (response) => response.data,
  (error) => {
    const message = error?.response?.data?.message || error.message || 'Request failed'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
