<script setup>
import { ref, computed } from 'vue'
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  customers: Array,
  modelValue: Object
})

const emit = defineEmits(['update:modelValue'])

const searchText = ref('')
const showSuggestions = ref(false)

const filteredCustomers = computed(() => {
  const text = searchText.value.trim().toLowerCase()
  if (!text) {
    showSuggestions.value = false
    return []
  }

  showSuggestions.value = true
  return props.customers.filter(c =>
    c.fullName.toLowerCase().includes(text) ||
    c.numberPhone.includes(text)
  )
})

const selectCustomer = (customer) => {
  emit('update:modelValue', customer)
  searchText.value = `${customer.fullName} - ${customer.numberPhone}`
  showSuggestions.value = false
}
</script>

<template>
  <div class="mb-2 position-relative">
    <label class="form-label">Tìm khách hàng:</label>
    <input type="text" class="form-control" v-model="searchText" placeholder="Nhập tên hoặc SĐT khách hàng"
      @focus="showSuggestions = !!searchText" @input="showSuggestions = !!searchText" />

    <!-- Danh sách gợi ý -->
    <ul v-if="showSuggestions && filteredCustomers.length" class="list-group position-absolute w-100 mt-1"
      style="z-index: 10; max-height: 200px; overflow-y: auto;">
      <li class="list-group-item list-group-item-action" v-for="c in filteredCustomers" :key="c.id"
        @click="selectCustomer(c)" style="cursor: pointer;">
        {{ c.fullName }} - {{ c.numberPhone }}
      </li>
    </ul>
  </div>
</template>
