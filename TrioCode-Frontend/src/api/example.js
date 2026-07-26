import request from '@/utils/request'

/**
 * Example API module.
 * Replace baseURL / endpoints with real backend (TrioCode-Backend) endpoints.
 */
export function ping() {
  return request({
    url: '/api/ping',
    method: 'get',
  })
}
