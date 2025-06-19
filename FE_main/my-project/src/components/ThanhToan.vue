<script setup>
import { ref, onMounted } from 'vue';

const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);

const newAddressForm = ref(null);

// Mở popup chọn địa chỉ
const openAddressOverlay = () => {
  showAddressOverlay.value = true;
};

// Đóng popup chọn địa chỉ
const closeAddressOverlay = () => {
  showAddressOverlay.value = false;
};

// Mở popup thêm địa chỉ
const openAddAddressOverlay = () => {
  showAddAddressOverlay.value = true;
};

// Đóng popup thêm địa chỉ
const closeAddAddressOverlay = () => {
  showAddAddressOverlay.value = false;
  if (newAddressForm.value) newAddressForm.value.reset();
};

// Đóng popup khi click bên ngoài
const handleOverlayClick = (e) => {
  if (e.target.classList.contains('overlay-background')) {
    showAddressOverlay.value = false;
    showAddAddressOverlay.value = false;
    if (newAddressForm.value) newAddressForm.value.reset();
  }
};
</script>

<template>
  <div class="container bg-white p-4 rounded shadow-sm">
    <!-- Địa chỉ nhận hàng -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="fw-semibold">Địa chỉ nhận hàng</h4>
      <button @click="openAddressOverlay" class="btn btn-outline-primary btn-sm">Thay đổi</button>
    </div>
    <div class="border rounded bg-light p-3">
      <strong>Bùi Hiếu</strong> - (+84) 123456789 
      <span class="badge bg-primary disabled-link">Mặc định</span><br>
      Số A<br>
      Phường Kênh Dương, Quận Lê Chân, Hải Phòng
    </div>

    <!-- Sản phẩm -->
    <div class="row fw-semibold border-bottom py-2 mt-4">
      <div class="col-6">Sản phẩm</div>
      <div class="col-2 text-end">Đơn giá</div>
      <div class="col-2 text-center">Số lượng</div>
      <div class="col-2 text-end">Thành tiền</div>
    </div>
    <div class="row align-items-center border-bottom py-3">
      <div class="col-6 d-flex">
        <img src="./HinhAnh/anh1.webp" alt="Sản phẩm" class="img-thumbnail me-3" style="width: 80px;" />
        <div>
          <div class="fw-medium">Nike Air Force 1 07</div>
          <div class="text-muted small">Màu: Trắng | Size: 42</div>
        </div>
      </div>
      <div class="col-2 text-end">2.000.000₫</div>
      <div class="col-2 text-center">1</div>
      <div class="col-2 text-end fw-semibold">2.000.000₫</div>
    </div>

    <!-- Phương thức thanh toán -->
    <h5 class="fw-semibold mt-4">Phương thức thanh toán</h5>
    <div class="border rounded bg-white p-3 mt-2">
      <div class="form-check mb-2">
        <input class="form-check-input" type="radio" name="paymentMethod" value="COD" id="cod" checked>
        <label class="form-check-label" for="cod">Thanh toán khi nhận hàng (COD)</label>
      </div>
      <div class="form-check mb-2">
        <input class="form-check-input" type="radio" name="paymentMethod" value="MOMO" id="momo">
        <label class="form-check-label" for="momo">Ví MoMo</label>
      </div>
      <div class="form-check">
        <input class="form-check-input" type="radio" name="paymentMethod" value="QR" id="qr">
        <label class="form-check-label" for="qr">Quét mã QR ngân hàng</label>
      </div>
    </div>

    <!-- Mã khuyến mãi -->
    <h5 class="mt-4 fw-medium">Mã khuyến mãi</h5>
    <div class="input-group mt-2">
      <input type="text" class="form-control" placeholder="Nhập mã giảm giá">
      <button class="btn btn-outline-primary">Áp dụng</button>
    </div>

    <!-- Tổng kết -->
    <div class="border-top mt-4 pt-3">
      <h5 class="fw-medium">Thông tin đơn hàng</h5>
      <div class="d-flex justify-content-between mt-2">
        <span>Tạm tính:</span>
        <span>2.000.000₫</span>
      </div>
      <div class="d-flex justify-content-between">
        <span>Phí vận chuyển:</span>
        <span>30.000₫</span>
      </div>
      <div class="d-flex justify-content-between">
        <span>Khuyến mãi:</span>
        <span>-100.000₫</span>
      </div>
      <hr>
      <div class="d-flex justify-content-between fw-bold fs-5">
        <span>Tổng thanh toán:</span>
        <span>1.930.000₫</span>
      </div>
    </div>

    <!-- Nút thanh toán -->
    <div class="text-end mt-4">
      <button class="btn btn-success px-4">Thanh toán</button>
    </div>
  </div>

  <!-- Popup chọn địa chỉ -->
  <!-- <div
    v-show="showAddressOverlay"
    @click="handleOverlayClick"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center"
  >
    <div class="bg-white rounded p-4 shadow w-100" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
      <h5 class="fw-semibold mb-3">Địa chỉ của tôi</h5>
      <form>

        <div class="form-check border rounded p-3 mb-3 position-relative">
          <input class="form-check-input" type="radio" name="selectedAddress" value="1" checked>
          <label class="form-check-label ms-2">
            <strong>Bùi A</strong><br>
            <span class="text-muted small">(+84) 123 456 789</span><br>
            <span class="small">Số 6, Ngõ 292 Hào Khê<br>Phường Kênh Dương, Quận Lê Chân, Hải Phòng</span><br>
            <span class="badge bg-primary mt-2">Mặc định</span>
          </label>
          <span class="position-absolute top-0 end-0 me-2 mt-2 text-primary text-decoration-underline small" role="button">Cập nhật</span>
        </div>


        <div class="form-check border rounded p-3 mb-3 position-relative">
          <input class="form-check-input" type="radio" name="selectedAddress" value="2">
          <label class="form-check-label ms-2">
            <strong>Bùi Văn A</strong><br>
            <span class="text-muted small">(+84) 123 456 789</span><br>
            <span class="small">1 Bưu Điện, Xã An Lư<br>Huyện Thủy Nguyên, Hải Phòng</span>
          </label>
          <span class="position-absolute top-0 end-0 me-2 mt-2 text-primary text-decoration-underline small" role="button">Cập nhật</span>
        </div>

        <button type="button" class="btn btn-success w-100 mb-2" @click="openAddAddressOverlay">+ Thêm Địa Chỉ Mới</button>
        <div class="text-end">
          <button type="button" class="btn btn-secondary me-2" @click="closeAddressOverlay">Huỷ</button>
          <button type="submit" class="btn btn-success">Xác nhận</button>
        </div>
      </form>
    </div>
  </div> -->

  <!-- Popup thêm địa chỉ -->
  <!-- <div
    v-show="showAddAddressOverlay"
    @click="handleOverlayClick"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center"
  >
    <div class="bg-white rounded p-4 shadow w-100" style="max-width: 600px;">
      <h5 class="fw-semibold mb-3">Thêm địa chỉ mới</h5>
      <form ref="newAddressForm">
        <div class="mb-3">
          <label class="form-label">Họ và tên</label>
          <input type="text" required class="form-control">
        </div>
        <div class="mb-3">
          <label class="form-label">Số điện thoại</label>
          <input type="tel" required class="form-control">
        </div>
        <div class="mb-3">
          <label class="form-label">Địa chỉ</label>
          <textarea rows="3" required class="form-control"></textarea>
        </div>
        <div class="text-end">
          <button type="button" class="btn btn-secondary me-2" @click="closeAddAddressOverlay">Huỷ</button>
          <button type="submit" class="btn btn-success">Lưu</button>
        </div>
      </form>
    </div>
  </div> -->
</template>

<style scoped>
.disabled-link {
  pointer-events: none;
  opacity: 0.8;
}
</style>
