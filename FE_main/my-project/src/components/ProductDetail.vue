<script setup>
import { ref } from 'vue'

const imagesByColor = {
  "Trắng": [
    "./HinhAnh/anh1.webp",
    "./HinhAnh/anh2.webp",
    "./HinhAnh/anh3.webp",
    "./HinhAnh/anh4.webp",
    "./HinhAnh/anh5.webp",
    "./HinhAnh/anh6.webp"
  ],
  "Đen": [
    "./HinhAnh/anhd1.webp",
    "./HinhAnh/anhd2.webp",
    "./HinhAnh/anhd3.webp",
    "./HinhAnh/anhd4.webp",
    "./HinhAnh/anhd5.webp",
    "./HinhAnh/anhd6.webp"
  ]
}

const currentColor = ref("Trắng")
const images = ref(imagesByColor[currentColor.value])
const currentIndex = ref(0)

const showImage = (index) => {
  currentIndex.value = index
}

const prevImage = () => {
  currentIndex.value = (currentIndex.value - 1 + images.value.length) % images.value.length
}

const nextImage = () => {
  currentIndex.value = (currentIndex.value + 1) % images.value.length
}

const selectColor = (color) => {
  currentColor.value = color
  images.value = imagesByColor[color]
  currentIndex.value = 0
}
</script>
<template>
 <div class="container bg-white rounded-4 shadow p-4">
    <div class="row g-4 align-items-start">
      <!-- Hình ảnh -->
      <div class="col-md-6 position-relative">
        <button class="image-nav-btn start-0" onclick="prevImage()">&#8592;</button>
        <img id="mainProductImage" src="./HinhAnh/anh1.webp" alt="Sản phẩm" class="w-100 product-image rounded shadow">
        <button class="image-nav-btn end-0" onclick="nextImage()">&#8594;</button>
      </div>

      <!-- Thông tin sản phẩm -->
      <div class="col-md-6">
        <h2 class="fw-semibold mb-2">Nike Air Force 1 07</h2>
        <p class="text-muted mb-2">Thương hiệu: <strong>Nike</strong> | Mã: CW2288-001</p>
        <p class="text-danger fs-4 fw-bold mb-4">2.000.000₫</p>

        <p class="mb-3">
          Màu sắc:
          <button onclick="selectColor('Trắng')" class="color-btn">Trắng</button>
          <button onclick="selectColor('Đen')" class="color-btn">Đen</button>
        </p>

        <div class="mb-3">
          Kích thước:
          <button class="size-btn">40</button>
          <button class="size-btn">41</button>
          <button class="size-btn">42</button>
          <button class="size-btn">43</button>
        </div>

        <div class="mb-4" style="max-width: 150px;">
          <label class="form-label">Số lượng</label>
          <input type="number" value="1" min="1" class="form-control">
        </div>

        <div class="d-flex gap-3 mb-4">
          <button class="btn btn-primary product-button fw-semibold">Thêm vào giỏ</button>
          <button class="btn btn-danger product-button fw-semibold">Mua ngay</button>
        </div>

        <hr />
        <p class="text-muted">
          Nike Air Force 1 07 mang lại sự thay đổi mới mẻ nhưng vẫn giữ bản sắc OG: lớp phủ khâu đẹp, kết thúc sạch sẽ và đèn phản quang khiến bạn luôn tỏa sáng.
        </p>
      </div>
    </div>
  </div>

</template>

<style scoped>
    .product-image {
      height: 500px;
      object-fit: cover;
      object-position: center;
    }

    .image-nav-btn {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      background-color: rgba(107, 114, 128, 0.6);
      color: white;
      padding: 0.5rem;
      border-radius: 50%;
      border: none;
    }

    .image-nav-btn:hover {
      background-color: rgba(75, 85, 99, 0.8);
    }

    .product-button {
      min-width: 120px;
    }

    .color-btn, .size-btn {
      border: 1px solid #6c757d;
      font-size: 0.875rem;
      padding: 0.25rem 0.75rem;
      border-radius: 5px;
      margin: 0 0.25rem;
      background-color: white;
    }

    .color-btn:hover, .size-btn:hover {
      background-color: #e2e6ea;
    }

</style> 