<script setup>
/**
 * 支付管理主视图：组合列表、快速搜索、新建支付弹窗、时间线弹窗
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PaymentTable from '@/components/PaymentTable.vue'
import PaymentCreateDialog from '@/components/PaymentCreateDialog.vue'
import PaymentTimelineDialog from '@/components/PaymentTimelineDialog.vue'
import { getHistoriesByPaymentNo } from '@/api/payment'
import { usePaymentStore } from '@/store/payment'

const store = usePaymentStore()

const tableRef = ref(null)
const quickSearchNo = ref('')

const createVisible = ref(false)
const timelineVisible = ref(false)

/**
 * 点击列表任意一行：直接用行数据（已含 paymentId）弹出时间线
 */
function handleRowClick(row) {
  store.setSelectedPayment(row)
  timelineVisible.value = true
}

/**
 * 顶部工具栏快速搜索：按订单号校验并定位支付时间线
 */
async function handleQuickSearch() {
  const paymentNo = quickSearchNo.value.trim()
  if (!paymentNo) {
    ElMessage.warning('请输入订单号')
    return
  }
  try {
    const res = await getHistoriesByPaymentNo(paymentNo)
    if (res.code === 'SUCCESS' && res.data && res.data.length > 0) {
      store.setSelectedPayment({ paymentNo })
      timelineVisible.value = true
    } else {
      ElMessage.error('未找到该订单号对应的支付记录')
    }
  } catch (error) {
    // 网络错误已由 request.js 统一提示；后端未找到订单号时通常返回非 SUCCESS code
    ElMessage.error('未找到该订单号对应的支付记录')
  }
}

/**
 * 新建支付成功（无论最终 COMPLETED 还是 FAILED）：刷新列表并弹出时间线
 */
function handleCreateSuccess(detail) {
  tableRef.value?.reload()
  store.setSelectedPayment(detail)
  timelineVisible.value = true
}
</script>

<template>
  <div class="payment-view">
    <div class="toolbar">
      <h2 class="title">支付管理</h2>
      <div class="toolbar-actions">
        <el-input
          v-model="quickSearchNo"
          placeholder="按订单号快速定位支付时间线"
          clearable
          style="width: 260px"
          @keyup.enter="handleQuickSearch"
        >
          <template #suffix>
            <el-icon class="search-icon" @click="handleQuickSearch"><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="createVisible = true">新建支付</el-button>
      </div>
    </div>

    <PaymentTable ref="tableRef" @row-click="handleRowClick" />

    <PaymentCreateDialog v-model="createVisible" @success="handleCreateSuccess" />

    <PaymentTimelineDialog v-model="timelineVisible" :payment="store.selectedPayment" />
  </div>
</template>

<style scoped>
.payment-view {
  padding: 20px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.title {
  margin: 0;
}
.toolbar-actions {
  display: flex;
  gap: 12px;
}
.search-icon {
  cursor: pointer;
}
</style>
