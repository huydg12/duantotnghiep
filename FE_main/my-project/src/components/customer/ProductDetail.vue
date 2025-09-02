<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Banner from "../common/Banner.vue";
import axios from 'axios'
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
import Swal from 'sweetalert2'
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

// === Size guide modal ===
const showSizeGuide = ref(false)
const sizeChartUrl = '/images/size-chart.png'
const openSizeGuide = () => (showSizeGuide.value = true)
const closeSizeGuide = () => (showSizeGuide.value = false)

if (userJson) {
  try {
    const user = JSON.parse(userJson);
    customerId = user.customerId;
    console.log("✅ Customer ID:", customerId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
}

const findCartIdByCustomerId = async () => {
  if (!customerId) {
    console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user")
    return;
  }

  try {
    const response = await axios.get(`http://localhost:8080/cart/getCartId/${customerId}`)
    console.log("📦 cartId trả về:", response.data)
    cartId.value = response.data
  } catch (error) {
    console.error("❌ Lỗi khi lấy cartId:", error)
  }
}

function validateQuantity() {
  const max = selectedProduct.value.quantity || 0

  if (quantity.value < 1) {
    quantity.value = 1
  } else if (quantity.value > max) {
    quantity.value = max
  }
}

function blockMinus(e) {
  if (e.key === '-' || e.key === 'e' || e.key === '+') {
    e.preventDefault()
  }
}

const addToCart = async () => {
  if (!customerId) {
    await Swal.fire({
      icon: 'warning',
      title: 'Bạn cần đăng nhập',
      text: 'Vui lòng đăng nhập để thêm vào giỏ hàng.',
      confirmButtonText: 'OK',
      didOpen: () => { Swal.getContainer().style.zIndex = '20000'; }
    });
    return;
  }

  try {
    const payload = {
      cartId: cartId.value, // Sử dụng cartId đã lấy từ API
      productDetailId: selectedProduct.value.productDetailId,
      quantity: quantity.value,
      images: selectedProduct.value.images
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
        quantity: payload.quantity, // số lượng muốn thêm
        images: payload.images

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
    await store.fetchCartItems(customerId); // Gọi lại để cập nhật giỏ hàng
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
  '42', '42.5', '43', '44', '44.5', '45', '46', '47', '48'])

// Danh sách các màu là duy nhất
const uniqueColors = computed(() => {
  const colors = productVariants.value.map(p => p.color)
  return [...new Set(colors)]
})

const selectColor = (color) => {
  const variantsOfColor = productVariants.value.filter(p => p.color === color);
  if (!variantsOfColor.length) return;

  // lấy cổ đầu tiên cho màu này
  const firstCollar = (variantsOfColor.find(v => (v.collar ?? v.colar))?.collar) ??
                      (variantsOfColor.find(v => (v.collar ?? v.colar))?.colar) ?? '';
  selectedCollar.value = firstCollar;

  // chọn 1 biến thể khớp màu + cổ
  const firstVariant = variantsOfColor.find(v => (v.collar ?? v.colar) === selectedCollar.value) || variantsOfColor[0];
  updateSelection(firstVariant);
};

const selectSize = (size) => {
  const newSelectedProduct = productVariants.value.find(
    p =>
      p.color === selectedProduct.value.color &&
      (p.collar ?? p.colar) === selectedCollar.value &&
      p.size === size
  );
  if (newSelectedProduct) {
    updateSelection(newSelectedProduct);
  }
};
// thêm vào phần khai báo ref bên trên
const selectedCollar = ref('');
const collarsForCurrentColor = computed(() => {
  const color = selectedProduct.value?.color;
  const set = new Set(
    productVariants.value
      .filter(v => v.color === color)
      .map(v => (v.collar ?? v.colar))
      .filter(Boolean)
  );
  return [...set];
});
const selectCollar = (collar) => {
  selectedCollar.value = collar;
  const prevSize = selectedProduct.value?.size;

  let v = productVariants.value.find(p =>
    p.color === selectedProduct.value.color &&
    (p.collar ?? p.colar) === collar &&
    p.size === prevSize
  );
  if (!v) {
    v = productVariants.value.find(p =>
      p.color === selectedProduct.value.color &&
      (p.collar ?? p.colar) === collar
    );
  }
  if (v) updateSelection(v);
};
const updateSelection = (variant) => {
  // chuẩn hoá collar/colar
  const collarVal = variant.collar ?? variant.colar ?? '';
  selectedProduct.value = { ...variant, collar: collarVal, colar: collarVal };

  // ảnh chính
  selectedImage.value = (variant.images && variant.images[0]) || '';

  // set cổ đang chọn
  selectedCollar.value = collarVal;

  // size khả dụng theo cả MÀU + CỔ hiện tại
  const sizesFor = productVariants.value
    .filter(p =>
      p.color === selectedProduct.value.color &&
      ( (p.collar ?? p.colar) === selectedCollar.value )
    )
    .map(p => p.size);
  availableSizes.value = [...new Set(sizesFor)];
};
const isActiveFlag = (v) => {
  if (v === 1 || v === true) return true
  const s = String(v ?? '').trim().toLowerCase()
  return s === '1' || s === 'true'
}
const fetchProductDetail = async () => {
  const id = route.params.id  // id từ URL có thể là productDetailId hoặc productId
  try {
    // 1) Chuẩn hoá thành productId
    let productId = id
    try {
      const detailRes = await axios.get(`http://localhost:8080/productDetail/findProductId/${id}`)
      if (typeof detailRes.data === 'number') productId = detailRes.data
      else if (detailRes.data && detailRes.data.productId) productId = detailRes.data.productId
    } catch (e) {
      // Nếu gọi findProductId fail thì coi id là productId
      console.warn('findProductId fail, dùng id làm productId', e)
    }

    // 2) Lấy toàn bộ biến thể theo productId
    const { data } = await axios.get(`http://localhost:8080/productDetail/show/${productId}`)

    // 3) Chuẩn hoá & CHỈ GIỮ biến thể active (IS_ACTIVE = 1)
    const processed = (Array.isArray(data) ? data : [])
      .map(v => {
        const collarVal = v.collar ?? v.colar ?? v.collarName ?? '';
        return {
          ...v,
          images: Array.isArray(v.images) ? v.images : [],
          collar: collarVal,
          colar: collarVal, // giữ tương thích chỗ cũ nếu nơi khác còn dùng "colar"
          __active: isActiveFlag(v.isActive ?? v.active ?? v.status),
        };
      })
      .filter(v => v.__active);

    if (!processed.length) {
      console.warn('Không có biến thể active để hiển thị')
      productVariants.value = []
      selectedProduct.value = {}
      return
    }

    productVariants.value = processed

    // 4) Chọn biến thể ban đầu: đúng productDetailId nếu khớp & active, ngược lại chọn phần tử đầu
    const picked = processed.find(v => String(v.productDetailId) === String(id)) || processed[0]
    updateSelection(picked)
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm', error)
  }
}

const buyNow = async() => {
  if (!customerId) {
    await Swal.fire({
      icon: 'warning',
      title: 'Bạn cần đăng nhập',
      text: 'Vui lòng đăng nhập để mua hàng.',
      confirmButtonText: 'OK',
      didOpen: () => { Swal.getContainer().style.zIndex = '20000'; }
    });
    return;
  }
  const checkoutItem = {
    productDetailId: selectedProduct.value.productDetailId,
    productName: selectedProduct.value.productName,
    brandName: selectedProduct.value.brandName,
    color: selectedProduct.value.color,
    size: selectedProduct.value.size,
    colar: selectedProduct.value.colar,
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

const stock = computed(() => {
  const n = Number(selectedProduct.value?.quantity ?? 0);
  return Number.isFinite(n) && n > 0 ? n : 0;  // chuẩn hóa
});

const inStock = computed(() => stock.value > 0);

const canBuy = computed(() => {
  const q = Number(quantity.value ?? 0);
  return inStock.value && Number.isFinite(q) && q >= 1 && q <= stock.value;
});

onMounted(() => {
  fetchProductDetail()

  if (customerId) {
    findCartIdByCustomerId()
  }
})
</script>

<template>
  <Banner title="" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container bg-white rounded-4 shadow p-4" v-if="selectedProduct.productDetailId">
    <div class="row g-4 align-items-start">
      <div class="col-md-6 position-relative">
        <!-- Ảnh chính -->
        <img :src="selectedImage.startsWith('./') ? selectedImage.replace('./', '/') : selectedImage" alt="Sản phẩm"
          class="w-100 rounded shadow-sm border border-light" style="max-height: 400px; object-fit: contain;" />

        <!-- Danh sách ảnh phụ -->
        <div class="d-flex gap-2 mt-3 overflow-auto">
          <img v-for="(img, index) in selectedProduct.images" :key="index"
            :src="img.startsWith('./') ? img.replace('./', '/') : img" alt="Ảnh phụ" @click="selectImage(img)"
            class="img-thumbnail border border-2" :class="{ 'border-primary': selectedImage === img }"
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
          <button v-for="color in uniqueColors" :key="color" @click="selectColor(color)" class="color-btn"
            :class="{ active: selectedProduct.color === color }">
            {{ color }}
          </button>
        </div>
        <div class="mb-3">
          Cổ:
          <button
            v-for="c in collarsForCurrentColor"
            :key="c"
            @click="selectCollar(c)"
            class="color-btn"
            :class="{ active: (selectedCollar === c) }"
          >
            {{ c }}
          </button>
        </div>
        <div class="mb-3">
          Kích thước:
          <button v-for="size in sizeList" :key="size" class="size-btn" :class="[
            { active: selectedProduct.size === size },
            { unavailable: !availableSizes.includes(size) }
          ]" :disabled="!availableSizes.includes(size)" @click="selectSize(size)">
            {{ size }}
          </button>
          <br>
            <a href="#" class="small text-decoration-underline ms-auto" @click.prevent="openSizeGuide">
            Xem bảng chọn size
    </a>
        </div>

        <div class="mb-4" style="max-width: 150px;">
          <label class="form-label">Số lượng</label>
          <input type="number" v-model.number="quantity" min="1" :max="selectedProduct.quantity" class="form-control"
            @blur="validateQuantity" @change="validateQuantity" @keydown="blockMinus" :disabled="!inStock"/>
          <small class="text-muted mt-1 d-block">
            {{ !inStock ? '0 sản phẩm có sẵn' : stock + ' sản phẩm có sẵn' }}
          </small>
        </div>

        <!-- Thông báo khi không thể mua -->
        <div v-if="!inStock" class="text-danger fw-semibold mt-1">
          Sản phẩm hiện đã hết hàng
        </div>

        <!-- Luôn hiển thị 2 nút nhưng disable nếu không thể mua -->
        <div class="d-flex gap-3 mb-4">
          <button class="btn btn-primary product-button fw-semibold" @click="addToCart()"
            :disabled="!canBuy">
            Thêm vào giỏ
          </button>

          <button class="btn btn-danger product-button fw-semibold" @click="buyNow()"
            :disabled="!canBuy">
            Mua ngay
          </button>
        </div>

        <hr />
        <p class="text-muted">{{ selectedProduct.description }}</p>
      </div>


    </div>
  </div>
  <!-- Toast thông báo thêm vào giỏ thành công -->
  <div v-if="showToast" class="position-fixed top-0 end-0 p-3" style="z-index: 1055;">
    <div class="toast align-items-center show bg-success text-white border-0">
      <div class="d-flex">
        <div class="toast-body">
          ✅ Đã thêm sản phẩm vào giỏ hàng!
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="showToast = false"></button>
      </div>
    </div>
  </div>

  <!-- Modal xem bảng size & cách đo -->
<div v-if="showSizeGuide" class="size-modal-backdrop" @click.self="closeSizeGuide">
  <div class="size-modal">
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h6 class="mb-0">Bảng size</h6>
      <button class="btn-close" @click="closeSizeGuide" aria-label="Close"></button>
    </div>

    <div class="size-modal-body">
      <img :src="sizeChartUrl" alt="Bảng size" class="img-fluid rounded border mb-3" />

    </div>
  </div>
</div>
</template>

<style scoped>
.size-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.5);
  display: grid;
  place-items: center;
  z-index: 1060;
}
.size-modal {
  background: #fff;
  width: min(800px, 90vw);
  max-height: 90vh;
  overflow: auto;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,.2);
}
.size-modal-body img {
  width: 100%;
  height: auto;
  object-fit: contain;
}
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