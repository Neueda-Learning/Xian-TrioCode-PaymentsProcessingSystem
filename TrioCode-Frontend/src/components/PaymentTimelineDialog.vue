<script setup>
/**
 * Payment status timeline dialog.
 * props.payment may come from three trigger scenarios:
 *  1) Table row click: full PaymentListItemVO fields are present
 *  2) Quick search by paymentNo: only paymentNo is available, so history is loaded by paymentNo
 *  3) Successful create payment: full PaymentDetailVO fields are present
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getHistoriesById, getHistoriesByPaymentNo } from '@/api/payment'
import { formatAmount, historyReferenceLabel, statusTagType, statusLabel } from '@/utils/format'
import { normalizePaymentErrorMessage } from '@/utils/paymentError'
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  payment: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])
const loading = ref(false)
const histories = ref([])
const headerInfo = ref({ paymentNo: '', amount: null, currency: '', status: '' })
function close() {
  emit('update:modelValue', false)
}
async function fetchHistories(target) {
  if (!target) return
  loading.value = true
  histories.value = []
  headerInfo.value = {
    paymentNo: target.paymentNo || '',
    amount: target.amount ?? null,
    currency: target.currency || '',
    status: target.status || '',
  }
  try {
    const res = target.paymentId
      ? await getHistoriesById(target.paymentId)
      : await getHistoriesByPaymentNo(target.paymentNo)
    if (res.code === 'SUCCESS') {
      histories.value = res.data || []
      if (!headerInfo.value.status && histories.value.length) {
        headerInfo.value.status = histories.value[histories.value.length - 1].toStatus
      }
    } else {
      ElMessage.error(normalizePaymentErrorMessage(res.message || 'Failed to load the payment timeline', res.code))
    }
  } catch (error) {
  } finally {
    loading.value = false
  }
}
watch(
  () => [props.modelValue, props.payment],
  ([visible, payment]) => {
    if (visible && payment) {
      fetchHistories(payment)
    }
  },
  { immediate: true },
)
</script>
<template>
  <el-dialog
    :model-value="modelValue"
    title="Payment Timeline"
    width="600px"
    class="payment-timeline-dialog"
    align-center
    @update:model-value="(val) => emit('update:modelValue', val)"
  >
    <div class="dialog-summary" v-loading="loading">
      <div class="summary-item">
        <span class="label">Order No.</span>
        <span class="value">{{ headerInfo.paymentNo || '-' }}</span>
      </div>
      <div class="summary-item" v-if="headerInfo.amount !== null">
        <span class="label">Amount</span>
        <span class="value amount">{{ formatAmount(headerInfo.amount, headerInfo.currency) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">Current Status</span>
        <el-tag class="status-tag" :class="[`status-tag--${(headerInfo.status || 'unknown').toLowerCase()}`]" :type="statusTagType(headerInfo.status)" effect="light">
          {{ statusLabel(headerInfo.status) }}
        </el-tag>
      </div>
    </div>
    <el-empty v-if="!loading && histories.length === 0" description="No timeline records available" />
    <el-timeline v-else class="timeline">
      <el-timeline-item
        v-for="item in histories"
        :key="item.historyId"
        :timestamp="item.createdAt"
        placement="top"
        :type="item.toStatus === 'FAILED' ? 'danger' : 'primary'"
      >
        <div class="timeline-title">
          {{ item.fromStatus ? statusLabel(item.fromStatus) : 'Created' }} -> {{ statusLabel(item.toStatus) }}
        </div>
        <div v-if="item.reference" class="timeline-reference">Reference: {{ historyReferenceLabel(item.reference) }}</div>
        <div v-if="item.toStatus === 'FAILED'" class="timeline-error">
          Error Code: {{ item.errorCode || '-' }}; Error Message: {{ normalizePaymentErrorMessage(item.errorMessage || '-', item.errorCode) }}
        </div>
      </el-timeline-item>
    </el-timeline>
    <template #footer>
      <div class="dialog-footer-actions">
        <el-button class="footer-button" @click="close">Close</el-button>
      </div>
    </template>
  </el-dialog>
</template>
<style scoped>
.payment-timeline-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
}
.payment-timeline-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
.payment-timeline-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.payment-timeline-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 10px;
}
.payment-timeline-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}
.dialog-summary {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(255, 255, 255, 0.96));
  margin-bottom: 18px;
}
.summary-item { flex: 1; min-width: 0; }
.summary-item .label { display: block; color: var(--el-text-color-secondary); font-size: 12px; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.08em; }
.summary-item .value { font-weight: 700; color: var(--el-text-color-primary); word-break: break-word; }
.summary-item .amount { font-size: 16px; }
.status-tag { min-width: 72px; justify-content: center; border-radius: 999px; font-weight: 600; border-color: transparent; box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04); }
.status-tag--created { background-color: rgba(100, 116, 139, 0.12); color: #475569; }
.status-tag--validated { background-color: rgba(37, 99, 235, 0.12); color: #1d4ed8; }
.status-tag--sent { background-color: rgba(245, 158, 11, 0.14); color: #b45309; }
.status-tag--completed { background-color: rgba(16, 185, 129, 0.14); color: #047857; }
.status-tag--failed { background-color: rgba(239, 68, 68, 0.14); color: #b91c1c; }
.status-tag--unknown { background-color: rgba(100, 116, 139, 0.12); color: #475569; }
.timeline { max-height: 420px; overflow-y: auto; padding: 4px 6px 6px 2px; }
.timeline :deep(.el-timeline-item__timestamp) { color: var(--el-text-color-secondary); font-size: 12px; }
.timeline-title { font-weight: 600; }
.timeline-reference { color: var(--el-text-color-secondary); font-size: 13px; margin-top: 4px; }
.timeline-error { color: var(--el-color-danger); font-size: 13px; margin-top: 4px; }
.dialog-footer-actions { display: flex; justify-content: flex-end; }
.footer-button { min-width: 92px; height: 40px; border-radius: 999px; }
@media (max-width: 640px) { .dialog-summary { flex-direction: column; } }
</style>
