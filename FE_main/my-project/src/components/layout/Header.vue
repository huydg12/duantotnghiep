<script setup>
import Logo from "/images/logo.png";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { ref, computed } from "vue";
import { useUserStore } from "@/stores/userStore";
import { onMounted } from "vue";

const userStore = useUserStore();
const router = useRouter();
const searchText = ref("");

const userInfo = ref(JSON.parse(localStorage.getItem("user")));
// Hàm đồng bộ từ localStorage → userStore
const syncUserFromStorage = () => {
    const userJson = localStorage.getItem("user");
    if (userJson) {
        try {
            const user = JSON.parse(userJson);
            userStore.setUser(user);
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

// Hàm xử lý khi nhấn Enter
const handleSearch = () => {
    if (searchText.value.trim()) {
        router.push({ path: "/product", query: { keyword: searchText.value } });
    }
};
// Khi app khởi tạo
onMounted(() => {
    syncUserFromStorage();
});
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
                        <li class="nav-item"><router-link class="nav-link active" to="/home">Trang Chủ</router-link>
                        </li>
                        <li class="nav-item"><router-link class="nav-link" to="/introduce">Giới Thiệu</router-link></li>
                        <li class="nav-item dropdown">
                            <div class="d-flex align-items-center">
                                <router-link class="nav-link pe-1" to="/product">Sản Phẩm</router-link>
                                <a class="nav-link dropdown-toggle ps-1" href="#" role="button"
                                    data-bs-toggle="dropdown"></a>
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
                        <i class="bi bi-search search-icon text-white" @click="handleSearch"
                            style="cursor: pointer;"></i>
                        <input type="text" placeholder="Tìm kiếm..." class="search-input form-control"
                            v-model="searchText" @keyup.enter="handleSearch" />
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
</template>

<style scoped></style>
