<script setup>
/**
 * Create payment dialog.
 * Submits the synchronous flow POST /api/v1/payments,
 * shows the final status (COMPLETED / FAILED), and notifies the parent to open the timeline.
 */
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayment } from '@/api/payment'
import { getAccountById } from '@/api/account'
import { usePaymentStore } from '@/store/payment'
import { normalizePaymentErrorMessage } from '@/utils/paymentError'
const props = defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'success'])
const store = usePaymentStore()
const formRef = ref(null)
const submitting = ref(false)
const amountTouched = ref(false)
let lastTimestampKey = ''
let sequence = 0

function pad(num, len = 2) {
  return String(num).padStart(len, '0')
}

/**
 * Format the current local time as yyyyMMddHHmmss.
 */
function formatTimestampKey(date) {
  return (
    `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}` +
    `${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`
  )
}

function nextSequence(timestampKey) {
  if (timestampKey === lastTimestampKey) {
    sequence += 1
  } else {
    lastTimestampKey = timestampKey
    sequence = 0
  }
  return sequence
}

/**
 * Generate the order number as PAY + yyyyMMddHHmmss.
 * If multiple payments are created within the same second, a short sequence
 * suffix is appended to keep the order number unique (paymentNo is the idempotency key).
 */
function generatePaymentNo() {
  const timestampKey = formatTimestampKey(new Date())
  const sequenceValue = nextSequence(timestampKey)
  return sequenceValue === 0 ? `PAY${timestampKey}` : `PAY${timestampKey}${pad(sequenceValue, 2)}`
}
const defaultForm = () => ({
  paymentNo: generatePaymentNo(),
  sourceAccountId: undefined,
  destinationAccountId: undefined,
  amount: undefined,
  currency: '',
  reference: '',
})
const form = reactive(defaultForm())
/**
 * Validation rules.
 */
