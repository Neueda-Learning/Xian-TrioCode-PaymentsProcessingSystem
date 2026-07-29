import request from '@/utils/request'

/**
 * Account lookup API wrapper.
 * Used to resolve an account name from its ID (e.g. Source/Destination Account ID
 * on the Create Payment form).
 */

/**
 * Get a single account by ID.
 * @param {number|string} id account ID
 * @returns {Promise<{code: string, message: string, data: {id, accountNo, name, status}}>}
 */
export function getAccountById(id) {
  return request({
    url: `/v1/accounts/${id}`,
    method: 'get',
  })
}
