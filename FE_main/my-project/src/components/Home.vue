<script setup>
import { onMounted, ref } from 'vue';

const productContainer = ref(null);

const products = {
    nike: { name: 'Nike Air Max', price: '2.300.000đ', text: 'Nike' },
    adidas: { name: 'Adidas Ultraboost', price: '2.900.000đ', text: 'Adidas' },
    converse: { name: 'Converse Classic', price: '1.200.000đ', text: 'Converse' },
    vans: { name: 'Vans Old Skool', price: '1.500.000đ', text: 'Vans' },
};

function createProductListHTML(brand, count) {
    let html = `<div class="product-list" id="${brand}-products"><div class="row g-4">`;
    const productData = products[brand];
    for (let i = 0; i < count; i++) {
        html += `
      <div class="col-6 col-md-3">
          <div class="card h-100 product-card">
              <img src="https://via.placeholder.com/300x200?text=${productData.text}" class="card-img-top" alt="${productData.name}">
              <div class="card-body text-center">
                  <h6 class="card-title">${productData.name}</h6>
                  <p class="product-price">${productData.price}</p>
              </div>
          </div>
      </div>
    `;
    }
    html += `</div></div>`;
    return html;
}

function showProducts(brand) {
    if (productContainer.value) {
        productContainer.value.innerHTML = createProductListHTML(brand, 8);
    }
}

onMounted(() => {
    const filterButtons = document.querySelectorAll('.filter-buttons .btn');

    filterButtons.forEach(button => {
        button.addEventListener('click', function () {
            const selectedBrand = this.getAttribute('data-brand');

            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            showProducts(selectedBrand);
        });
    });

    // Hiển thị sản phẩm mặc định
    showProducts('nike');
});
</script>
<template>
    <main>
        <section class="hero-section"
            style="background-image: url('https://allgoodtales.com/wp-content/uploads/2018/10/Nike-Header-min.jpg');">
            <div class="overlay">
                <div class="text-center text-white px-4">
                    <h1 class="display-5 fw-bold mb-3">Giày Xịn – Phong Cách Chất – Chill Hết Nấc</h1>
                    <p class="mb-5 fs-5">Đập hộp ngay đôi sneaker đỉnh cao dành riêng cho bạn</p>
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
                        Những mẫu giày hot trend, được giới trẻ săn đón – nổi bật về cả kiểu dáng lẫn chất lượng.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="card h-100 product-card">
                            <img src="https://via.placeholder.com/300x200" class="card-img-top" alt="Sản phẩm 1" />
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="card-title">Nike Air Max</h5>
                                <p class="product-price mt-1">2.500.000₫</p>
                                <button class="btn btn-buy mt-auto">Thêm vào giỏ</button>
                            </div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="card h-100 product-card">
                            <img src="https://via.placeholder.com/300x200" class="card-img-top" alt="Sản phẩm 2" />
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="card-title">Adidas Ultraboost</h5>
                                <p class="product-price mt-1">2.900.000₫</p>
                                <button class="btn btn-buy mt-auto">Thêm vào giỏ</button>
                            </div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="card h-100 product-card">
                            <img src="https://via.placeholder.com/300x200" class="card-img-top" alt="Sản phẩm 3" />
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="card-title">Converse Classic</h5>
                                <p class="product-price mt-1">1.200.000₫</p>
                                <button class="btn btn-buy mt-auto">Thêm vào giỏ</button>
                            </div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-md-3">
                        <div class="card h-100 product-card">
                            <img src="https://via.placeholder.com/300x200" class="card-img-top" alt="Sản phẩm 4" />
                            <div class="card-body d-flex flex-column text-center">
                                <h5 class="card-title">Vans Old Skool</h5>
                                <p class="product-price mt-1">1.600.000₫</p>
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
                    <button class="btn active" data-brand="nike">Nike</button>
                    <button class="btn" data-brand="adidas">Adidas</button>
                    <button class="btn" data-brand="converse">Converse</button>
                    <button class="btn" data-brand="vans">Vans</button>
                </div>

                <div id="product-list-container" ref="productContainer">
                </div>
            </div>
        </section>
    </main>



</template>

<style>
:root {
    --color-primary: #ef4444;
    --color-primary-dark: #dc2626;
    --color-dark: #000;
    --color-light: #fff;
    --color-secondary-text: #6c757d;
    --font-family-main: 'Be Vietnam Pro', sans-serif;
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
    /* h-48 */
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
    background-color: #ef4444;
    color: var(--color-light);
    font-weight: 500;
    transition: background-color 0.3s ease;
}

.btn-buy:hover {
    background-color: #dc2626;
    color: var(--color-light);
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