<script setup>
function updateTotal() {}
function removeItem() {}
</script>
<template>
  <div class="container my-5 bg-white p-4 rounded shadow">
    <h3 class="mb-4 fw-semibold">🛒 Giỏ Hàng Của Bạn</h3>

    <!-- Danh sách sản phẩm -->
    <div id="cartItems">
      <!-- Sản phẩm mẫu -->
      <div class="row align-items-center g-3 mb-4 cart-item">
        <div class="col-12 col-md-2 text-center">
          <img
            src="./HinhAnh/anh1.webp"
            alt="Nike Air Force 1"
            class="img-fluid rounded"
            style="max-width: 96px; height: auto;"
          />
        </div>
        <div class="col-12 col-md-4">
          <h6 class="mb-1 fw-medium">Nike Air Force 1</h6>
          <small class="text-muted">Mã: CW2288-001 | Size: 42</small>
        </div>
        <div class="col-6 col-md-2">
          <p class="text-danger fw-semibold mb-0">2.000.000₫</p>
        </div>
        <div class="col-6 col-md-2">
          <input
            type="number"
            value="1"
            min="1"
            class="form-control text-center quantity"
            @change="updateTotal"
          />
        </div>
        <div class="col-12 col-md-2 text-md-end">
          <button class="btn btn-outline-danger btn-sm" @click="removeItem">
            Xóa
          </button>
        </div>
      </div>

      <!-- Thêm sản phẩm khác tại đây -->
    </div>

    <!-- Tổng cộng -->
    <div class="border-top pt-4 mt-4">
      <h5 class="text-end fw-semibold">
        Tổng cộng:
        <span id="totalPrice" class="text-danger fw-bold">2.000.000₫</span>
      </h5>
      <div class="mt-4 d-flex flex-column flex-md-row justify-content-between gap-2">
        <a href="home.html" class="btn btn-secondary text-center">
          ⬅️ Tiếp tục mua sắm
        </a>
        <a href="thanhToan.html" class="btn btn-success text-center">
          🧾 Thanh toán
        </a>
      </div>
    </div>
  </div>

</template>

<style scoped>


</style> 