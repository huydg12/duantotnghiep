<script setup>
import Logo from "/images/logo.png";
</script>

<template>
    <footer class="footer pt-5 pb-4">
        <div class="container-xl">
            <div class="row g-5">
                <div class="col-lg-4 col-md-12">
                    <img :src="Logo" alt="Kix Store" style="height: 64px" class="mb-3" />
                    <p>
                        Kix Store – Nơi hội tụ những đôi sneaker đỉnh cao, đậm chất cá tính
                        và thời trang.
                    </p>
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
</template>

<style scoped></style>
