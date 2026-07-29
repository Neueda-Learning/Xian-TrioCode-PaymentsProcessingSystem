<script setup>
/**
 * Payment list component: filter form + table + pagination.
 * Clicking a row (outside buttons) triggers row-click, and the parent opens the timeline dialog.
 */
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usePaymentStore } from '@/store/payment'
import { getPaymentList } from '@/api/payment'
import { formatAmount, formatDateTime, statusTagType, statusLabel } from '@/utils/format'
import { normalizePaymentErrorMessage } from '@/utils/paymentError'

const emit = defineEmits(['row-click'])

const store = usePaymentStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const statusOptions = [
  { value: 'CREATED', label: 'Created' },
  { value: 'VALIDATED', label: 'Validated' },
  { value: 'SENT', label: 'Sent' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'FAILED', label: 'Failed' },
]

const pageSizeOptions = [10, 20, 50, 100]

// Column header filters (funnel icon) act on the currently loaded page only.
const statusFilters = statusOptions.map((item) => ({ text: item.label, value: item.value }))

const amountRangeFilters = [
  { text: 'Under 100', value: '0-100' },
  { text: '100 - 1,000', value: '100-1000' },
  { text: '1,000 - 10,000', value: '1000-10000' },
  { text: '10,000 and above', value: '10000-'},
]

function uniqueAccountFilters(idKey, nameKey) {
  const seen = new Map()
  for (const row of tableData.value) {
    const id = row[idKey]
    if (id === undefined || id === null || seen.has(id)) continue
    seen.set(id, { text: row[nameKey] ? `${row[nameKey]} (ID: ${id})` : `ID: ${id}`, value: id })
  }
  return Array.from(seen.values())
}

const sourceAccountFilters = computed(() => uniqueAccountFilters('sourceAccountId', 'sourceAccountName'))
const destinationAccountFilters = computed(() => uniqueAccountFilters('destinationAccountId', 'destinationAccountName'))

function filterStatusColumn(value, row) {
  return row.status === value
}

function filterSourceAccount(value, row) {
  return row.sourceAccountId === value
}

function filterDestinationAccount(value, row) {
  return row.destinationAccountId === value
}

function filterAmountRange(value, row) {
  const amount = Number(row.amount)
  if (Number.isNaN(amount)) return false
  const [minStr, maxStr] = value.split('-')
  const min = minStr === '' ? -Infinity : Number(minStr)
  const max = maxStr === '' ? Infinity : Number(maxStr)
  return amount >= min && amount < max
}

// Currency select: type-to-match against both the code and the currency name.
const filteredCurrencyOptions = ref([])
watch(
  () => store.currencyOptions,
  (list) => {
    filteredCurrencyOptions.value = list
  },
  { immediate: true },
)
function filterCurrencyOption(query) {
  const keyword = query.trim().toLowerCase()
  if (!keyword) {
    filteredCurrencyOptions.value = store.currencyOptions
    return
  }
  filteredCurrencyOptions.value = store.currencyOptions.filter((item) => {
    const text = `${item.code} ${item.codeName || ''}`.toLowerCase()
    return text.includes(keyword)
  })
}

/**
 * Fetch payment list data.
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
      ElMessage.error(normalizePaymentErrorMessage(res.message || 'Failed to fetch payment list', res.code))
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    // Network errors are already handled by request.js; clear data here to avoid a blank state.
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * Search button: go back to the first page and reload.
 */
function handleSearch() {
  store.pageNum = 1
  fetchList()
}

