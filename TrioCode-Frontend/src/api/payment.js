import request from '@/utils/request'

/**
 * 支付管理相关接口封装
 * 统一响应结构 Result<T>: { code, message, data }
 * 分页结构 PageResult<T>: { records, total, pageNum, pageSize, totalPages }
 */

// ---------------------------------------------------------------------------
// 本地 Mock 数据兜底
// 当后端 GET /api/v1/payments 接口暂未部署（联调阶段常见网络错误）时，
// 使用与真实接口结构完全一致的本地数据，方便后续无缝切换到真实接口。
// ---------------------------------------------------------------------------
const MOCK_STATUSES = ['CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED']
const MOCK_CURRENCIES = ['USD', 'EUR', 'GBP', 'CNY', 'JPY']
const MOCK_CURRENCY_NAMES = {
  USD: '美元',
  EUR: '欧元',
  GBP: '英镑',
  CNY: '人民币',
  JPY: '日元',
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
 * 对 mock 数据做与后端一致的筛选 + 分页处理
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
 * 获取支付分页列表
 * 真实接口不可用时（网络错误 / 未部署），自动降级为本地 mock 数据
 * @param {Object} params 查询参数
 */
export async function getPaymentList(params) {
  try {
    return await request({
      url: '/v1/payments',
      method: 'get',
      params,
    })
  } catch (error) {
    // 仅在请求本身未收到后端响应（网络错误/接口未部署）时才降级为 mock，
    // 避免掩盖后端返回的真实业务错误。
    if (!error?.response) {
      console.warn('[payment] /v1/payments 接口不可用，使用本地 mock 数据兜底', error)
      return mockPaymentList(params)
    }
    throw error
  }
}

/**
 * 创建支付（同步全流程：CREATED -> VALIDATED -> SENT -> COMPLETED/FAILED）
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
 * 根据 paymentId 查询支付状态时间线
 * @param {number|string} paymentId
 */
export function getHistoriesById(paymentId) {
  return request({
    url: `/v1/payments/${paymentId}/histories`,
    method: 'get',
  })
}

/**
 * 根据 paymentNo 查询支付状态时间线
 * @param {string} paymentNo
 */
export function getHistoriesByPaymentNo(paymentNo) {
  return request({
    url: `/v1/payments/no/${paymentNo}/histories`,
    method: 'get',
  })
}

/**
 * 获取所有可用币种
 * 真实接口不可用时降级为本地 mock 币种列表，保证筛选/创建表单下拉框可用
 */
export async function getAllCurrency() {
  try {
    return await request({
      url: '/v1/getAllCurrency',
      method: 'get',
    })
  } catch (error) {
    if (!error?.response) {
      console.warn('[payment] /v1/getAllCurrency 接口不可用，使用本地 mock 币种数据兜底', error)
      return {
        code: 'SUCCESS',
        message: 'ok (mock data)',
        data: MOCK_CURRENCIES.map((code) => ({ code, codeName: MOCK_CURRENCY_NAMES[code] || code })),
      }
    }
    throw error
  }
}
