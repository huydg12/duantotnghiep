<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
import Swal from 'sweetalert2'
// Store và router
const store = useCartFavoriteStore();
const router = useRouter();

// Reactive
const products = ref([]);
const allProducts = ref([]);
const showToast = ref(false);
const showRemoveToast = ref(false);
const favoriteProductIds = ref(new Set());
const favoriteMap = ref(new Map());
const recentlyRemovedId = ref(null); // 👈 để hiệu ứng icon
let customerId = null;

// Lấy thông tin khách hàng
const userJson = localStorage.getItem("user");
if (userJson) {
  try {
    const user = JSON.parse(userJson);
    customerId = user.customerId;
    console.log("✅ Customer ID:", customerId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
}

// Điều hướng trang chi tiết sản phẩm
const goToDetail = (id) => {
  router.push(`/productdetail/${id}`);
};

// Lấy danh sách yêu thích từ backend
const fetchFavorites = async () => {
  if (!customerId) return;
  try {
    const res = await axios.get(`http://localhost:8080/favorite/show/${customerId}`);
    favoriteProductIds.value = new Set(res.data.map(item => item.productId));
    favoriteMap.value = new Map(res.data.map(item => [item.productId, item.favoriteId]));
  } catch (err) {
    console.error("❌ Lỗi lấy danh sách yêu thích", err);
  }
};

// Xử lý thêm/xóa yêu thích
const toggleFavorite = async (productId) => {
  if (!customerId) {
    alert("Bạn cần đăng nhập để sử dụng tính năng này");
    return;
  }

  const isFav = favoriteProductIds.value.has(productId);
  try {
    if (isFav) {
      const favoriteId = favoriteMap.value.get(productId);
      if (!favoriteId) {
        console.warn("⚠️ Không tìm thấy favoriteId tương ứng.");
        return;
      }

      await axios.delete(`http://localhost:8080/favorite/delete/${favoriteId}`);
      favoriteProductIds.value.delete(productId);
      favoriteMap.value.delete(productId);
      recentlyRemovedId.value = favoriteId;

      showRemoveToast.value = true;
      setTimeout(() => {
        showRemoveToast.value = false;
        recentlyRemovedId.value = null;
      }, 3000);
    } else {
      const res = await axios.post(`http://localhost:8080/favorite/add`, {
        customerId,
        productId
      });
      const newFavoriteId = res.data.favoriteId || res.data.id;
      favoriteMap.value.set(productId, newFavoriteId);
      favoriteProductIds.value.add(productId);

      showToast.value = true;
      setTimeout(() => {
        showToast.value = false;
      }, 3000);
    }

    // Cập nhật lại store
    await store.fetchFavoriteItems(customerId);

  } catch (err) {
    console.error("❌ Lỗi toggle yêu thích", err);
  }
};

// Lấy sản phẩm nổi bật
const fetchTop4Products = async () => {
  try {
    const response = await axios.get('http://localhost:8080/product/top4');
    products.value = response.data;
    console.log("🔥 Sản phẩm nổi bật", products.value);
  } catch (error) {
    console.error('❌ Lỗi hiển thị sản phẩm nổi bật', error);
  }
};

// Lấy toàn bộ sản phẩm
const fetchAllProducts = async () => {
  try {
    const response = await axios.get('http://localhost:8080/product/showSPdto');
    allProducts.value = response.data;
    console.log("📦 Tất cả sản phẩm", allProducts.value);
  } catch (error) {
    console.error('❌ Lỗi hiển thị sản phẩm', error);
  }
};

// Lifecycle
onMounted(() => {
  const flag = localStorage.getItem("paymentSuccessFlag");
  if (flag === "1") {
    Swal.fire({
      icon: 'success',
      title: 'Thanh toán thành công!',
      text: 'Cảm ơn bạn đã mua hàng tại cửa hàng chúng tôi.',
      confirmButtonText: 'Đóng'
    });
    localStorage.removeItem("paymentSuccessFlag");
  }
  fetchTop4Products();
  fetchAllProducts();
  fetchFavorites();
});

// ==== FILTER BRAND ====
const selectedBrand = ref("nike");

const productsByBrand = computed(() => {
  return allProducts.value.filter(
    p => p.brandName?.toLowerCase() === selectedBrand.value.toLowerCase()
  );
});

function selectBrand(brand) {
  selectedBrand.value = brand;
}

// ==== FORMAT TIỀN ====
function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}
</script>
<template>
    <main>
        <section class="hero-section" style="
        background-image: url('https://allgoodtales.com/wp-content/uploads/2018/10/Nike-Header-min.jpg');">
            <div class="overlay">
                <div class="text-center text-white px-4">
                    <h1 class="display-5 fw-bold mb-3">
                        Giày Xịn – Phong Cách Chất – Chill Hết Nấc
                    </h1>
                    <p class="mb-5 fs-5">
                        Đập hộp ngay đôi sneaker đỉnh cao dành riêng cho bạn
                    </p>
                    <a href="#" class="btn btn-hero">Mua Ngay</a>
                </div>
            </div>
        </section>

        <section class="py-5 bg-light">
            <div class="container-xl">
                <div class="text-center mb-5 section-title">
                    <h2>Sản Phẩm Nổi Bật</h2>
                    <span class="divider"></span>
                    <p class="mt-3 mx-auto">
                        Những mẫu giày hot trend, được giới trẻ săn đón – nổi bật về cả kiểu
                        dáng lẫn chất lượng.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-12 col-sm-6 col-md-3" v-for="product in products" :key="product.productId">
                        <div class="card h-100 product-card" @click="goToDetail(product.productId)" style="cursor: pointer;">
                                <!-- Icon yêu thích -->
                                <i
                                    class="fa-heart fa position-absolute top-0 end-0 m-2 favorite-icon transition"
                                    :class="{
                                    'fas text-danger scale-up': favoriteMap.has(product.productId),
                                    'far text-secondary': !favoriteMap.has(product.productId)
                                    }"
                                    @click.stop="toggleFavorite(product.productId)"
                                ></i>
                            <div class="image-container position-relative">
                            <img
                                :src="product.image1"
                                class="product-image image-front"
                                :alt="product.productName"
                            />
                            <img
                                :src="product.image2"
                                class="product-image image-hover position-absolute top-0 start-0"
                                :alt="product.productName"
                            />
                            </div>
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="cart-title">{{ product.productName  }}</h5>
                                <p class="product-price mt-1">
                                    {{ formatCurrency(product.price) }}
                                </p>
                                <!-- <button class="btn btn-buy mt-auto"@click="goToDetail(product.productId)">Xem chi tiết</button> -->
                            </div>
                        </div>
                    </div>
                </div>

                <div class="text-center mt-5">
                    <router-link to="/product" class="btn btn-dark px-4 py-2">Xem Tất Cả Sản Phẩm</router-link>
                </div>
            </div>
        </section>

        <section id="tat-ca-san-pham" class="py-5">
            <div class="container-xl">
                <div class="text-center mb-5 section-title">
                    <h2>Sản Phẩm Theo Hãng</h2>
                    <span class="divider"></span>
                    <p class="mt-3 mx-auto">
                        Khám phá những đôi giày nổi bật từ các thương hiệu đình đám.
                    </p>
                </div>

                <div class="d-flex justify-content-center gap-3 mb-5 filter-buttons">
                    <button class="btn" :class="{ active: selectedBrand === 'nike' }" @click="selectBrand('nike')">Nike</button>
                    <button class="btn" :class="{ active: selectedBrand === 'adidas' }" @click="selectBrand('adidas')">Adidas</button>
                    <button class="btn" :class="{ active: selectedBrand === 'puma' }" @click="selectBrand('puma')">Puma</button>
                    <button class="btn" :class="{ active: selectedBrand === 'converse' }" @click="selectBrand('converse')">Converse</button>
                    <button class="btn" :class="{ active: selectedBrand === 'new balance' }" @click="selectBrand('new balance')">New Balance</button>

                </div>

                <div id="product-list-container">
                    <div class="row g-4">
                        <div class="col-6 col-md-3" v-for="product in productsByBrand" :key="product.productId">
                            <div class="card h-100 product-card" @click="goToDetail(product.productId)" style="cursor: pointer;">
                                <!-- Icon yêu thích -->
                                <i
                                    class="fa-heart fa position-absolute top-0 end-0 m-2 favorite-icon transition"
                                    :class="{
                                    'fas text-danger scale-up': favoriteMap.has(product.productId),
                                    'far text-secondary': !favoriteMap.has(product.productId)
                                    }"
                                    @click.stop="toggleFavorite(product.productId)"
                                ></i>
                                <img
                                    :src="product.image1"
                                    class="product-image image-front"
                                    :alt="product.productName"
                                />
                                <img
                                    :src="product.image2"
                                    class="product-image image-hover position-absolute top-0 start-0"
                                    :alt="product.productName"
                                />
                                <div class="card-body text-center">
                                    <h6 class="card-title">{{ product.productName }}</h6>
                                    <p class="product-price">{{ formatCurrency(product.price) }}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>
        <!-- Toast thông báo thêm vào giỏ thành công -->
  <div
    v-if="showToast"
    class="position-fixed top-0 end-0 p-3"
    style="z-index: 1055;"
  >
    <div class="toast align-items-center show bg-success text-white border-0">
      <div class="d-flex">
        <div class="toast-body">
          ✅ Đã thêm vào mục yêu thích!
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          @click="showToast = false"
        ></button>
      </div>
    </div>
  </div>

  <!-- Toast xóa khỏi yêu thích -->
