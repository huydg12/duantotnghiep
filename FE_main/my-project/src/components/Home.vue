<script setup>
import { computed, ref } from "vue";

// Mảng chứa tất cả sản phẩmm
const allProducts = ref([
    {
        id: 1,
        name: "Nike Air Force 1",
        price: 2500000,
        brand: "nike",
        image: "https://via.placeholder.com/300x200?text=Nike+AF1",
        featured: true,
    },
    {
        id: 2,
        name: "Adidas Ultraboost 22",
        price: 2900000,
        brand: "adidas",
        image: "https://via.placeholder.com/300x200?text=Ultraboost",
        featured: true,
    },
    {
        id: 3,
        name: "Converse Chuck 70",
        price: 1800000,
        brand: "converse",
        image: "https://via.placeholder.com/300x200?text=Chuck+70",
        featured: true,
    },
    {
        id: 4,
        name: "Vans Old Skool",
        price: 1600000,
        brand: "vans",
        image: "https://via.placeholder.com/300x200?text=Vans+Old+Skool",
        featured: true,
    },
    {
        id: 5,
        name: "Nike Dunk Low",
        price: 2700000,
        brand: "nike",
        image: "https://via.placeholder.com/300x200?text=Nike+Dunk",
    },
    {
        id: 6,
        name: "Nike Air Jordan 1",
        price: 3500000,
        brand: "nike",
        image: "https://via.placeholder.com/300x200?text=Jordan+1",
    },
    {
        id: 7,
        name: "Adidas Stan Smith",
        price: 2100000,
        brand: "adidas",
        image: "https://via.placeholder.com/300x200?text=Stan+Smith",
    },
    {
        id: 8,
        name: "Adidas NMD_R1",
        price: 3200000,
        brand: "adidas",
        image: "https://via.placeholder.com/300x200?text=NMD+R1",
    },
]);

// Lấy sản phẩm nổi bật
const featuredProducts = computed(() => {
    return allProducts.value.filter((p) => p.featured).slice(0, 4);
});

// State reactive để theo dõi brand được chọn
const selectedBrand = ref("nike");

// Lấy sản phẩm theo brand
const productsByBrand = computed(() => {
    return allProducts.value.filter((p) => p.brand == selectedBrand.value);
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
                    <div class="col-12 col-sm-6 col-md-3" v-for="product in featuredProducts" :key="product.id">
                        <div class="card h-100 product-card">
                            <img :src="product.image" class="card-img-top" :alt="product.name" />
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="cart-title">{{ product.name }}</h5>
                                <p class="product-price mt-1">
                                    {{ formatCurrency(product.price) }}
                                </p>
                                <button class="btn btn-buy mt-auto">Thêm vào giỏ</button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="text-center mt-5">
                    <a href="#tat-ca-san-pham" class="btn btn-dark px-4 py-2">Xem Tất Cả Sản Phẩm</a>
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
                    <button class="btn" :class="{ active: selectedBrand === 'nike' }" @click="selectBrand('nike')">
                        Nike
                    </button>
                    <button class="btn" :class="{ active: selectedBrand === 'adidas' }" @click="selectBrand('adidas')">
                        Adidas
                    </button>
                    <button class="btn" :class="{ active: selectedBrand === 'converse' }"
                        @click="selectBrand('converse')">
                        Converse
                    </button>
                    <button class="btn" :class="{ active: selectedBrand === 'vans' }" @click="selectBrand('vans')">
                        Vans
                    </button>
                </div>

                <div id="product-list-container">
                    <div class="row g-4">
                        <div class="col-6 col-md-3" v-for="product in productsByBrand" :key="product.id">
                            <div class="card h-100 product-card">
                                <img :src="product.image" class="card-img-top" :alt="product.name" />
                                <div class="card-body text-center">
                                    <h6 class="card-title">{{ product.name }}</h6>
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
</style>
