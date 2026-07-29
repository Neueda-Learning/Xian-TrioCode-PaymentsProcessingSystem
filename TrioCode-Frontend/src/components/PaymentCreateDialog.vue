<script setup>
/**
 * Create payment dialog.
 * Submits the synchronous flow POST /api/v1/payments,
 * shows the final status (COMPLETED / FAILED), and notifies the parent to open the timeline.
 */
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayment } from '@/api/payment'
import { usePaymentStore } from '@/store/payment'
import { normalizePaymentErrorMessage } from '@/utils/paymentError'
const props = defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'success'])
const store = usePaymentStore()
const formRef = ref(null)
const submitting = ref(false)
let lastTimestamp = 0
let sequence = 0

function nextSequence(nowTimestamp) {
  if (nowTimestamp === lastTimestamp) {
    sequence += 1
  } else {
    lastTimestamp = nowTimestamp
    sequence = 0
  }
  return String(sequence).padStart(4, '0')
}

function generatePaymentNo() {
  const nowTimestamp = Date.now()
  const sequencePart = nextSequence(nowTimestamp)
  return `PAY${nowTimestamp}${sequencePart}`
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
        if (value === undefined || value === null || value === '') {
          callback(new Error('Please enter an amount'))
          return
        }
        const num = Number(value)
        if (Number.isNaN(num) || num <= 0) {
          callback(new Error('The amount must be greater than 0'))
        } else if (num >= 1000000) {
          callback(new Error('The amount must be less than 1,000,000'))
        } else if (!/^\d+(\.\d{1,2})?$/.test(String(value))) {
          callback(new Error('The amount can have at most 2 decimal places'))
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
function close() {
  emit('update:modelValue', false)
}
function resetForm() {
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
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
      </el-form-item>
      <el-form-item label="Destination ID" prop="destinationAccountId">
        <el-input-number v-model="form.destinationAccountId" :min="1" :controls="false" class="full-width-input" placeholder="Enter the destination account ID" />
      </el-form-item>
      <el-form-item label="Amount" prop="amount">
        <el-input-number
          v-model="form.amount"
          :min="0.01"
          :max="999999.99"
          :precision="2"
          :step="0.01"
          :controls="false"
          class="full-width-input"
          placeholder="Enter an amount (up to 2 decimal places)"
        />
      </el-form-item>
      <el-form-item label="Currency" prop="currency">
        <el-select v-model="form.currency" class="full-width-input" placeholder="Select a currency">
          <el-option
            v-for="item in store.currencyOptions"
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
        <el-button class="footer-button primary" type="primary" :loading="submitting" @click="handleSubmit">Submit</el-button>
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
