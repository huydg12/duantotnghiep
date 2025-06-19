<script setup>

import Logo from './components/HinhAnh/logo.png'
import Payment from './components/Payment.vue';
</script>

<template>
  <header class="header sticky-top">
    <nav class="navbar navbar-expand-lg container-xl">
      <div class="container-fluid">
        <a class="navbar-brand" href="#">
          <img :src="Logo" alt="Logo" />
        </a>

        <div class="collapse navbar-collapse justify-content-center">
          <ul class="navbar-nav gap-3">
            <li class="nav-item">
              <a class="nav-link active" aria-current="page" href="#">Trang Chủ</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#">Giới Thiệu</a>
            </li>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown"
                aria-expanded="false">
                Sản Phẩm
              </a>
              <ul class="dropdown-menu">
                <li><a class="dropdown-item" href="#">Adidas</a></li>
                <li><a class="dropdown-item" href="#">Nike</a></li>
                <li><a class="dropdown-item" href="#">Converse</a></li>
                <li><a class="dropdown-item" href="#">Vans</a></li>
                <li><a class="dropdown-item" href="#">Puma</a></li>
                <li><a class="dropdown-item" href="#">Fila</a></li>
              </ul>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#">Liên Hệ</a>
            </li>
          </ul>
        </div>

        <div class="d-flex align-items-center gap-4 header-tools">
          <div class="search-container">
            <i class="bi bi-search search-icon"></i>
            <input type="text" placeholder="Tìm kiếm..." class="search-input">
          </div>
          <a href="#" title="Yêu thích"><i class="bi bi-heart-fill"></i></a>
          <a href="#" title="Giỏ hàng"><i class="bi bi-bag-fill"></i></a>
          <a href="#" title="Đăng nhập"><i class="bi bi-person-circle"></i></a>
        </div>
      </div>
    </nav>
  </header>

  
<Payment/>

<footer class="footer pt-5 pb-4">
        <div class="container-xl">
            <div class="row g-5">
                <div class="col-lg-4 col-md-12">
                    <img :src="Logo" alt="Kix Store" style="height: 64px" class="mb-3" />
                    <p>
                        Kix Store – Nơi hội tụ những đôi sneaker đỉnh cao, đậm chất cá tính và thời trang.
                    </p>
                </div>
                <div class="col-lg-4 col-md-6">
                    <h5 class="mb-3 fw-bold">Liên kết nhanh</h5>
                    <ul class="list-unstyled space-y-2 footer-links">
                        <li><a href="#">Trang chủ</a></li>
                        <li><a href="#">Sản phẩm</a></li>
                        <li><a href="#">Giới thiệu</a></li>
                        <li><a href="#">Liên hệ</a></li>
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
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
</template>

