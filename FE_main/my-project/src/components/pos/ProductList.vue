<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps(['products'])
defineEmits(['select'])

const searchTerm = ref('')
const showDropdown = ref(false)

const filteredProducts = computed(() => {
    if (!searchTerm.value.trim()) return []
    const lowerSearch = searchTerm.value.toLowerCase()
    return props.products.filter(p =>
        p.productName.toLowerCase().includes(lowerSearch) ||
        (p.brandName && p.brandName.toLowerCase().includes(lowerSearch))
    )
})

// Ẩn dropdown khi click ra ngoài
const dropdownRef = ref(null)
const handleClickOutside = (event) => {
    if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
        showDropdown.value = false
    }
}
onMounted(() => {
    document.addEventListener('click', handleClickOutside)
})
</script>

<template>
    <div class="position-relative" ref="dropdownRef">
        <!-- Thanh tìm kiếm -->
        <input type="text" class="form-control form-control-sm" placeholder="Tìm sản phẩm..." v-model="searchTerm"
            @focus="showDropdown = true" />

        <!-- Gợi ý tìm kiếm (dropdown overlay) -->
        <div v-if="showDropdown && searchTerm" class="position-absolute bg-white border rounded shadow-sm mt-1 p-2"
            style="z-index: 1000; width: 100%; max-height: 300px; overflow-y: auto;">
            <div v-for="product in filteredProducts" :key="product.id" class="p-2 hover-bg" style="cursor: pointer;"
                @click="$emit('select', product); showDropdown = false; searchTerm = ''">
                <strong>{{ product.productName }}</strong><br />
                <small class="text-muted">{{ product.brandName }}</small>
            </div>

            <div v-if="filteredProducts.length === 0" class="text-muted small text-center mt-2">
                Không tìm thấy sản phẩm
            </div>
        </div>
    </div>
</template>

<style scoped>
.hover-bg:hover {
    background-color: #f8f9fa;
}
</style>
