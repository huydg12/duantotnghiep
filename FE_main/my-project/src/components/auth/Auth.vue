<script setup>
import Header from "../layout/Header.vue";
import Footer from "../layout/Footer.vue";
import Banner from "../common/Banner.vue";
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const titleBanner = computed(() => {
    if (route.path.includes("/login")) return "Đăng Nhập";
    return "Đăng Ký";
});

const breadcrumbBanner = computed(() => {
    if (route.path.includes("/login")) return "Đăng Nhập";
    return "Đăng Ký";
});
function switchTab(tab) {
    router.push(`/auth/${tab}`);
}
</script>

<template>
    <Header />
    <Banner :title="titleBanner" :breadcrumb="breadcrumbBanner"
        backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
    <div class="bg-light">
        <div class="container">
            <div class="bg-white mx-auto mt-5 p-4 rounded shadow" style="max-width: 480px">
                <!-- Tabs -->
                <ul class="nav nav-tabs justify-content-center mb-4">
                    <li class="nav-item">
                        <button class="nav-link" :class="{ active: route.path.includes('/login') }"
                            @click="switchTab('login')">
                            Đăng nhập
                        </button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link" :class="{ active: route.path.includes('/register') }"
                            @click="switchTab('register')">
                            Đăng ký
                        </button>
                    </li>
                </ul>
                <router-view />
            </div>
        </div>
    </div>
    <Footer />
</template>

<style scoped></style>
