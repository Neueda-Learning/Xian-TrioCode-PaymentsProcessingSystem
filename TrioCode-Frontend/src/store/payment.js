import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAllCurrency } from '@/api/payment'
/**
 * Payment management global state.
 * - filters / pageNum / pageSize: list filtering and pagination conditions
 * - selectedPayment: the payment shown in the timeline dialog
 * - currencyOptions: currency dropdown data source, cached once globally to avoid repeated requests
 */
export const usePaymentStore = defineStore('payment', () => {
  function dedupeCurrencyOptions(options = []) {
    const unique = new Map()
    for (const item of options) {
      if (!item?.code) continue
      const code = String(item.code).trim().toUpperCase()
      if (!code || unique.has(code)) continue
      unique.set(code, { ...item, code })
    }
    return Array.from(unique.values())
  }
  // List filter conditions, aligned with PaymentListQueryDTO fields.
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
  // Currently selected/viewed payment, used by the timeline dialog.
  const selectedPayment = ref(null)
  // Currency dropdown options: code-first for both forms and filters.
  const currencyOptions = ref([])
  const currencyLoaded = ref(false)
  /**
   * Load currency dictionary; only the first call actually performs the request.
   */
  async function loadCurrencyOptions() {
    if (currencyLoaded.value) return
    const res = await getAllCurrency()
    if (res.code === 'SUCCESS') {
      currencyOptions.value = dedupeCurrencyOptions(res.data || [])
      currencyLoaded.value = true
    }
  }
  /**
   * Reset filters and pagination page number (keep pageSize unchanged).
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
