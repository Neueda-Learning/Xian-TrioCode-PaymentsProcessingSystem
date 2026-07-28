<script setup>
/**
 * 支付列表组件：筛选表单 + 表格 + 分页
 * 点击行（非按钮区域）触发 row-click 事件，由父组件负责弹出时间线弹窗
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usePaymentStore } from '@/store/payment'
import { getPaymentList } from '@/api/payment'
import { formatAmount, statusTagType, statusLabel } from '@/utils/format'

const emit = defineEmits(['row-click'])

const store = usePaymentStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const statusOptions = [
  { value: 'CREATED', label: '已创建' },
  { value: 'VALIDATED', label: '已校验' },
  { value: 'SENT', label: '已发送' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'FAILED', label: '失败' },
]

const pageSizeOptions = [10, 20, 50, 100]

/**
 * 拉取支付列表数据
 */
async function fetchList() {
  loading.value = true
  try {
    const params = {
      ...store.filters,
      pageNum: store.pageNum,
      pageSize: store.pageSize,
    }
    const res = await getPaymentList(params)
    if (res.code === 'SUCCESS') {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '查询支付列表失败')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    // 网络错误等已由 request.js 统一提示，这里仅兜底清空数据，避免白屏
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮：回到第一页重新加载
 */
function handleSearch() {
  store.pageNum = 1
  fetchList()
}

/**
 * 重置按钮：清空筛选条件并重新加载
 */
function handleReset() {
  store.resetFilters()
  fetchList()
}

function handlePageNumChange(val) {
  store.pageNum = val
  fetchList()
}

function handlePageSizeChange(val) {
  store.pageSize = val
  store.pageNum = 1
  fetchList()
}

function handleRowClick(row) {
  emit('row-click', row)
}

onMounted(() => {
  store.loadCurrencyOptions()
  fetchList()
})

// 暴露给父组件，用于新建支付成功后刷新列表
defineExpose({ reload: fetchList })
</script>

<template>
  <div class="payment-table">
    <!-- 筛选区域 -->
    <el-form :model="store.filters" inline class="filter-form" @submit.prevent>
      <el-form-item label="订单号">
        <el-input v-model="store.filters.paymentNo" placeholder="请输入订单号" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="store.filters.reference" placeholder="请输入备注关键字" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="store.filters.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="币种">
        <el-select v-model="store.filters.currency" placeholder="全部币种" clearable style="width: 140px">
          <el-option
            v-for="item in store.currencyOptions"
            :key="item.code"
            :label="`${item.code} - ${item.codeName}`"
            :value="item.code"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间起">
        <el-date-picker
          v-model="store.filters.createdFrom"
          type="datetime"
          placeholder="选择起始时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 190px"
        />
      </el-form-item>
      <el-form-item label="创建时间止">
        <el-date-picker
          v-model="store.filters.createdTo"
          type="datetime"
          placeholder="选择截止时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 190px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <template #empty>
        <el-empty description="暂无支付数据" />
      </template>
      <el-table-column prop="paymentNo" label="订单号" min-width="160" />
      <el-table-column prop="sourceAccountId" label="付款账户ID" min-width="120" />
      <el-table-column prop="destinationAccountId" label="收款账户ID" min-width="120" />
      <el-table-column prop="amount" label="金额" min-width="150" sortable>
        <template #default="{ row }">{{ formatAmount(row.amount, row.currency) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180" sortable />
    </el-table>

    <!-- 分页区域 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="store.pageNum"
        v-model:page-size="store.pageSize"
        :page-sizes="pageSizeOptions"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageNumChange"
        @size-change="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
:deep(.el-table__row) {
  cursor: pointer;
}
</style>
