<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  customers: Array,
  modelValue: Object
})

const emit = defineEmits(['update:modelValue'])

const selectCustomer = (e) => {
  const id = Number(e.target.value)
  const selected = props.customers.find(c => c.id === id)
  emit('update:modelValue', selected)
}
</script>

<template>
  <div class="mb-2">
    <label class="form-label">Chọn khách hàng:</label>
    <select class="form-select" @change="selectCustomer" :value="modelValue?.id || ''">
      <option value="">-- Chọn khách hàng --</option>
      <option v-for="c in customers" :key="c.id" :value="c.id">
        {{ c.fullName }} - {{ c.numberPhone }}
      </option>
    </select>
  </div>
</template>