<div
  v-if="showRemoveToast"
  class="position-fixed top-0 end-0 p-3"
  style="z-index: 1055;"
>
  <div class="toast align-items-center show bg-danger text-white border-0">
    <div class="d-flex">
      <div class="toast-body">
        ❌ Đã xóa khỏi mục yêu thích!
      </div>
      <button
        type="button"
        class="btn-close btn-close-white me-2 m-auto"
        @click="showRemoveToast = false"
      ></button>
    </div>
  </div>
</div>
</template>

<style scoped>
:root {
    --color-primary: #ef4444;
    --color-primary-dark: #dc2626;
    --color-dark: #000;
    --color-light: #fff;
    --color-secondary-text: #6c757d;
    --font-family-main: "Be Vietnam Pro", sans-serif;
}

.hero-section {
    position: relative;
    background-size: cover;
    background-position: center;
    height: 24rem;
}

.hero-section .overlay {
    position: absolute;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
}

.btn-hero {
    background-color: #ef4444 !important;
    color: #fff !important;
    border: none !important;
    font-weight: 750 !important;
    padding: 0.75rem 1.5rem !important;
    transition: background-color 0.3s ease;
}

.btn-hero:hover {
    background-color: #dc2626 !important;
    color: #fff !important;
}

/* Product card */
.product-card {
    border: 1px solid #e5e7eb;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.product-card .card-img-top {
    height: 12rem;
    object-fit: cover;
}

