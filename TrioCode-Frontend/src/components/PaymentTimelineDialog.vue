<script setup>
/**
 * 支付状态时间线弹窗
 * props.payment 可能来自三种触发场景：
 *  1) 列表行点击：包含完整 PaymentListItemVO 字段（paymentId/paymentNo/amount/currency/status）
 *  2) 快速搜索（按 paymentNo）：仅有 paymentNo，需按 paymentNo 查历史
 *  3) 新建支付成功：包含完整 PaymentDetailVO 字段
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getHistoriesById, getHistoriesByPaymentNo } from '@/api/payment'
import { formatAmount, statusTagType, statusLabel } from '@/utils/format'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  payment: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const histories = ref([])
// 头部展示用的支付基本信息（部分场景下可能字段不全）
const headerInfo = ref({ paymentNo: '', amount: null, currency: '', status: '' })

function close() {
  emit('update:modelValue', false)
}

/**
 * 拉取时间线数据：优先按 paymentId 查询，没有 paymentId 时按 paymentNo 查询
 */
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
      // 若调用方未提供当前状态，用最后一条历史的 toStatus 兜底展示
      if (!headerInfo.value.status && histories.value.length) {
        headerInfo.value.status = histories.value[histories.value.length - 1].toStatus
      }
    } else {
      ElMessage.error(res.message || '查询支付时间线失败')
    }
  } catch (error) {
    // 网络错误已由 request.js 统一提示
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
    title="支付状态时间线"
    width="600px"
    @update:model-value="(val) => emit('update:modelValue', val)"
  >
    <div class="payment-header" v-loading="loading">
      <div class="header-item">
        <span class="label">订单号：</span>
        <span class="value">{{ headerInfo.paymentNo || '-' }}</span>
      </div>
      <div class="header-item" v-if="headerInfo.amount !== null">
        <span class="label">金额：</span>
        <span class="value">{{ formatAmount(headerInfo.amount, headerInfo.currency) }}</span>
      </div>
      <div class="header-item">
        <span class="label">当前状态：</span>
        <el-tag :type="statusTagType(headerInfo.status)">{{ statusLabel(headerInfo.status) }}</el-tag>
      </div>
    </div>

    <el-empty v-if="!loading && histories.length === 0" description="暂无时间线记录" />

    <el-timeline v-else class="timeline">
      <el-timeline-item
        v-for="item in histories"
        :key="item.historyId"
        :timestamp="item.createdAt"
        placement="top"
        :type="item.toStatus === 'FAILED' ? 'danger' : 'primary'"
      >
        <div class="timeline-title">
          {{ item.fromStatus ? statusLabel(item.fromStatus) : '创建' }} → {{ statusLabel(item.toStatus) }}
        </div>
        <div v-if="item.reference" class="timeline-reference">备注：{{ item.reference }}</div>
        <div v-if="item.toStatus === 'FAILED'" class="timeline-error">
          错误码：{{ item.errorCode || '-' }}；错误信息：{{ item.errorMessage || '-' }}
        </div>
      </el-timeline-item>
    </el-timeline>

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.payment-header {
  display: flex;
  gap: 24px;
  padding: 8px 12px 16px;
  border-bottom: 1px solid var(--el-border-color);
  margin-bottom: 16px;
}
.header-item .label {
  color: var(--el-text-color-secondary);
}
.header-item .value {
  font-weight: 600;
}
.timeline {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 8px;
}
.timeline-title {
  font-weight: 600;
}
.timeline-reference {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.timeline-error {
  color: var(--el-color-danger);
  font-size: 13px;
  margin-top: 4px;
}
</style>
