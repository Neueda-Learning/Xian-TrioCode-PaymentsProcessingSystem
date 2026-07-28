import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAllCurrency } from '@/api/payment'

/**
 * 支付管理全局状态
 * - filters / pageNum / pageSize：列表筛选与分页条件
 * - selectedPayment：当前在时间线弹窗中展示的支付（行点击 / 搜索 / 新建成功 均会写入）
 * - currencyOptions：币种下拉数据源，全局缓存一次，避免重复请求
 */
export const usePaymentStore = defineStore('payment', () => {
  // 列表筛选条件，与 PaymentListQueryDTO 字段保持一致
  const filters = ref({
    status: '',
    paymentNo: '',
    reference: '',
    currency: '',
    createdFrom: '',
    createdTo: '',
  })

  const pageNum = ref(1)
  const pageSize = ref(10)

  // 当前选中/查看的支付信息，用于时间线弹窗
  const selectedPayment = ref(null)

  // 币种下拉选项：[{ code, codeName, countryName }]
  const currencyOptions = ref([])
  const currencyLoaded = ref(false)

  /**
   * 加载币种字典，仅首次调用时真正发起请求
   */
  async function loadCurrencyOptions() {
    if (currencyLoaded.value) return
    const res = await getAllCurrency()
    if (res.code === 'SUCCESS') {
      currencyOptions.value = res.data || []
      currencyLoaded.value = true
    }
  }

  /**
   * 重置筛选条件与分页页码（不改变 pageSize）
   */
  function resetFilters() {
    filters.value = {
      status: '',
      paymentNo: '',
      reference: '',
      currency: '',
      createdFrom: '',
      createdTo: '',
    }
    pageNum.value = 1
  }

  function setSelectedPayment(payment) {
    selectedPayment.value = payment
  }

  return {
    filters,
    pageNum,
    pageSize,
    selectedPayment,
    currencyOptions,
    loadCurrencyOptions,
    resetFilters,
    setSelectedPayment,
  }
})
