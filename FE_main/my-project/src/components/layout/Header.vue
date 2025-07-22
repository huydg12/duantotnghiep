<script setup>
import Logo from "/images/logo.png";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { ref, computed, watch  } from "vue";
import { useUserStore } from "@/stores/userStore";
import { onMounted } from "vue";
import axios from "axios";
import { useCartFavoriteStore } from "../../stores/cartFavoriteStore";
const store = useCartFavoriteStore()
const userStore = useUserStore();
const router = useRouter();
const userInfo = ref(JSON.parse(localStorage.getItem("user")));
let customerId =ref(null);
const brandList = ref([])
const suggestedProducts = ref([]);
const fetchSuggestedProducts = async () => {
  const keyword = searchText.value.trim();
  if (!keyword) {
    suggestedProducts.value = [];
    return;
  }

  try {
    const res = await axios.get(`http://localhost:8080/product/search?keyword=${keyword}`);
    suggestedProducts.value = res.data; // giả sử API trả về danh sách sản phẩm
  } catch (error) {
    console.error("❌ Lỗi khi fetch gợi ý:", error);
    suggestedProducts.value = [];
  }
};
const fetchBrands = async () => {
  try {
    const res = await axios.get('http://localhost:8080/brand/show')
    brandList.value = res.data // giả sử là mảng object có thuộc tính brandName
  } catch (error) {
    console.error('❌ Lỗi khi fetch brand:', error)
  }
}
// Modal tìm kiếm
const searchText = ref("");
const showSearch = ref(false); // Biến điều khiển hiển thị modal tìm kiếm
// Mở modal tìm kiếm
const openSearchModal = () => {
    showSearch.value = true;
};
// Nếu userInfo hợp lệ thì lấy customerId
if (userInfo.value && userInfo.value.customerId) {
  customerId.value = userInfo.value.customerId; // ✅ Gán vào .value của ref
}
// Từ khóa phổ biến
const popularTerms = ref(["Nike", "Adidas", "Converse", "Vans", "Puma"]);
// Tìm kiếm từ modal
const doSearchFromModal = () => {
  if (searchText.value.trim()) {
    router.push({ path: "/product", query: { keyword: searchText.value } });
    showSearch.value = false; // ✅ Đóng popup khi nhấn Enter
  }
};

// Hàm đồng bộ từ localStorage → userStore + customerId
const syncUserFromStorage = () => {
  const userJson = localStorage.getItem("user");
  if (userJson) {
    try {
      const user = JSON.parse(userJson);
      userStore.setUser(user);
      userInfo.value = user;
      customerId.value = user.customerId;
      console.log("✅ Đồng bộ user từ localStorage:", user);
    } catch (e) {
      console.error("❌ Lỗi parse user từ localStorage:", e);
    }
  }
};

// Hàm đăng xuất
const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("user");
    userStore.setUser(null); // Reset userStore
    userInfo.value = null;
    router.push("/auth/login");
};

const formatCurrency = (value) => {
  if (!value && value !== 0) return "";
  return value.toLocaleString("vi-VN", {
    style: "currency",
    currency: "VND",
  });
};
watch(searchText, (newVal) => {
  fetchSuggestedProducts();
});
// ✅ Gọi khi component mount
onMounted(async () => {
  fetchBrands()
  syncUserFromStorage()

  if (customerId.value) {
    await store.refreshAll(customerId.value);
  } else {
    console.warn("⚠️ Không có customerId để fetch dữ liệu");
  }
});


function goToProductDetail(productDetailId) {
  router.push(`/productdetail/${productDetailId}`);
  console.log("sp chi tiết: " + productDetailId);
}
const goToDetail = (id) => {
  router.push(`/productdetail/${id}`)
}
</script>

