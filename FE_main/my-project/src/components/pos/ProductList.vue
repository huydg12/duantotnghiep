<script setup>
import { ref, computed } from 'vue'

const props = defineProps(['products'])
defineEmits(['select'])

const searchTerm = ref('')

const filteredProducts = computed(() => {
    if (!searchTerm.value.trim()) return props.products
    const lowerSearch = searchTerm.value.toLowerCase()
    return props.products.filter(p =>
        p.productName.toLowerCase().includes(lowerSearch) ||
        (p.brandName && p.brandName.toLowerCase().includes(lowerSearch))
    )
})
</script>

<template>
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <input type="text" class="form-control form-control-sm w-50" placeholder="Tìm sản phẩm..."
                v-model="searchTerm" />
        </div>
        <div class="card-body" style="max-height: 500px; overflow-y: auto;">
            <div class="row g-3">
                <div v-for="product in filteredProducts" :key="product.id" class="col-6 col-md-4"
                    @click="$emit('select', product)" style="cursor: pointer;">
                    <div class="card h-100">
                        <div class="card-body d-flex flex-column justify-content-center">
                            <h6 class="card-title text-truncate text-center" :title="product.productName">{{
                                product.productName }}
                            </h6>
                            <small class="text-muted">{{ product.brandName }}</small>
                        </div>
                    </div>
                </div>
            </div>
            <div v-if="filteredProducts.length === 0" class="text-center text-muted mt-3">
                Không tìm thấy sản phẩm
            </div>
        </div>
    </div>
</template>
