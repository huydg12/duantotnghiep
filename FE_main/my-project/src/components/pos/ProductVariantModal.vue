<template>
  <div class="modal d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.4)">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Chọn biến thể: {{ props.product?.productName }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <img :src="selectedImage" class="img-fluid rounded border" alt="Ảnh sản phẩm" />
            </div>
            <div class="col-md-6">
              <div class="mb-3">
                <label class="form-label fw-bold">Màu sắc:</label>
                <div>
                  <button v-for="color in uniqueColors" :key="color" class="btn btn-outline-dark btn-sm me-2 mb-2"
                    :class="{ active: selectedColor === color }" @click="selectColor(color)">
                    {{ color }}
                  </button>
                </div>
              </div>

              <div v-if="selectedColor" class="mb-3">
                <label class="form-label fw-bold">Kích cỡ:</label>
                <div>
                  <button v-for="size in sizesForSelectedColor" :key="size"
                    class="btn btn-outline-primary btn-sm me-2 mb-2" :class="{ active: selectedSize === size }"
                    @click="selectSize(size)">
                    {{ size }}
                  </button>
                </div>
              </div>

              <div v-if="selectedVariant" class="mb-3">
                <p>Giá: <strong>{{ selectedVariant.price.toLocaleString('vi-VN') }} đ</strong></p>
                <p>Tồn kho: <strong>{{ selectedVariant.quantity }}</strong></p>
                <div class="mb-3">
                  <label class="form-label">Số lượng:</label>
                  <input type="number" class="form-control" v-model.number="quantity" min="1"
                    :max="selectedVariant.quantity" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-success"
            :disabled="!selectedVariant || quantity < 1 || quantity > selectedVariant.quantity" @click="() => {
              console.log('Sản phẩm được thêm:', { ...selectedVariant, quantity });
              $emit('confirm', { ...selectedVariant, quantity });
            }">
            Thêm vào giỏ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const props = defineProps({
  product: Object,
  variants: Array,
})

defineEmits(['close', 'confirm'])

const selectedColor = ref(null)
const selectedSize = ref(null)
const quantity = ref(1)

const uniqueColors = computed(() => [...new Set(props.variants.map(v => v.color))])

const sizesForSelectedColor = computed(() => {
  return props.variants
    .filter(v => v.color === selectedColor.value)
    .map(v => v.size)
})

const selectedVariant = computed(() => {
  return props.variants.find(
    v => v.color === selectedColor.value && v.size === selectedSize.value
  )
})

const selectedImage = computed(() => {
  // Nếu đã chọn đủ màu + size → lấy ảnh của variant
  if (selectedVariant.value?.images?.[0]) {
    return selectedVariant.value.images[0]
  }
  // Nếu mới chọn màu → lấy ảnh của bất kỳ variant nào có màu đó
  const variantWithColor = props.variants.find(v => v.color === selectedColor.value)
  if (variantWithColor?.images?.[0]) {
    return variantWithColor.images[0]
  }
  // Fallback ảnh sản phẩm gốc
  return props.product?.images?.[0] || ''
})


function selectColor(color) {
  selectedColor.value = color
  selectedSize.value = null
  quantity.value = 1
}

function selectSize(size) {
  selectedSize.value = size
  quantity.value = 1
}

watch(selectedColor, (newColor) => {
  const sizes = Array.from(
    new Set(props.variants.filter(v => v.color === newColor).map(v => v.size))
  )
  if (sizes.length > 0) {
    selectedSize.value = sizes[0] // Tự động chọn size đầu tiên
  }
})

watch(() => [props.product, props.variants], ([newProduct, newVariants]) => {
  if (newVariants.length > 0) {
    const first = newVariants[0]
    selectedColor.value = first.color
    selectedSize.value = first.size
    quantity.value = 1
  } else {
    selectedColor.value = null
    selectedSize.value = null
    quantity.value = 1
  }
}, { immediate: true })


</script>

<style scoped>
button.active {
  border-color: #000;
  background-color: #000;
  color: #fff;
}
</style>
