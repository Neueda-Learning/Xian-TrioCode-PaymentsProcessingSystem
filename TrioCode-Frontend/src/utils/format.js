/**
 * 格式化工具函数
 */

/**
 * 将金额格式化为带千分位、保留两位小数的字符串，并附加币种代码
 * @param {number|string} amount 金额
 * @param {string} currency 币种代码，如 USD
 * @returns {string} 例如 "1,234.56 USD"
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
 * 支付状态 -> el-tag type 映射
 */
export const STATUS_TAG_TYPE = {
  CREATED: 'info',
  VALIDATED: 'primary',
  SENT: 'warning',
  COMPLETED: 'success',
  FAILED: 'danger',
}

/**
 * 支付状态 -> 中文展示文案
 */
export const STATUS_LABEL = {
  CREATED: '已创建',
  VALIDATED: '已校验',
  SENT: '已发送',
  COMPLETED: '已完成',
  FAILED: '失败',
}

export function statusTagType(status) {
  return STATUS_TAG_TYPE[status] || 'info'
}

export function statusLabel(status) {
  return STATUS_LABEL[status] || status || '-'
}