.product-card .card-title {
    font-weight: 700;
}

.product-price {
    color: #ef4444;
    font-weight: 700;
    font-size: 1.1rem;
}

.btn-buy {
    background-color: #ef4444 !important;
    color: var(--color-light) !important;
    font-weight: 500 !important;
    transition: background-color 0.3s ease;
}

.btn-buy:hover {
    background-color: #dc2626 !important;
    color: var(--color-light) !important;
}

/*  Filter Section */
.filter-buttons .btn {
    background-color: #000;
    color: var(--color-light);
    border: none;
    font-weight: 500;
    transition: background-color 0.3s ease;
}

.filter-buttons .btn:hover {
    background-color: #ef4444;
    color: var(--color-light);
}

.filter-buttons .btn.active {
    background-color: #ef4444;
    box-shadow: 0 4px 15px -3px rgb(239 68 68 / 0.5);
}

#product-list-container .product-list {
    transition: opacity 0.5s ease;
}
.image-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-radius: 8px;
}

.image-front,
.image-hover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.image-hover {
  opacity: 0;
  z-index: 1;
}

.image-container:hover .image-hover {
  opacity: 1;
}

.image-container:hover .image-front {
  opacity: 0;
}
.favorite-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #ccc;
  font-size: 1.5rem;
  z-index: 10;
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-icon.active {
  color: hotpink;
}

.favorite-icon:hover {
  color: red;
}
.scale-up {
  transform: scale(1.2);
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
