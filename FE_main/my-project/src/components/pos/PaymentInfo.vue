<script setup>
import { defineProps, defineEmits, ref } from 'vue'

const props = defineProps({
  total: Number,
  modelValue: Number,
  change: Number
})
const emit = defineEmits(['update:modelValue', 'checkout', 'applyPromo'])

const updatePaid = (e) => {
  emit('update:modelValue', Number(e.target.value))
}

const checkout = () => {
  emit('checkout')
}

</script>

<template>
  <div class="card mt-2 shadow-sm border-0">
    <div class="card-body">
      <div class="mb-3">
        <label class="form-label fw-medium">Khách đưa</label>
        <input type="number" class="form-control" :value="modelValue" @input="updatePaid"
          placeholder="Nhập số tiền khách đưa" />
      </div>

      <div class="mb-3 d-flex justify-content-between">
        <span class="fw-medium">Tiền thừa:</span>
        <span class="fw-semibold text-success">{{ change.toLocaleString() }}đ</span>
      </div>

      <button class="btn btn-success w-100 fw-semibold" @click="checkout">
        <i class="bi bi-cash-stack me-2"></i>Thanh toán
      </button>
    </div>
  </div>
</template>

<style scoped>
input::placeholder {
  color: #aaa;
}
</style>
