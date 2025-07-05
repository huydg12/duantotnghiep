<script setup>
import Logo from '/images/logo.png';
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { ref, computed } from 'vue'
import { useUserStore } from './stores/userStore';
import { onMounted } from 'vue';

const userStore = useUserStore();
const router = useRouter()
const searchText = ref('')

const userInfo = ref(JSON.parse(localStorage.getItem('user')))
const user = computed(() => userInfo.value)
onMounted(() => {
  userStore.loadUserFromLocalStorage(); // Đọc lại từ localStorage khi load trang
});
// Hàm đăng xuất
const handleLogout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("user");
  userStore.setUser(null); // Reset userStore
  userInfo.value = null;
  router.push("/auth/login");
};


// Hàm xử lý khi nhấn Enter
const handleSearch = () => {
  if (searchText.value.trim()) {
    router.push({ path: '/product', query: { keyword: searchText.value } })
  }
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
            <li class="nav-item"><router-link class="nav-link active" to="/home">Trang Chủ</router-link></li>
            <li class="nav-item"><router-link class="nav-link" to="/introduce">Giới Thiệu</router-link></li>
            <li class="nav-item dropdown">
              <div class="d-flex align-items-center">
                <router-link class="nav-link pe-1" to="/product">Sản Phẩm</router-link>
                <a class="nav-link dropdown-toggle ps-1" href="#" role="button" data-bs-toggle="dropdown"></a>
              </div>
              <ul class="dropdown-menu">
                <li><a class="dropdown-item" href="#">Adidas</a></li>
                <li><a class="dropdown-item" href="#">Nike</a></li>
                <li><a class="dropdown-item" href="#">Converse</a></li>
                <li><a class="dropdown-item" href="#">Vans</a></li>
                <li><a class="dropdown-item" href="#">Puma</a></li>
                <li><a class="dropdown-item" href="#">Fila</a></li>
              </ul>
            </li>
            <li class="nav-item"><router-link class="nav-link" to="/contact">Liên Hệ</router-link></li>
          </ul>
        </div>

        <div class="d-flex align-items-center gap-4 header-tools">
          <!-- Thanh tìm kiếm -->
          <div class="search-container position-relative">
            <i class="bi bi-search search-icon text-white" @click="handleSearch" style="cursor: pointer;"></i>
            <input
              type="text"
              placeholder="Tìm kiếm..."
              class="search-input form-control"
              v-model="searchText"
              @keyup.enter="handleSearch"
            />
          </div>

          <a href="#" title="Yêu thích"><i class="bi bi-heart-fill text-white"></i></a>
          <router-link to="/cart" title="Giỏ hàng"><i class="bi bi-bag-fill text-white"></i></router-link>
          <!-- Nếu đã đăng nhập -->
            <div v-if="userStore.user" class="dropdown">
              <a class="text-white dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                <i class="bi bi-person-circle"></i>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li class="dropdown-item fw-semibold text-uppercase">{{ userStore.user.name }}</li>
                <li><router-link class="dropdown-item" to="/informationcustomer">Thông tin cá nhân</router-link></li>

                <li><a class="dropdown-item text-danger" href="#" @click.prevent="handleLogout">Đăng xuất</a></li>
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

  <div id="app">

    <RouterView />
  </div>

  <footer class="footer pt-5 pb-4">
    <div class="container-xl">
      <div class="row g-5">
        <div class="col-lg-4 col-md-12">
          <img :src="Logo" alt="Kix Store" style="height: 64px" class="mb-3" />
          <p>Kix Store – Nơi hội tụ những đôi sneaker đỉnh cao, đậm chất cá tính và thời trang.</p>
        </div>
        <div class="col-lg-4 col-md-6">
          <h5 class="mb-3 fw-bold">Liên kết nhanh</h5>
          <ul class="list-unstyled footer-links">
            <li><router-link to="/home">Trang chủ</router-link></li>
            <li><router-link to="/product">Sản phẩm</router-link></li>
            <li><router-link to="/introduce">Giới thiệu</router-link></li>
            <li><router-link to="/contact">Liên hệ</router-link></li>
          </ul>
        </div>
        <div class="col-lg-4 col-md-6">
          <h5 class="mb-3 fw-bold">Liên hệ</h5>
          <p><i class="bi bi-telephone-fill me-2"></i>0123 456 789</p>
          <p><i class="bi bi-envelope-fill me-2"></i>support@kixstore.vn</p>
          <div class="d-flex gap-4 mt-4 social-icons">
            <a href="#"><i class="bi bi-facebook"></i></a>
            <a href="#"><i class="bi bi-instagram"></i></a>
            <a href="#"><i class="bi bi-twitter-x"></i></a>
            <a href="#"><i class="bi bi-youtube"></i></a>
          </div>
        </div>
      </div>
      <div class="mt-5 pt-4 text-center copyright">
        © 2025 Kix Store. All rights reserved.
      </div>
    </div>
  </footer>

  <!-- Bootstrap & Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
</template>
