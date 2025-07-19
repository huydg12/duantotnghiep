<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import Banner from "../common/Banner.vue";
import axios from 'axios'
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
const store = useCartFavoriteStore()
const favoriteList = ref([])
let customerId = null;
const showRemoveToast = ref(false);
const userJson = localStorage.getItem("user");
const router = useRouter()
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
const goToDetail = (id) => {
  router.push(`/productdetail/${id}`) // Chuyển hướng đến trang chi tiết sản phẩm với ID
}
const fetchFavorite = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/favorite/show/${customerId}`);
    favoriteList.value = res.data;
    console.log("Yêu thích:", favoriteList.value);
  } catch (err) {
    console.error("Lỗi khi lấy cart", err);
  }
};

const deleteFavorite = async (favoriteId) => {
  try {
    await axios.delete(`http://localhost:8080/favorite/delete/${favoriteId}`);
        await fetchFavorite() // Gọi lại danh sách yêu thích
    await store.fetchFavoriteItems(customerId);
    showRemoveToast.value = true
    setTimeout(() => {
    showRemoveToast.value = false
    }, 3000)
  } catch (err) {
    console.error("Lỗi khi lấy cart", err);
  }
};
onMounted(() => {
  fetchFavorite()
})  
</script>

<template>
  <Banner
    title=""
    breadcrumb=""
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg"
  />

  <div class="container mt-4">
    <h3 class="text-center mb-4 fw-bold text-black">Danh sách yêu thích của tôi</h3>

    <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4 mb-5">
      <div v-for="favorite in favoriteList" :key="favorite.favoriteId" class="col">
        <div
          class="card h-100 shadow-sm position-relative"
          @click="goToDetail(favorite.productId)"
          style="cursor: pointer;"
        >
          <!-- Icon yêu thích -->
          <i
            class="fas fa-heart favorite-icon text-danger"
            :class="{ 'scale-up': favorite.favoriteId === recentlyRemovedId }"
            @click.stop="handleFavoriteClick(favorite.favoriteId)"
          ></i>

          <div class="image-container position-relative">
            <img
              :src="favorite.image1"
              class="product-image image-front"
              :alt="favorite.productName"
            />
            <img
              :src="favorite.image2"
              class="product-image image-hover position-absolute top-0 start-0"
              :alt="favorite.productName"
            />
          </div>

          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ favorite.productName }}</h5>
            <p class="card-text text-muted">{{ favorite.brandName }}</p>
            <p class="card-text fw-bold text-danger">{{ favorite.price }}₫</p>
            <!-- <button
              class="btn btn-outline-primary mt-auto"
              @click="goToDetail(favorite.productId)"
            >
              Xem chi tiết
            </button> -->
          </div>
        </div>
      </div>
    </div>

            <!-- Phân trang -->
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <li class="page-item disabled">
              <a class="page-link" href="#"><span>&laquo;</span></a>
            </li>
            <li class="page-item active"><a class="page-link" href="#">1</a></li>
            <li class="page-item"><a class="page-link" href="#">2</a></li>
            <li class="page-item"><a class="page-link" href="#">3</a></li>
            <li class="page-item">
              <a class="page-link" href="#"><span>&raquo;</span></a>
            </li>
          </ul>
        </nav>
    <!-- Toast xóa -->
    <div
      v-if="showRemoveToast"
      class="position-fixed top-0 end-0 p-3"
      style="z-index: 1055;"
    >
      <div class="toast align-items-center show bg-danger text-white border-0">
        <div class="d-flex">
          <div class="toast-body">❌ Đã xóa sản phẩm khỏi mục yêu thích!</div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            @click="showRemoveToast = false"
          ></button>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
.card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-radius: 10px;
  height: 100%; /* Giữ thẻ card không cao quá */
  padding: 0.5rem;
}

.card-body {
  padding: 0.8rem 1rem;
  font-size: 0.95rem;
}

.card-title {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.card-text {
  font-size: 0.9rem;
}

.btn {
  font-size: 0.85rem;
  padding: 6px 12px;
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



.card:hover {
  transform: scale(1.03);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.favorite-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 1.6rem;
  color: #ccc;
  z-index: 10;
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease;
}

.favorite-icon:hover {
  color: red;
}

.scale-up {
  transform: scale(1.3);
  color: red;
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