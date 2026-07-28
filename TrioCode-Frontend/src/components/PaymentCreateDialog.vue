<script setup>
/**
 * 新建支付弹窗
 * 提交后端同步全流程接口 POST /api/v1/payments，
 * 根据返回的最终 status（COMPLETED / FAILED）分别提示，并通知父组件弹出时间线
 */
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createPayment } from '@/api/payment'
import { usePaymentStore } from '@/store/payment'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'success'])

const store = usePaymentStore()

const formRef = ref(null)
const submitting = ref(false)

const defaultForm = () => ({
  paymentNo: '',
  sourceAccountId: undefined,
  destinationAccountId: undefined,
  amount: undefined,
  currency: '',
  reference: '',
})

const form = reactive(defaultForm())

/**
 * 校验规则
 */
const rules = {
  paymentNo: [
    { required: true, message: '请输入订单号', trigger: 'blur' },
    { max: 32, message: '订单号长度不能超过32位', trigger: 'blur' },
  ],
  sourceAccountId: [
    { required: true, message: '请输入付款账户ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '付款账户ID必须为正数', trigger: 'blur' },
  ],
  destinationAccountId: [
    { required: true, message: '请输入收款账户ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '收款账户ID必须为正数', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && form.sourceAccountId && value === form.sourceAccountId) {
          callback(new Error('收款账户不能与付款账户相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === undefined || value === null || value === '') {
          callback(new Error('请输入金额'))
          return
        }
        const num = Number(value)
        if (Number.isNaN(num) || num <= 0) {
          callback(new Error('金额必须大于0'))
        } else if (num >= 1000000) {
          callback(new Error('金额必须小于1000000'))
        } else if (!/^\d+(\.\d{1,2})?$/.test(String(value))) {
          callback(new Error('金额最多保留2位小数'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  reference: [{ max: 128, message: '备注长度不能超过128位', trigger: 'blur' }],
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
        ElMessage.success('支付创建成功')
      } else if (detail.status === 'FAILED') {
        ElMessage.warning(detail.failureMessage || '支付处理失败')
      }
      close()
      emit('success', detail)
    } else {
      // 业务错误：DUPLICATE_PAYMENT / INSUFFICIENT_FUNDS 等，保留弹窗方便用户修改后重试
      ElMessage.error(res.message || '创建支付失败')
    }
  } catch (error) {
    // 网络错误已由 request.js 统一提示，弹窗保持打开
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
    title="新建支付"
    width="520px"
    @update:model-value="(val) => emit('update:modelValue', val)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="订单号" prop="paymentNo">
        <el-input v-model="form.paymentNo" placeholder="请输入订单号，长度不超过32位" maxlength="32" />
      </el-form-item>
      <el-form-item label="付款账户ID" prop="sourceAccountId">
        <el-input-number v-model="form.sourceAccountId" :min="1" :controls="false" style="width: 100%" placeholder="请输入付款账户ID" />
      </el-form-item>
      <el-form-item label="收款账户ID" prop="destinationAccountId">
        <el-input-number v-model="form.destinationAccountId" :min="1" :controls="false" style="width: 100%" placeholder="请输入收款账户ID" />
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number
          v-model="form.amount"
          :min="0.01"
          :max="999999.99"
          :precision="2"
          :step="0.01"
          :controls="false"
          style="width: 100%"
          placeholder="请输入金额，最多2位小数"
        />
      </el-form-item>
      <el-form-item label="币种" prop="currency">
        <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
          <el-option
            v-for="item in store.currencyOptions"
            :key="item.code"
            :label="`${item.code} - ${item.codeName}`"
            :value="item.code"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="reference">
        <el-input v-model="form.reference" type="textarea" :rows="2" maxlength="128" show-word-limit placeholder="选填，长度不超过128位" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>
