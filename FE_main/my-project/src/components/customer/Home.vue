<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
const router = useRouter()
// Mảng chứa tất cả sản phẩmm
const products = ref([]);
const allProducts = ref([]);
const goToDetail = (id) => {
  router.push(`/productdetail/${id}`) // Chuyển hướng đến trang chi tiết sản phẩm với ID
}
const fetchTop4Products = async () => {
  try {
    const response = await axios.get('http://localhost:8080/product/top4') // Thay thế bằng API thực tế
    products.value = response.data
    console.log("sản phẩm nổi bật",products.value)
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm', error)
  }
}

const fetchAllProducts = async () => {
  try {
    const response = await axios.get('http://localhost:8080/product/showSPdto') // Thay thế bằng API thực tế
    allProducts.value = response.data
    console.log("sản phẩm", allProducts.value)
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm', error)
  }
}
onMounted(() => {
  fetchTop4Products();
  fetchAllProducts();
}) 


// State reactive để theo dõi brand được chọn
const selectedBrand = ref("nike");

// Lấy sản phẩm theo brand
const productsByBrand = computed(() => {
  return allProducts.value.filter(p => p.brandName?.toLowerCase() === selectedBrand.value.toLowerCase());
});

// Hàm để thay đổi brand khi click button
function selectBrand(brand) {
    selectedBrand.value = brand;
}

// Định dạng tiền
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
                                <button class="btn btn-buy mt-auto"@click="goToDetail(product.productId)">Xem chi tiết</button>
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
</style>
