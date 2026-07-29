const CODE_MESSAGES = {
  VALIDATION_FAILED: 'Validation failed.',
  INVALID_AMOUNT: 'Invalid amount.',
  INVALID_ACCOUNT: 'Invalid account.',
  INVALID_CURRENCY: 'Unsupported currency.',
  INSUFFICIENT_FUNDS: 'Insufficient account balance.',
  INVALID_STATUS_TRANSITION: 'Invalid payment status transition.',
  DUPLICATE_PAYMENT: 'Order number already exists. Please do not submit twice.',
  PAYMENT_NOT_FOUND: 'Payment record not found.',
  PROCESSING_ERROR: 'Payment processing error.',
  NETWORK_ERROR: 'Network communication failed. Please try again later.',
}

const FALLBACK_RULES = [
  { pattern: /\u8ba2\u5355\u53f7\u5df2\u5b58\u5728|\u8bf7\u52ff\u91cd\u590d\u63d0\u4ea4/, message: CODE_MESSAGES.DUPLICATE_PAYMENT },
  { pattern: /\u672a\u627e\u5230\u8be5\u8ba2\u5355\u53f7\u5bf9\u5e94\u7684\u652f\u4ed8\u8bb0\u5f55|\u8ba2\u5355\u53f7\u5bf9\u5e94\u7684\u652f\u4ed8\u8bb0\u5f55\u4e0d\u5b58\u5728/, message: 'No payment record was found for this order number.' },
  { pattern: /\u652f\u4ed8\u8bb0\u5f55\u4e0d\u5b58\u5728/, message: CODE_MESSAGES.PAYMENT_NOT_FOUND },
  { pattern: /\u8d26\u6237\u4f59\u989d\u4e0d\u8db3/, message: CODE_MESSAGES.INSUFFICIENT_FUNDS },
  { pattern: /\u8d26\u6237\u4e0d\u5b58\u5728\u6216\u5df2\u7981\u7528/, message: CODE_MESSAGES.INVALID_ACCOUNT },
  { pattern: /\u4e0d\u652f\u6301\u7684\u5e01\u79cd(:\s*.*)?/, message: CODE_MESSAGES.INVALID_CURRENCY },
  { pattern: /\u91d1\u989d\u65e0\u6548/, message: CODE_MESSAGES.INVALID_AMOUNT },
  { pattern: /\u975e\u6cd5\u7684\u652f\u4ed8\u72b6\u6001\u6d41\u8f6c/, message: CODE_MESSAGES.INVALID_STATUS_TRANSITION },
  { pattern: /\u7cfb\u7edf\u5185\u90e8\u5904\u7406\u5f02\u5e38/, message: CODE_MESSAGES.PROCESSING_ERROR },
  { pattern: /\u7f51\u7edc\u901a\u4fe1\u5931\u8d25/, message: CODE_MESSAGES.NETWORK_ERROR },
  { pattern: /\u4ed8\u6b3e\u8d26\u6237\u4e0e\u6536\u6b3e\u8d26\u6237\u4e0d\u80fd\u76f8\u540c/, message: 'The source and destination accounts cannot be the same.' },
  { pattern: /\u4ed8\u6b3e\u8d26\u6237\u4e0d\u5b58\u5728\u6216\u5df2\u7981\u7528/, message: 'The source account does not exist or is disabled.' },
  { pattern: /\u6536\u6b3e\u8d26\u6237\u4e0d\u5b58\u5728\u6216\u5df2\u7981\u7528/, message: 'The destination account does not exist or is disabled.' },
  { pattern: /\u5e76\u53d1\u51b2\u7a81\uff0c\u72b6\u6001\u66f4\u65b0\u5931\u8d25/, message: 'Status update failed due to a concurrent change.' },
  { pattern: /\u5e76\u53d1\u51b2\u7a81\uff0cFAILED\u72b6\u6001\u66f4\u65b0\u5931\u8d25/, message: 'FAILED status update failed due to a concurrent change.' },
  { pattern: /\u6e05\u7b97\u7cfb\u7edf\u6682\u65f6\u4e0d\u53ef\u7528/, message: 'The clearing system is temporarily unavailable.' },
]

function containsCjk(text) {
  return /[\u4e00-\u9fff]/.test(text)
}

export function normalizePaymentErrorMessage(message, code = '') {
  const text = String(message || '').trim()
  const normalizedCode = String(code || '').trim().toUpperCase()

  if (normalizedCode && CODE_MESSAGES[normalizedCode]) {
    return CODE_MESSAGES[normalizedCode]
  }

  if (!text) {
    return 'Request failed.'
  }

  if (!containsCjk(text)) {
    return text
  }

  for (const rule of FALLBACK_RULES) {
    if (rule.pattern.test(text)) {
      return rule.message
    }
  }

  return 'The request could not be completed.'
}

export function normalizePaymentPayload(payload) {
  if (Array.isArray(payload)) {
    return payload.map((item) => normalizePaymentPayload(item))
  }

  if (!payload || typeof payload !== 'object') {
    return payload
  }

  const normalized = { ...payload }

  if (typeof normalized.message === 'string') {
    normalized.message = normalizePaymentErrorMessage(normalized.message, normalized.code)
  }

  if (typeof normalized.failureMessage === 'string') {
    normalized.failureMessage = normalizePaymentErrorMessage(
      normalized.failureMessage,
      normalized.failureCode || normalized.code,
    )
  }

  if (typeof normalized.errorMessage === 'string') {
    normalized.errorMessage = normalizePaymentErrorMessage(
      normalized.errorMessage,
      normalized.errorCode || normalized.code,
    )
  }

  if ('data' in normalized) {
    normalized.data = normalizePaymentPayload(normalized.data)
  }

  return normalized
}

