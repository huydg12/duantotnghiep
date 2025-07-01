<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import Banner from './Banner.vue'
import axios from 'axios'

const route = useRoute()

const quantity = ref(1)
const selectedProduct = ref({})
const selectedImage = ref('')
const productVariants = ref([])
const availableSizes = ref([])

const sizeList = ref(['33', '34', '35', '36', '37', '38', '39', '40', '41', '42', '43', '44', '45'])

// Danh sách các màu là duy nhất
const uniqueColors = computed(() => {
  const colors = productVariants.value.map(p => p.color)
  return [...new Set(colors)]
})


const selectColor = (color) => {
  // Tìm sản phẩm đầu tiên có màu này để làm lựa chọn mặc định
  const firstVariantOfColor = productVariants.value.find(p => p.color === color)
  if (firstVariantOfColor) {
    updateSelection(firstVariantOfColor)
  }
}

const selectSize = (size) => {
  const newSelectedProduct = productVariants.value.find(
    p => p.color === selectedProduct.value.color && p.size === size
  )
  if (newSelectedProduct) {
    selectedProduct.value = newSelectedProduct
  }
}

const updateSelection = (variant) => {
  // Cập nhật sản phẩm đang được chọn
  selectedProduct.value = variant

  // Xử lý lại chuỗi ảnh cho phiên bản vừa chọn
  selectedImage.value.images = (variant.images && variant.images[0]) || ''

  // Tìm tất cả size có sẵn cho màu hiện tại
  const sizesForColor = productVariants.value
    .filter(p => p.color === variant.color)
    .map(p => p.size)
  availableSizes.value = [...new Set(sizesForColor)]
}

const fetchProductDetail = async () => {
  const id = route.params.id
  try {
    const response = await axios.get(`http://localhost:8080/productDetail/show/${id}`)
    console.log("Dữ liệu API trả về: ", response.data)

    // Kiểm tra API trả về mảng hợp lệ không
    if (Array.isArray(response.data) && response.data.length > 0) {
      const processedVariants = response.data.map(variant => ({
        ...variant,
        images: typeof variant.images === 'string' ? variant.images.split(',') : [],
      }))
      
      productVariants.value = processedVariants
      updateSelection(processedVariants[0]) // Chọn phiên bản đầu tiên làm mặc định
    } else {
      console.error("API không trả về dữ liệu sản phẩm hợp lệ.")
    }
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm', error)
  }
}

const selectImage = (img) => {
  selectedImage.value = img
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<template>
  <Banner title="" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container bg-white rounded-4 shadow p-4" v-if="selectedProduct.productDetailId">
    <div class="row g-4 align-items-start">
      <div class="col-md-6 position-relative">
        <img :src="selectedImage" alt="Sản phẩm" class="w-100 rounded shadow-sm border border-light"
          style="max-height: 400px; object-fit: contain;" />
        <div class="d-flex gap-2 mt-3 overflow-auto">
          <img v-for="(img, index) in selectedProduct.images" :key="index" :src="img" alt="Ảnh phụ"
            @click="selectImage(img)" class="img-thumbnail border border-2"
            :class="{ 'border-primary': selectedImage === img }"
            style="width: 80px; height: 80px; object-fit: cover; cursor: pointer;" />
        </div>
      </div>

      <div class="col-md-6">
        <h2 class="fw-semibold mb-2">{{ selectedProduct.productName }}</h2>
        <p class="text-muted mb-2">Thương hiệu: <strong>{{ selectedProduct.brandName }}</strong> | Mã phiên bản: {{
          selectedProduct.productDetailId }}</p>
        <p class="text-danger fs-4 fw-bold mb-4">{{ selectedProduct.price }}₫</p>

        <div class="mb-3">
          Màu sắc:
          <button
            v-for="color in uniqueColors"
            :key="color"
            @click="selectColor(color)"
            class="color-btn"
            :class="{ active: selectedProduct.color === color }"
          >
            {{ color }}
          </button>
        </div>

        <div class="mb-3">
          Kích thước:
          <button
            v-for="size in sizeList"
            :key="size"
            class="size-btn"
            :class="{ active: selectedProduct.size === size }"
            :disabled="!availableSizes.includes(size)"
            @click="selectSize(size)"
          >
            {{ size }}
          </button>
        </div>

        <div class="mb-4" style="max-width: 150px;">
          <label class="form-label">Số lượng</label>
          <input type="number" v-model="quantity" min="1" class="form-control">
        </div>

        <div class="d-flex gap-3 mb-4">
          <button class="btn btn-primary product-button fw-semibold">Thêm vào giỏ</button>
          <button class="btn btn-danger product-button fw-semibold">Mua ngay</button>
        </div>

        <hr />
        <p class="text-muted">{{ selectedProduct.descriptionProduct }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-image {
  height: 500px;
  object-fit: cover;
  object-position: center;
}

.image-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(107, 114, 128, 0.6);
  color: white;
  padding: 0.5rem;
  border-radius: 50%;
  border: none;
}

.image-nav-btn:hover {
  background-color: rgba(75, 85, 99, 0.8);
}

.product-button {
  min-width: 120px;
}

.color-btn,
.size-btn {
  border: 1px solid #6c757d;
  font-size: 0.875rem;
  padding: 0.25rem 0.75rem;
  border-radius: 5px;
  margin: 0 0.25rem;
  background-color: white;
}

.color-btn:hover,
.size-btn:hover {
  background-color: #e2e6ea;
}

.size-btn {
  margin: 0 4px;
  border: 1px solid #ccc;
  padding: 6px 12px;
  border-radius: 5px;
  background: white;
  cursor: pointer;
}

.color-btn.active,
.size-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.size-btn:disabled {
  background-color: #f8f9fa;
  color: #adb5bd;
  cursor: not-allowed;
  border-color: #dee2e6;
}
</style>