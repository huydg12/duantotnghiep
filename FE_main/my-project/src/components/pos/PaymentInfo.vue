<script setup>
import { defineProps, defineEmits, ref } from 'vue'

const props = defineProps({
  total: Number,
  modelValue: Number,
  change: Number
})
const emit = defineEmits(['update:modelValue', 'checkout', 'applyPromo'])

const promoCode = ref('')

const updatePaid = (e) => {
  emit('update:modelValue', Number(e.target.value))
}

const checkout = () => {
  emit('checkout')
}

const applyPromo = () => {
  emit('applyPromo', promoCode.value.trim())
}
</script>

<template>
  <div class="card mt-2 shadow-sm border-0">
    <div class="card-body">
      <div class="mb-3 d-flex justify-content-between">
        <span class="fw-medium">Tổng tiền:</span>
        <span class="fw-semibold text-danger fs-5">{{ total.toLocaleString() }}đ</span>
      </div>

      <div class="mb-3">
        <label class="form-label fw-medium">Mã khuyến mãi</label>
        <div class="input-group">
          <input
            type="text"
            class="form-control"
            v-model="promoCode"
            placeholder="Nhập mã giảm giá"
          />
          <button class="btn btn-outline-primary" @click="applyPromo">Áp dụng</button>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label fw-medium">Khách đưa</label>
        <input
          type="number"
          class="form-control"
          :value="modelValue"
          @input="updatePaid"
          placeholder="Nhập số tiền khách đưa"
        />
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
