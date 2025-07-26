<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  total: Number,
  modelValue: Number,
  change: Number
})
const emit = defineEmits(['update:modelValue', 'checkout'])

const updatePaid = (e) => {
  emit('update:modelValue', Number(e.target.value))
}

const checkout = () => {
  emit('checkout')
}
</script>

<template>
  <div class="card mt-2">
    <div class="card-body">
      <div class="mb-2">Tổng tiền: {{ total.toLocaleString() }}đ</div>
      <div class="mb-2">
        <label>Khách đưa:</label>
        <input type="number" class="form-control" :value="modelValue" @input="updatePaid" />
      </div>
      <div class="mb-2">Tiền thừa: {{ change.toLocaleString() }}đ</div>
      <button class="btn btn-success w-100" @click="checkout">Thanh toán</button>
    </div>
  </div>
</template>