const rules = {
  paymentNo: [
    { required: true, message: 'Please enter an order number', trigger: 'blur' },
    { max: 32, message: 'The order number cannot exceed 32 characters', trigger: 'blur' },
  ],
  sourceAccountId: [
    { required: true, message: 'Please enter the source account ID', trigger: 'blur' },
    { type: 'number', min: 1, message: 'The source account ID must be a positive number', trigger: 'blur' },
  ],
  destinationAccountId: [
    { required: true, message: 'Please enter the destination account ID', trigger: 'blur' },
    { type: 'number', min: 1, message: 'The destination account ID must be a positive number', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && form.sourceAccountId && value === form.sourceAccountId) {
          callback(new Error('The destination account cannot be the same as the source account'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  amount: [
    { required: true, message: 'Please enter an amount', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === undefined || value === null || value === '') return callback()
        const num = Number(value)
        if (Number.isNaN(num)) return callback()
        if (num < 0) {
          callback(new Error('The amount must be greater than 0'))
        } else if (num >= 1000000) {
          callback(new Error('The amount must be less than 1000000'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  currency: [{ required: true, message: 'Please select a currency', trigger: 'change' }],
  reference: [{ max: 128, message: 'The reference cannot exceed 128 characters', trigger: 'blur' }],
}
const amountPattern = /^\d+(\.\d{1,2})?$/

const amountRealtimeError = computed(() => {
  const raw = String(form.amount ?? '').trim()
  if (!raw) {
    return amountTouched.value ? 'Please enter an amount' : ''
  }

  const num = Number(raw)
  if (Number.isNaN(num)) return ''
  if (num < 0) return 'The amount must be greater than 0'
  if (num >= 1000000) return 'The amount must be less than 1000000'

  return ''
})

function handleAmountBlur() {
  amountTouched.value = true
}

// Keep submit disabled until the full form meets client-side constraints.
const isFormSubmittable = computed(() => {
  const paymentNo = String(form.paymentNo || '').trim()
  const sourceId = Number(form.sourceAccountId)
  const destinationId = Number(form.destinationAccountId)
  const amountRaw = String(form.amount ?? '').trim()
  const amountNum = Number(amountRaw)
  const currency = String(form.currency || '').trim()
  const reference = String(form.reference || '')

  if (!paymentNo || paymentNo.length > 32) return false
  if (!Number.isInteger(sourceId) || sourceId < 1) return false
  if (!Number.isInteger(destinationId) || destinationId < 1) return false
  if (sourceId === destinationId) return false
  // Both account IDs must be resolved successfully before submit is enabled.
  if (accountLookup.source.loading || accountLookup.destination.loading) return false
  if (accountLookup.source.notFound || accountLookup.destination.notFound) return false
  if (accountLookup.source.lookupError || accountLookup.destination.lookupError) return false
  if (!accountLookup.source.verified || !accountLookup.destination.verified) return false
  if (!amountRaw || !amountPattern.test(amountRaw)) return false
  if (Number.isNaN(amountNum) || amountNum <= 0 || amountNum >= 1000000) return false
  if (!currency) return false
  if (reference.length > 128) return false
  return true
})
function close() {
  emit('update:modelValue', false)
}
function resetForm() {
  Object.assign(form, defaultForm())
  amountTouched.value = false
  formRef.value?.clearValidate()
  filteredCurrencyOptions.value = store.currencyOptions
  resetAccountLookup(accountLookup.source)
  resetAccountLookup(accountLookup.destination)
}
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      resetForm()
    }
  },
)
async function handleSubmit() {
  if (accountLookup.source.loading || accountLookup.destination.loading) {
    ElMessage.warning('Please wait for account verification to complete')
    return
  }
  if (accountLookup.source.notFound || accountLookup.destination.notFound) {
    ElMessage.error('Please provide valid source and destination account IDs')
    return
  }
  if (accountLookup.source.lookupError || accountLookup.destination.lookupError) {
    ElMessage.error('Account verification failed, please try again')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await createPayment({
      paymentNo: form.paymentNo,
      sourceAccountId: form.sourceAccountId,
      destinationAccountId: form.destinationAccountId,
      amount: form.amount,
      currency: form.currency,
      reference: form.reference || undefined,
    })
    if (res.code === 'SUCCESS') {
      const detail = res.data
      if (detail.status === 'COMPLETED') {
        ElMessage.success('Payment created successfully')
      } else if (detail.status === 'FAILED') {
        ElMessage.warning(normalizePaymentErrorMessage(detail.failureMessage || 'Payment processing failed', detail.failureCode))
      }
      close()
      emit('success', detail)
    } else {
      ElMessage.error(normalizePaymentErrorMessage(res.message || 'Failed to create payment', res.code))
    }
  } catch (error) {
  } finally {
    submitting.value = false
  }
}
onMounted(() => {
  store.loadCurrencyOptions()
})

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

// ----------------------------------------------------------------
// Account name lookup: resolve and display the account name (or a
// "not found" hint) as the user types a Source/Destination Account ID.
// ----------------------------------------------------------------
function createAccountLookupState() {
  return { loading: false, name: '', notFound: false, lookupError: false, requestId: 0, verified: false }
}
const accountLookup = reactive({
  source: createAccountLookupState(),
  destination: createAccountLookupState(),
})

function resetAccountLookup(state) {
  state.loading = false
  state.name = ''
  state.notFound = false
  state.lookupError = false
  state.verified = false
}

async function lookupAccount(state, accountId) {
  if (!accountId) {
    resetAccountLookup(state)
    return
  }
  const requestId = ++state.requestId
  state.loading = true
  state.notFound = false
  state.lookupError = false
  state.verified = false
  try {
    const res = await getAccountById(accountId)
    if (requestId !== state.requestId) return // A newer lookup has since started; discard this stale result.
    if (res.code === 'SUCCESS' && res.data) {
      state.name = res.data.name || ''
      state.notFound = false
      state.lookupError = false
      state.verified = true
    } else if (res.code === 'ACCOUNT_NOT_FOUND') {
      state.name = ''
      state.notFound = true
      state.lookupError = false
      state.verified = false
    } else {
      state.name = ''
      state.notFound = false
      state.lookupError = true
      state.verified = false
    }
  } catch (error) {
    if (requestId !== state.requestId) return
    const code = String(error?.response?.data?.code || '').toUpperCase()
    state.name = ''
    state.notFound = code === 'ACCOUNT_NOT_FOUND'
    state.lookupError = code !== 'ACCOUNT_NOT_FOUND'
    state.verified = false
  } finally {
    if (requestId === state.requestId) {
      state.loading = false
    }
  }
}

/**
 * Debounce helper so we don't fire a request on every keystroke.
 */
function debounce(fn, delay) {
  let timer = null
  const debounced = (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
  debounced.cancel = () => clearTimeout(timer)
  return debounced
}

const debouncedLookupSource = debounce((id) => lookupAccount(accountLookup.source, id), 400)
const debouncedLookupDestination = debounce((id) => lookupAccount(accountLookup.destination, id), 400)

watch(
  () => form.sourceAccountId,
  (id) => {
    resetAccountLookup(accountLookup.source)
    debouncedLookupSource(id)
  },
)
watch(
  () => form.destinationAccountId,
  (id) => {
    resetAccountLookup(accountLookup.destination)
    debouncedLookupDestination(id)
  },
)

onBeforeUnmount(() => {
  debouncedLookupSource.cancel()
  debouncedLookupDestination.cancel()
})
</script>
<template>
  <el-dialog
    :model-value="modelValue"
    title="Create Payment"
    width="520px"
    class="payment-create-dialog"
    align-center
    @update:model-value="(val) => emit('update:modelValue', val)"
  >
    <div class="dialog-intro">
      <div class="dialog-intro-title">Create a new payment request</div>
      <div class="dialog-intro-subtitle">The order number is auto-generated. Fill in account details to submit the payment.</div>
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" class="payment-form">
      <el-form-item label="Source ID" prop="sourceAccountId">
        <el-input-number v-model="form.sourceAccountId" :min="1" :controls="false" class="full-width-input" placeholder="Enter the source account ID" />
        <div v-if="accountLookup.source.loading" class="account-hint account-hint--loading">Looking up account…</div>
        <div v-else-if="accountLookup.source.notFound" class="account-hint account-hint--error">Account not found</div>
        <div v-else-if="accountLookup.source.lookupError" class="account-hint account-hint--error">Unable to verify account now</div>
        <div v-else-if="accountLookup.source.name" class="account-hint account-hint--success">{{ accountLookup.source.name }}</div>
      </el-form-item>
      <el-form-item label="Destination ID" prop="destinationAccountId">
        <el-input-number v-model="form.destinationAccountId" :min="1" :controls="false" class="full-width-input" placeholder="Enter the destination account ID" />
        <div v-if="accountLookup.destination.loading" class="account-hint account-hint--loading">Looking up account…</div>
        <div v-else-if="accountLookup.destination.notFound" class="account-hint account-hint--error">Account not found</div>
        <div v-else-if="accountLookup.destination.lookupError" class="account-hint account-hint--error">Unable to verify account now</div>
        <div v-else-if="accountLookup.destination.name" class="account-hint account-hint--success">{{ accountLookup.destination.name }}</div>
      </el-form-item>
      <el-form-item label="Amount" prop="amount" :show-message="false">
        <el-input
          v-model="form.amount"
          class="full-width-input"
          placeholder="Enter an amount"
          clearable
          inputmode="decimal"
          @blur="handleAmountBlur"
        />
        <div v-if="amountRealtimeError" class="account-hint account-hint--error">{{ amountRealtimeError }}</div>
      </el-form-item>
      <el-form-item label="Currency" prop="currency">
        <el-select
          v-model="form.currency"
          class="full-width-input"
          placeholder="Type or select a currency"
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
      <el-form-item label="Reference" prop="reference">
        <el-input v-model="form.reference" type="textarea" :rows="3" maxlength="128" show-word-limit placeholder="Optional, max 128 characters" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer-actions">
        <el-button class="footer-button secondary" @click="close">Cancel</el-button>
        <el-button
          class="footer-button primary"
          type="primary"
          :loading="submitting"
          :disabled="!isFormSubmittable"
          @click="handleSubmit"
        >
          Submit
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>
<style scoped>
.payment-create-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
}
.payment-create-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
.payment-create-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.payment-create-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 8px;
}
.payment-create-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}
.dialog-intro {
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(16, 185, 129, 0.05));
}
.dialog-intro-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}
.dialog-intro-subtitle {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.payment-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.payment-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.full-width-input {
  width: 100%;
}
.payment-form :deep(.el-input__wrapper),
.payment-form :deep(.el-select__wrapper),
.payment-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.12);
}
.payment-form :deep(.el-textarea__inner) {
  min-height: 92px;
}
/* Element Plus centers el-input-number text by default; align it left with other inputs. */
.payment-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
}
.account-hint {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
}
.account-hint--loading {
  color: var(--el-text-color-secondary);
}
.account-hint--success {
  color: var(--el-color-success);
  font-weight: 600;
}
.account-hint--error {
  color: var(--el-color-danger);
  font-weight: 600;
}
.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.footer-button {
  min-width: 92px;
  height: 40px;
  border-radius: 999px;
}
.footer-button.primary {
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.24);
}
.footer-button.secondary {
  border-color: rgba(148, 163, 184, 0.28);
}
</style>