/**
 * Reset button: clear filters and reload.
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

// Expose reload to the parent so the list can refresh after a successful payment creation.
defineExpose({ reload: fetchList })
</script>

<template>
  <div class="payment-table">
    <!-- Filter section -->
    <el-form :model="store.filters" inline class="filter-form" @submit.prevent>
      <el-form-item label="Order No.">
        <el-input v-model="store.filters.paymentNo" class="filter-input" placeholder="Enter order number" clearable />
      </el-form-item>
      <el-form-item label="Reference">
        <el-input v-model="store.filters.reference" class="filter-input" placeholder="Enter reference keyword" clearable />
      </el-form-item>
      <el-form-item label="Status">
        <el-select
          v-model="store.filters.status"
          class="filter-select"
          placeholder="Select a status"
          clearable
        >
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Currency">
        <el-select
          v-model="store.filters.currency"
          class="filter-select"
          placeholder="Type or select a currency"
          clearable
          filterable
          :filter-method="filterCurrencyOption"
        >
          <el-option
            v-for="item in filteredCurrencyOptions"
            :key="item.code"
            :label="item.codeName ? `${item.code} - ${item.codeName}` : item.code"
            :value="item.code"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Created From">
        <el-date-picker
          v-model="store.filters.createdFrom"
          type="datetime"
          placeholder="Select start time"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="filter-date"
        />
      </el-form-item>
      <el-form-item label="Created To">
        <el-date-picker
          v-model="store.filters.createdTo"
          type="datetime"
          placeholder="Select end time"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="filter-date"
        />
      </el-form-item>
      <el-form-item class="filter-actions">
        <div class="action-group">
          <el-button class="reset-button" plain @click="handleReset">Reset</el-button>
          <el-button class="search-button" type="primary" @click="handleSearch">Search</el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- Table section -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      class="financial-table"
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <template #empty>
        <el-empty description="No payment data available" />
      </template>
      <el-table-column prop="paymentNo" label="Order No." min-width="160" align="center" header-align="center" />
      <el-table-column
        prop="sourceAccountId"
        label="Source Account"
        min-width="180"
        align="center"
        header-align="center"
        column-key="sourceAccountId"
        :filters="sourceAccountFilters"
        :filter-method="filterSourceAccount"
      >
        <template #default="{ row }">
          <div class="account-cell">
            <span class="account-name">{{ row.sourceAccountName || '-' }}</span>
            <span class="account-id">ID: {{ row.sourceAccountId }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="destinationAccountId"
        label="Destination Account"
        min-width="180"
        align="center"
        header-align="center"
        column-key="destinationAccountId"
        :filters="destinationAccountFilters"
        :filter-method="filterDestinationAccount"
      >
        <template #default="{ row }">
          <div class="account-cell">
            <span class="account-name">{{ row.destinationAccountName || '-' }}</span>
            <span class="account-id">ID: {{ row.destinationAccountId }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="amount"
        label="Amount"
        min-width="150"
        align="center"
        header-align="center"
        column-key="amount"
        :filters="amountRangeFilters"
        :filter-method="filterAmountRange"
      >
        <template #default="{ row }">{{ formatAmount(row.amount, row.currency) }}</template>
      </el-table-column>
      <el-table-column
        prop="status"
        label="Status"
        min-width="100"
        align="center"
        header-align="center"
        column-key="status"
        :filters="statusFilters"
        :filter-method="filterStatusColumn"
      >
        <template #default="{ row }">
          <el-tag
            :type="statusTagType(row.status)"
            effect="light"
            class="status-tag"
            :class="[`status-tag--${(row.status || 'unknown').toLowerCase()}`]"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="Created At" min-width="180" sortable align="center" header-align="center">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <!-- Pagination section -->
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
.payment-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 100%;
}
.filter-form {
  padding: 14px 16px 4px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(255, 255, 255, 0.98));
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}
.filter-form :deep(.el-form-item) {
  margin-right: 12px;
  margin-bottom: 8px;
}
.filter-input,
.filter-select,
.filter-date {
  width: 172px;
}
.filter-actions {
  margin-left: auto;
}
.action-group {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 6px;
}
.search-button,
.reset-button {
  height: 38px;
  min-width: 92px;
  padding: 0 18px;
  border-radius: 12px;
  font-weight: 600;
}
.search-button {
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.22);
}
.reset-button {
  color: var(--tc-navy-700);
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(148, 163, 184, 0.3);
}
.reset-button:hover {
  color: var(--tc-blue-700);
  border-color: rgba(37, 99, 235, 0.26);
  background: rgba(37, 99, 235, 0.04);
}
.filter-form :deep(.el-input__wrapper),
.filter-form :deep(.el-select__wrapper),
.filter-form :deep(.el-date-editor.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.12);
}
.pagination-wrapper {
  padding: 9px 12px;
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.14);
}
:deep(.financial-table .el-table__row) {
  cursor: pointer;
}
:deep(.financial-table) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.07);
}
:deep(.financial-table .el-table__header th) {
  background: linear-gradient(180deg, #f8fafc, #eef2f7);
  color: var(--el-text-color-primary);
  font-weight: 700;
  border-right: 1px solid rgba(148, 163, 184, 0.18);
}
:deep(.financial-table .el-table__body td) {
  border-right: 1px solid rgba(148, 163, 184, 0.1);
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  padding: 10px 0;
  line-height: 1.4;
}
:deep(.financial-table .el-table__body tr:hover > td) {
  background-color: rgba(37, 99, 235, 0.04) !important;
}
.status-tag {
  min-width: 68px;
  justify-content: center;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.01em;
  border-color: transparent;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
}
.status-tag--created {
  background-color: rgba(100, 116, 139, 0.12);
  color: #475569;
}
.status-tag--validated {
  background-color: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
}
.status-tag--sent {
  background-color: rgba(245, 158, 11, 0.14);
  color: #b45309;
}
.status-tag--completed {
  background-color: rgba(16, 185, 129, 0.14);
  color: #047857;
}
.status-tag--failed {
  background-color: rgba(239, 68, 68, 0.14);
  color: #b91c1c;
}
.status-tag--unknown {
  background-color: rgba(100, 116, 139, 0.12);
  color: #475569;
}
.account-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.3;
}
.account-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.account-id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
:deep(.el-pagination) {
  --el-pagination-button-width: 32px;
  --el-pagination-button-height: 32px;
}
:deep(.el-pagination .el-pager li),
:deep(.el-pagination button),
:deep(.el-pagination .el-select__wrapper) {
  border-radius: 10px;
}
:deep(.el-pagination.is-background .el-pager li) {
  background-color: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.14);
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--tc-blue-700);
  color: #fff;
}

@media (max-width: 960px) {
  .payment-table {
    gap: 12px;
  }

  .filter-form {
    padding: 16px;
  }

  .filter-input,
  .filter-select,
  .filter-date {
    width: 100%;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
  }

  .action-group {
    width: 100%;
    padding-left: 0;
  }

  .filter-actions :deep(.el-form-item__content) {
    width: 100%;
    display: flex;
    gap: 12px;
  }

  .search-button,
  .reset-button {
    flex: 1;
  }

  .pagination-wrapper {
    justify-content: center;
    padding: 12px;
  }
}
</style>