<template>
    <header class="header sticky-top">
        <nav class="navbar navbar-expand-lg container-xl">
            <div class="container-fluid">
                <router-link class="navbar-brand" to="/home">
                    <img :src="Logo" alt="Logo" />
                </router-link>

                <div class="collapse navbar-collapse justify-content-center">
                    <ul class="navbar-nav gap-3">
                        <li class="nav-item"><router-link class="nav-link" to="/home">Trang Chủ</router-link>
                        </li>
                        <li class="nav-item"><router-link class="nav-link" to="/introduce">Giới Thiệu</router-link></li>
                        <li class="nav-item dropdown">
                          <div class="d-flex align-items-center">
                            <router-link class="nav-link pe-1" to="/product">Sản Phẩm</router-link>
                            <a class="nav-link dropdown-toggle ps-1" href="#" role="button" data-bs-toggle="dropdown"></a>
                          </div>
                          <ul class="dropdown-menu">
                            <li v-for="brand in brandList" :key="brand.id">
                            <a
                              class="dropdown-item cursor-pointer"
                              @click="$router.push({ path: '/product', state: { brandFromHeader: brand.name } })"
                            >
                              {{ brand.name }}
                            </a>
                            </li>
                          </ul>
                        </li>
                        <li class="nav-item"><router-link class="nav-link" to="/contact">Liên Hệ</router-link></li>
                    </ul>
                </div>

                <div class="d-flex align-items-center gap-4 header-tools">
                    <!-- Thanh tìm kiếm -->
                    <div class="search-container position-relative">
                        <i class="bi bi-search search-icon text-white" @click="openSearchModal()" style="cursor: pointer;"></i>
                    </div>
                      <!-- Icon yêu thích -->
                      <div class="cart-icon">
                    <router-link  to="/favorite" ><a href="#" title="Yêu thích">
                      <i class="bi bi-heart-fill text-white"></i></a></router-link>
                        <span class="cart-count">{{ store.favoriteItems.length  }}</span>
                    </div>

                    <!-- Khung giỏ hàng -->
                    <div class="position-relative cart-dropdown">
                    <!-- Icon giỏ hàng -->
                    <div class="cart-icon">
                      <router-link  to="/cart" title="Đăng nhập">
                        <i class="bi bi-bag-fill text-white" title="Giỏ hàng"></i>
                      </router-link>
                        <span class="cart-count">{{ store.cartItems.length }}</span>
                    </div>

                    <!-- Popup hiện khi hover -->
                    <div class="cart-popup shadow bg-white rounded">
                        <div v-if="store.cartItems.length > 0">
                        <div v-for="item in store.cartItems.slice(0, 5)" :key="item.cartDetailId" class="d-flex align-items-center mb-2 px-2 hover-highlight" @click="goToProductDetail(item.productDetailId)">
                            <img :src="item.images" alt="..." class="me-2" style="width: 50px; height: 50px; object-fit: contain;" />
                            <div class="flex-grow-1">
                            <div class="fw-medium text-truncate" style="max-width: 220px;">{{ item.productName }}</div>
                            <small class="text-danger">{{ formatCurrency(item.price) }} x {{ item.quantity }}</small>
                            </div>
                        </div>
                        <hr class="my-2" />
                        <div class="d-flex justify-content-between align-items-center px-2 pb-2">
                            <span class="text-muted small">{{ store.cartItems.length }} Thêm Hàng Vào Giỏ</span>
                            <router-link to="/cart" class="btn btn-sm btn-danger custom-cart-btn">Xem Giỏ Hàng</router-link>
                        </div>
                        </div>
                        <div v-else class="text-center text-muted py-2">Chưa có sản phẩm</div>
                    </div>
                    </div>
                    <!-- Nếu đã đăng nhập -->
                    <div v-if="userStore.user" class="dropdown">
                        <a class="text-white dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle"></i>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li class="dropdown-item fw-semibold text-uppercase">{{ userStore.user.name }}</li>
                            <li><router-link class="dropdown-item" to="/informationcustomer">Thông tin cá
                                    nhân</router-link></li>

                            <li><a class="dropdown-item text-danger" href="#" @click.prevent="handleLogout">Đăng
                                    xuất</a></li>
                        </ul>
                    </div>

                    <!-- Nếu chưa đăng nhập -->
                    <router-link v-else to="/auth/login" title="Đăng nhập">
                        <i class="bi bi-person-circle text-white"></i>
                    </router-link>
                </div>
            </div>
        </nav>
    </header>
