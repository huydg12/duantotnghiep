<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import Banner from "../common/Banner.vue";
import axios from 'axios'
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
const store = useCartFavoriteStore()
const route = useRoute()
const router = useRouter()
const showToast = ref(false)
const quantity = ref(1)
const selectedProduct = ref({})
const selectedImage = ref('')
const productVariants = ref([])
const availableSizes = ref([])

const cartId = ref(null)
let customerId = null;
const userJson = localStorage.getItem("user");

if (userJson) {
  try {
    const user = JSON.parse(userJson);
    customerId = user.customerId;
    console.log("✅ Customer ID:", customerId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
} else {
  console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user");
}

const findCartIdByCustomerId = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/cart/getCartId/${customerId}`)
    console.log("📦 cartId trả về:", response.data)
    cartId.value = response.data
  } catch (error) {
    console.error("❌ Lỗi khi lấy cartId:", error)
  }
}



const addToCart = async () => {
  try {
    const payload = {
      cartId: cartId.value, // Sử dụng cartId đã lấy từ API
      productDetailId: selectedProduct.value.productDetailId,
      quantity: quantity.value
    }
        console.log("📦 Payload gửi lên /cartDetail/add:", payload)

    // Kiểm tra từng phần tử riêng biệt
    console.log("🆔 cartId:", cartId.value)
    console.log("🛒 productDetailId:", selectedProduct.value?.productDetailId)
    console.log("🔢 quantity:", quantity.value)

    // Kiểm tra có dữ liệu nào là undefined/null không
    if (!payload.cartId || !payload.productDetailId || !payload.quantity || payload.quantity <= 0) {
      alert("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.")
      return
    }
    // Gọi API kiểm tra xem productDetail đã có trong giỏ chưa
    const checkUrl = `http://localhost:8080/cartDetail/exists?cartId=${payload.cartId}&productDetailId=${payload.productDetailId}`
    const checkResponse = await axios.get(checkUrl)


  if (checkResponse.data === true) {
      // Đã tồn tại → cập nhật số lượng mới
      console.log("🔍 checkResponse.data:", checkResponse.data)
      const updatePayload = {
        cartId: payload.cartId,
        productDetailId: payload.productDetailId,
        quantity: payload.quantity // số lượng muốn thêm
      }
      await axios.put('http://localhost:8080/cartDetail/updateQuantity', updatePayload)
      console.log("✅ Đã cập nhật số lượng trong giỏ")
      // ✅ Gọi lại hàm fetchProductDetail sau khi cập nhật
      await fetchProductDetail()
    } else {
      // Chưa tồn tại → thêm mới
      await axios.post('http://localhost:8080/cartDetail/add', payload)
      console.log("✅ Đã thêm mới vào giỏ hàng")
      await fetchProductDetail();
      await store.fetchCartItems(customerId);


    }
    showToast.value = true
    setTimeout(() => {
      showToast.value = false
    }, 3000)
  } catch (error) {
    console.error("❌ Lỗi khi thêm vào giỏ hàng:", error)
    alert("Thêm vào giỏ hàng thất bại.")
  }
}



const sizeList = ref(['35', '36', '36.5', '37', '37.5', '38', '38.5', '39', '40', '40.5', '41',
 '42', '42.5', '43', '44', '44.5', '45', '46','47', '48'])

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
  selectedImage.value = (variant.images && variant.images[0]) || ''
    console.log('Variant.images:', variant.images)

  // Tìm tất cả size có sẵn cho màu hiện tại
  const sizesForColor = productVariants.value
    .filter(p => p.color === variant.color)
    .map(p => p.size)
  availableSizes.value = [...new Set(sizesForColor)]
}

const fetchProductDetail = async () => {
  const id = route.params.id

  try {
    // Gọi API getById để kiểm tra xem id là productDetail hay không
    const detailRes = await axios.get(`http://localhost:8080/productDetail/findProductId/${id}`)
    console.log("API trả về productDetailId:", detailRes.data)
    let productId

      // Nếu detailRes.data là số → chính là productId
      if (typeof detailRes.data === "number") {
        productId = detailRes.data
      } else if (detailRes.data && detailRes.data.productId) {
        productId = detailRes.data.productId
      } else {
        productId = id
      }

      console.log("productId sau xử lý: ", productId)

    // Gọi API để lấy tất cả phiên bản sản phẩm theo productId
    const response = await axios.get(`http://localhost:8080/productDetail/show/${productId}`)
    console.log("Dữ liệu API trả về: ", response.data)

    if (Array.isArray(response.data) && response.data.length > 0) {
      const processedVariants = response.data.map(variant => ({
        ...variant,
        images: Array.isArray(variant.images) ? variant.images : [],
      }))

      productVariants.value = processedVariants
      console.log("Dữ liệu đã xử lý:", processedVariants)

      // Nếu gọi từ cart và có productDetailId, chọn đúng productDetail đó
      const selected = processedVariants.find(v => v.productDetailId == id) || processedVariants[0]
      updateSelection(selected)
    } else {
      console.error("API không trả về dữ liệu sản phẩm hợp lệ.")
    }
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm', error)
  }
}

