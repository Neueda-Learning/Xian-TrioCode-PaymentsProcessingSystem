/**
 * Formatting utilities.
 */

/**
 * Format an amount with thousands separators, two decimals, and an optional currency code.
 * @param {number|string} amount
 * @param {string} currency
 * @returns {string} e.g. "1,234.56 USD"
 */
export function formatAmount(amount, currency) {
  const num = Number(amount)
  if (Number.isNaN(num)) return amount ?? '-'
  const formatted = num.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
  return currency ? `${formatted} ${currency}` : formatted
}

/**
 * Payment status -> el-tag type mapping
 */
export const STATUS_TAG_TYPE = {
  CREATED: 'info',
  VALIDATED: 'primary',
  SENT: 'warning',
  COMPLETED: 'success',
  FAILED: 'danger',
}

/**
 * Payment status -> English display label
 */
export const STATUS_LABEL = {
  CREATED: 'Created',
  VALIDATED: 'Validated',
  SENT: 'Sent',
  COMPLETED: 'Completed',
  FAILED: 'Failed',
}

/**
 * Legacy/history reference text -> English display label mapping.
 */
export const HISTORY_REFERENCE_LABEL = {
  '支付创建': 'Payment created',
  '业务校验通过': 'Validation passed',
  '发送成功': 'Payment sent',
  '清算完成': 'Settlement completed',
  'Payment created': 'Payment created',
  'Validation passed': 'Validation passed',
  'Payment sent': 'Payment sent',
  'Settlement completed': 'Settlement completed',
}

export function statusTagType(status) {
  return STATUS_TAG_TYPE[status] || 'info'
}

export function statusLabel(status) {
  return STATUS_LABEL[status] || status || '-'
}

export function historyReferenceLabel(reference) {
  const text = String(reference || '').trim()
  return HISTORY_REFERENCE_LABEL[text] || text || '-'
}

