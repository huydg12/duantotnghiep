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