const buyNow = () => {
  const checkoutItem = {
    productDetailId: selectedProduct.value.productDetailId,
    productName: selectedProduct.value.productName,
    brandName: selectedProduct.value.brandName,
    color: selectedProduct.value.color,
    size: selectedProduct.value.size,
    price: selectedProduct.value.price,
    quantity: quantity.value,
    images: selectedProduct.value.images?.[0] || ""
  }

  sessionStorage.setItem("checkoutItems", JSON.stringify([checkoutItem]))
  router.push("/payment")
}

const selectImage = (img) => {
  selectedImage.value = img
}

onMounted(() => {
  fetchProductDetail()
  findCartIdByCustomerId() // <-- THÊM DÒNG NÀY
})
</script>

<template>
  <Banner title="" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container bg-white rounded-4 shadow p-4" v-if="selectedProduct.productDetailId">
    <div class="row g-4 align-items-start">
<div class="col-md-6 position-relative">
      <!-- Ảnh chính -->
      <img
        :src="selectedImage.startsWith('./') ? selectedImage.replace('./', '/') : selectedImage"
        alt="Sản phẩm"
        class="w-100 rounded shadow-sm border border-light"
        style="max-height: 400px; object-fit: contain;"
      />

      <!-- Danh sách ảnh phụ -->
      <div class="d-flex gap-2 mt-3 overflow-auto">
        <img
          v-for="(img, index) in selectedProduct.images"
          :key="index"
          :src="img.startsWith('./') ? img.replace('./', '/') : img"
          alt="Ảnh phụ"
          @click="selectImage(img)"
          class="img-thumbnail border border-2"
          :class="{ 'border-primary': selectedImage === img }"
          style="width: 80px; height: 80px; object-fit: cover; cursor: pointer;"
        />
      </div>
    </div>

      <div class="col-md-6">
        <h2 class="fw-semibold mb-2">{{ selectedProduct.productName }}</h2>
        <p class="text-muted mb-2">Thương hiệu: <strong>{{ selectedProduct.brandName }}</strong> | Mã phiên bản: {{
          selectedProduct.productDetailId }}</p>
        <p class="text-danger fs-4 fw-bold mb-4">{{ selectedProduct.price }}₫</p>

        <div class="mb-3">
          Màu sắc:
          <button v-for="color in uniqueColors" :key="color" @click="selectColor(color)" class="color-btn"
            :class="{ active: selectedProduct.color === color }">
            {{ color }}
          </button>
        </div>

          <div class="mb-3">
            Kích thước:
            <button
              v-for="size in sizeList"
              :key="size"
              class="size-btn"
              :class="[
                { active: selectedProduct.size === size },
                { unavailable: !availableSizes.includes(size) }
              ]"
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
          <button class="btn btn-primary product-button fw-semibold" @click="addToCart()">Thêm vào giỏ</button>
          <button class="btn btn-danger product-button fw-semibold" @click="buyNow()">Mua ngay</button>
        </div>

        <hr />
        <p class="text-muted">{{ selectedProduct.descriptionProduct }}</p>
      </div>
    </div>
  </div>
  <!-- Toast thông báo thêm vào giỏ thành công -->
  <div
    v-if="showToast"
    class="position-fixed top-0 end-0 p-3"
    style="z-index: 1055;"
  >
    <div class="toast align-items-center show bg-success text-white border-0">
      <div class="d-flex">
        <div class="toast-body">
          ✅ Đã thêm sản phẩm vào giỏ hàng!
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          @click="showToast = false"
        ></button>
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
.toast {
  animation: slideIn 0.5s ease-out, fadeOut 0.5s ease-in 2.5s forwards;
  min-width: 250px;
  max-width: 300px;
  border-radius: 0.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0%);
    opacity: 1;
  }
}

@keyframes fadeOut {
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>