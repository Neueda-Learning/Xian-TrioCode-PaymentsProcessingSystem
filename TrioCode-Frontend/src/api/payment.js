import request from '@/utils/request'

/**
 * Payment management API wrappers.
 * Standard response shape Result<T>: { code, message, data }
 * Pagination shape PageResult<T>: { records, total, pageNum, pageSize, totalPages }
 */

// ---------------------------------------------------------------------------
// Local mock fallback data.
// When GET /api/v1/payments is unavailable (common during integration),
// we use local data that matches the real response shape so switching later is seamless.
// ---------------------------------------------------------------------------
const MOCK_STATUSES = ['CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED']
const MOCK_CURRENCIES = ['USD', 'EUR', 'GBP', 'CNY', 'JPY']
const MOCK_CURRENCY_NAMES = {
  USD: 'US Dollar',
  EUR: 'Euro',
  GBP: 'British Pound',
  CNY: 'Chinese Yuan',
  JPY: 'Japanese Yen',
}

function buildMockRecords() {
  const records = []
  for (let i = 1; i <= 57; i++) {
    records.push({
      paymentId: i,
      paymentNo: `PAY${String(i).padStart(8, '0')}`,
      sourceAccountId: 1000 + (i % 10),
      destinationAccountId: 2000 + (i % 7),
      amount: Math.round((Math.random() * 10000 + 10) * 100) / 100,
      currency: MOCK_CURRENCIES[i % MOCK_CURRENCIES.length],
      status: MOCK_STATUSES[i % MOCK_STATUSES.length],
      createdAt: new Date(Date.now() - i * 3600 * 1000).toISOString(),
    })
  }
  return records
}

const MOCK_RECORDS = buildMockRecords()

/**
 * Apply the same filtering and pagination rules to the mock data.
 */
function mockPaymentList(params = {}) {
  let list = MOCK_RECORDS.slice()

  if (params.status) {
    list = list.filter((item) => item.status === params.status)
  }
  if (params.paymentNo) {
    list = list.filter((item) => item.paymentNo.includes(params.paymentNo))
  }
  if (params.currency) {
    list = list.filter((item) => item.currency === params.currency)
  }
  if (params.createdFrom) {
    const from = new Date(params.createdFrom).getTime()
    list = list.filter((item) => new Date(item.createdAt).getTime() >= from)
  }
  if (params.createdTo) {
    const to = new Date(params.createdTo).getTime()
    list = list.filter((item) => new Date(item.createdAt).getTime() <= to)
  }

  const pageNum = params.pageNum || 1
  const pageSize = params.pageSize || 10
  const total = list.length
  const start = (pageNum - 1) * pageSize
  const records = list.slice(start, start + pageSize)

  return {
    code: 'SUCCESS',
    message: 'ok (mock data)',
    data: {
      records,
      total,
      pageNum,
      pageSize,
      totalPages: Math.ceil(total / pageSize) || 1,
    },
  }
}

/**
 * Fetch the payment list page.
 * When the real endpoint is unavailable (network error / not deployed), fall back to local mock data.
 * @param {Object} params query parameters
 */
export async function getPaymentList(params) {
  try {
    return await request({
      url: '/v1/payments',
      method: 'get',
      params,
    })
  } catch (error) {
    // Only fall back to mock when no backend response is received (network error / endpoint not deployed)
    // so we do not hide real business errors returned by the backend.
    if (!error?.response) {
      console.warn('[payment] /v1/payments unavailable, using local mock data', error)
      return mockPaymentList(params)
    }
    throw error
  }
}

/**
 * Create payment (full synchronous flow: CREATED -> VALIDATED -> SENT -> COMPLETED/FAILED).
 * @param {Object} data PaymentCreateReqDTO
 */
export function createPayment(data) {
  return request({
    url: '/v1/payments',
    method: 'post',
    data,
  })
}

/**
 * Query payment timeline by paymentId.
 * @param {number|string} paymentId
 */
export function getHistoriesById(paymentId) {
  return request({
    url: `/v1/payments/${paymentId}/histories`,
    method: 'get',
  })
}

/**
 * Query payment timeline by paymentNo.
 * @param {string} paymentNo
 */
export function getHistoriesByPaymentNo(paymentNo) {
  return request({
    url: `/v1/payments/no/${paymentNo}/histories`,
    method: 'get',
  })
}

/**
 * Get all available currencies.
 * If the real endpoint is unavailable, fall back to a local mock list so the dropdowns still work.
 */
export async function getAllCurrency() {
  try {
    return await request({
      url: '/v1/getAllCurrency',
      method: 'get',
    })
  } catch (error) {
    if (!error?.response) {
      console.warn('[payment] /v1/getAllCurrency unavailable, using local mock currency data', error)
      return {
        code: 'SUCCESS',
        message: 'ok (mock data)',
        data: MOCK_CURRENCIES.map((code) => ({ code, codeName: MOCK_CURRENCY_NAMES[code] || code })),
      }
    }
    throw error
  }
}
