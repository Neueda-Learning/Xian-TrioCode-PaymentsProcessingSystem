<script setup>
/**
 * Payment management main view: combines list, quick search, create dialog, and timeline dialog.
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import logoUrl from '@/assets/logo.svg'
import PaymentTable from '@/components/PaymentTable.vue'
import PaymentCreateDialog from '@/components/PaymentCreateDialog.vue'
import PaymentTimelineDialog from '@/components/PaymentTimelineDialog.vue'
import { getHistoriesByPaymentNo } from '@/api/payment'
import { usePaymentStore } from '@/store/payment'
import { normalizePaymentErrorMessage } from '@/utils/paymentError'
const store = usePaymentStore()
const tableRef = ref(null)
const quickSearchNo = ref('')
const createVisible = ref(false)
const timelineVisible = ref(false)
/**
 * Clicking any table row opens the timeline using the row data (which already includes paymentId).
 */
function handleRowClick(row) {
  store.setSelectedPayment(row)
  timelineVisible.value = true
}
/**
 * Top toolbar quick search: validate by order number and open the payment timeline.
 */
async function handleQuickSearch() {
  const paymentNo = quickSearchNo.value.trim()
  const notFoundMessage = 'No payment record was found for this order number.'
  if (!paymentNo) {
    ElMessage.warning('Please enter an order number')
    return
  }
  try {
    const res = await getHistoriesByPaymentNo(paymentNo)
    if (res.code === 'SUCCESS' && res.data && res.data.length > 0) {
      store.setSelectedPayment({ paymentNo })
      timelineVisible.value = true
    } else {
      ElMessage.error(normalizePaymentErrorMessage(res.message || notFoundMessage, res.code) || notFoundMessage)
    }
  } catch (error) {
    ElMessage.error(error?.normalizedMessage || notFoundMessage)
  }
}
/**
 * On successful create payment (whether COMPLETED or FAILED): refresh the list and open the timeline.
 */
function handleCreateSuccess(detail) {
  tableRef.value?.reload()
  store.setSelectedPayment(detail)
  timelineVisible.value = true
}
</script>
<template>
  <div class="payment-view">
    <section class="page-hero">
      <div class="page-heading">
        <div class="brand-row">
          <img :src="logoUrl" alt="TrioCode Payment" class="brand-logo" />
          <p class="eyebrow">TrioCode Payment</p>
        </div>
        <h2 class="title">Payment Management</h2>
        <p class="subtitle">Search orders, create payments, and review the full timeline.</p>
      </div>
      <div class="toolbar-actions">
        <el-input
          v-model="quickSearchNo"
          class="quick-search"
          placeholder="Search payment timeline by order number"
          clearable
          @keyup.enter="handleQuickSearch"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-button class="search-button" plain @click="handleQuickSearch">
          <el-icon><Search /></el-icon>
          Search
        </el-button>
        <el-button class="create-button" type="primary" @click="createVisible = true">New Payment</el-button>
      </div>
    </section>
    <div class="content-shell">
      <PaymentTable ref="tableRef" @row-click="handleRowClick" />
    </div>
    <PaymentCreateDialog v-model="createVisible" @success="handleCreateSuccess" />
    <PaymentTimelineDialog v-model="timelineVisible" :payment="store.selectedPayment" />
  </div>
</template>
<style scoped>
.payment-view {
  max-width: 1680px;
  margin: 0 auto;
  padding: 24px 28px 30px;
}
.page-hero {
  display: grid;
  grid-template-columns: minmax(420px, 1fr) auto;
  align-items: center;
  column-gap: 18px;
  margin-bottom: 14px;
  padding: 18px 22px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 16px;
  background:
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.14), transparent 34%),
    linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(30, 41, 59, 0.92));
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.14);
}
.page-heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.brand-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.brand-logo {
  width: 24px;
  height: 24px;
  filter: drop-shadow(0 0 6px rgba(56, 189, 248, 0.45));
}
.eyebrow {
  margin: 0;
  color: #93c5fd;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
  color: #f8fafc;
}
.subtitle {
  margin: 0;
  color: rgba(226, 232, 240, 0.78);
  font-size: 13px;
}
.toolbar-actions {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  justify-content: flex-end;
  gap: 10px;
}
.quick-search {
  width: 380px;
}
.search-button {
  height: 40px;
  padding: 0 18px;
  border-radius: 999px;
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(226, 232, 240, 0.22);
}
.search-button:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(226, 232, 240, 0.34);
}
.search-button :deep(.el-icon) {
  margin-right: 6px;
}
.quick-search :deep(.el-input__wrapper) {
  border-radius: 999px;
  padding-left: 14px;
  padding-right: 14px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.18);
}
.search-icon {
  cursor: pointer;
  color: #64748b;
}
.search-icon:hover {
  color: var(--el-color-primary);
}
.create-button {
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  box-shadow: 0 14px 30px rgba(59, 130, 246, 0.3);
}
.content-shell {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
  min-height: 620px;
}
@media (max-width: 960px) {
  .payment-view {
    padding: 16px;
  }
  .page-hero {
    display: flex;
    align-items: stretch;
    flex-direction: column;
  }
  .toolbar-actions {
    flex-wrap: wrap;
    justify-content: stretch;
  }
  .quick-search {
    width: 100%;
  }
  .search-button,
  .create-button {
    width: 100%;
  }
  .create-button {
    order: 3;
  }
  .search-button {
    order: 2;
  }
}
</style>