<!-- Overlay toàn màn hình để bắt click -->
<div class="search-overlay" v-if="showSearch" @click.self="showSearch = false">

  <!-- Khung tìm kiếm -->
  <div class="search-bar-wrapper">
    <div class="container py-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <i class="bi bi-search search-icon text-white me-3" style="cursor: pointer;"></i>
        <input
          type="text"
          class="search-input form-control me-2"
          v-model="searchText"
          placeholder="Tìm kiếm sản phẩm..."
          @keyup.enter="doSearchFromModal"
        />
        <button class="btn custom-close-button ms-2" @click="showSearch = false">Đóng</button>
      </div>

      <h6 class="text-white mb-2">Từ khóa phổ biến</h6>
      <div class="d-flex flex-wrap gap-2">
        <span
          v-for="term in popularTerms"
          :key="term"
          class="badge bg-secondary text-white px-3 py-2 rounded-pill"
          @click="searchByPopularTerm(term)"
          style="cursor: pointer"
        >
          {{ term }}
        </span>
      </div>
      <br>
      <div v-if="suggestedProducts.length > 0" >
        <h6 class="text-white mb-2">Gợi ý sản phẩm</h6>
        <ul class="list-unstyled mb-0">
          <li
            v-for="product in suggestedProducts"
            :key="product.productId"
            class="d-flex align-items-center py-1 px-2 hover-highlight"
            style="cursor: pointer;"
            @click="goToDetail(product.productId); showSearch = false"
          >
            <img
              :src="product.image1"
              alt="img"
              class="me-2"
              style="width: 40px; height: 40px; object-fit: contain;"
            />
            <span class="text-white">{{ product.productName }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>

</div>
</template>

<style >
.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1050;
}

.search-bar-wrapper {
  background: black;
  width: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.search-input {
  border: none;
  font-size: 1.25rem;
  background-color: #222;
  padding: 0.75rem 1rem;
  width: 100%;
  border-radius: 8px;
  color: white;
}

.search-input::placeholder {
  color: #aaa;
}

.cart-dropdown {
  display: inline-block;
}

.cart-icon {
  position: relative;
  cursor: pointer;
  font-size: 22px;
}

.cart-count {
  position: absolute;
  top: -6px;
  right: -10px;
  background-color: red;
  color: white;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 50%;
}

.cart-popup {
  display: none;
  position: absolute;
  right: 0;
  top: 120%;
  width: 280px;
  z-index: 999;
  background: white;
  border: 1px solid #ddd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 14px;
  transition: all 0.3s ease;
  padding-top: 8px;
}

.cart-items-scroll {
  max-height: 280px; /* đúng khoảng hiển thị 5 sp */
  overflow: hidden;  /* không cho cuộn */
}

.cart-dropdown:hover .cart-popup {
  display: block;
}
.cart-popup {
  top: 100%; /* Hoặc 105% nếu cần cách nhẹ */
}
.hover-highlight {
  transition: background-color 0.2s ease;
  cursor: pointer;
}
.hover-highlight:hover {
  background-color: #f8f9fa; /* hoặc #f0f0f0 nếu muốn đậm hơn */
}
.cart-popup .btn.btn-danger {
  font-size: 12px !important;
  padding: 7px 8px !important;
  line-height: 1 !important;
  border-radius: 4px !important;
}
.cursor-pointer {
  cursor: pointer;
}
.hover-highlight:hover {
  background-color: #343a40 !important; /* Tối hơn, gần với bg-dark */
  transition: background-color 0.2s ease;
}
</style>
