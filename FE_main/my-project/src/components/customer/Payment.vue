<script setup>
import { ref, onMounted,computed  } from 'vue';
import axios from 'axios';
const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);
const checkoutItems = ref([]);
const CustomerData = ref(null);
const note = ref(""); // Ghi chú đơn hàng
const userJson = localStorage.getItem("user");
let customerId = null;
const discountAmount = 100000;
const shippingFee = 30000;

const subTotal = computed(() =>
  checkoutItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
);

const grandTotal = computed(() =>
  subTotal.value - discountAmount + shippingFee
);

if (userJson) {
  try {
    const user = JSON.parse(userJson);
    customerId = user.customerId;
    console.log("✅ Customer ID:", customerId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
} else {
  console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user");
}
onMounted(() => {
  const stored = sessionStorage.getItem("checkoutItems");
  if (stored) {
    checkoutItems.value = JSON.parse(stored);
  }
    if (customerId) {
    findCustomerByAccountId();
  }
});


// Hàm tính toán tổng tiền hàng (subtotal)



const findCustomerByAccountId = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/customer/findByAccountId/${customerId}`);
    CustomerData.value = response.data[0];
    console.log("Thông tin khách hàng:", response.data);
  } catch (err) {
    console.error("Lỗi tìm kiếm khách hàng:", err);
  }
};

const selectedPaymentMethod = ref("CASH"); // Mặc định là CASH

// Mapping phương thức thanh toán sang ID trong DB
const paymentMethodMapping = {
  CASH: 1,
  MOMO: 2,
  QR: 3
};
// Hàm tạo billPayload động
const generateBillPayload = () => {
  const _subTotal = subTotal.value
  const _discountAmount = 100000 // sau này nếu có khuyến mãi thì thay vào
  const _shippingFee = 30000
  const _grandTotal = grandTotal.value

  const today = new Date().toISOString().split("T")[0]

  const billDetails = checkoutItems.value.map(item => ({
    productDetailId: item.productDetailId,
    quantity: item.quantity,
    price: item.price,
    status: 1,
    productImage: item.images,
    size: item.size,
    productName: item.productName
  }))

  return {
    customerId: customerId,
    employeeId: null,
    ptttId: paymentMethodMapping[selectedPaymentMethod.value] || 1, // Mặc định là CASH
    code: "HD" + Math.floor(Math.random() * 100000), // auto code
    billType: "ONLINE",
    status: 1,
    createdBy: customerId,
    createdDate: today,
    shippingDate: today,
    dateOfPayment: null,
    recipientName: CustomerData.value.fullName,
    recipientPhoneNumber: CustomerData.value.numberPhone,
    receiverAddress: null,
    addressMethod: "GIAO_TAN_NOI",
    estimatedDeliveryDate: today,
    modifiedBy: null,
    modifiedDate: today,
    note: note.value,
    statusPayment: "CHUA_THANH_TOAN",
    subTotal: _subTotal,
    discountAmount: _discountAmount,
    shippingFee: _shippingFee,
    grandTotal: _grandTotal,
    billDetails: billDetails
  }
}


const createBill = async () => {
  try {
    const payload = generateBillPayload();
    console.log("Payload gửi lên:", payload); // ✅ debug
    const response = await axios.post("http://localhost:8080/bill/add", payload);
    console.log("Đơn hàng đã tạo:", response.data);

    alert("Đặt hàng thành công!");
    sessionStorage.removeItem("checkoutItems"); // nếu có lưu local
  } catch (err) {
    console.error("Lỗi tạo đơn hàng:", err);
    alert("Đặt hàng thất bại!");
  }
};


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
// Hàm format tiền VND
const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
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

<div v-if="checkoutItems.length > 0">
  <div
    v-for="item in checkoutItems"
    :key="item.cartDetailId"
    class="row align-items-center border-bottom py-3"
  >
    <div class="col-6 d-flex">
      <img
        :src="item.images"
        :alt="item.productName"
        class="img-thumbnail me-3"
        style="width: 80px;"
      />
      <div>
        <div class="fw-medium">{{ item.productName }}</div>
        <div class="text-muted small">
          Màu: {{ item.color }} | Size: {{ item.size }}
        </div>
      </div>
    </div>
    <div class="col-2 text-end">{{ formatCurrency(item.price) }}</div>
    <div class="col-2 text-center">{{ item.quantity }}</div>
    <div class="col-2 text-end fw-semibold">
      {{ formatCurrency(item.price * item.quantity) }}
    </div>
  </div>
</div>

    <!-- Phương thức thanh toán -->
    <h5 class="fw-semibold mt-4">Phương thức thanh toán</h5>
    <div class="border rounded bg-white p-3 mt-2">
      <div class="form-check mb-2">
        <input class="form-check-input" type="radio" name="paymentMethod" value="CASH" id="Cash" v-model="selectedPaymentMethod" checked>
        <label class="form-check-label" for="paymentCash">Thanh toán khi nhận hàng</label>
      </div>
      <div class="form-check mb-2">
        <input class="form-check-input" type="radio" name="paymentMethod" value="MOMO" id="momo" v-model="selectedPaymentMethod">
        <label class="form-check-label" for="paymentMomo">Ví MoMo</label>
      </div>
      <div class="form-check">
        <input class="form-check-input" type="radio" name="paymentMethod" value="QR" id="qr" v-model="selectedPaymentMethod">
        <label class="form-check-label" for="paymentQR">Quét mã QR ngân hàng</label>
      </div>
    </div>
    <textarea
      class="form-control"
      id="note"
      name="note"
      rows="3"
      placeholder="Nhập ghi chú cho đơn hàng..."
      v-model="note"
  ></textarea>
    <!-- Mã khuyến mãi -->
    <h5 class="mt-4 fw-medium">Mã khuyến mãi</h5>
    <div class="input-group mt-2">
      <input type="text" class="form-control" placeholder="Nhập mã giảm giá">
      <button class="btn btn-outline-primary">Áp dụng</button>
    </div>
    <!-- Ghi chú đơn hàng -->
    <h5 class="fw-semibold mt-4">Ghi chú đơn hàng</h5>
    <div class="border rounded bg-white p-3 mt-2">
      <div class="form-group">
        <label for="note" class="form-label">Ghi chú</label>
        <textarea
          class="form-control"
          id="note"
          name="note"
          rows="3"
          placeholder="Nhập ghi chú cho đơn hàng..."
          v-model="note"
        ></textarea>
      </div>
    </div>
    <!-- Tổng kết -->
    <div class="border-top mt-4 pt-3">
      <h5 class="fw-medium">Thông tin đơn hàng</h5>
      <div class="d-flex justify-content-between mt-2">
        <span>Tạm tính:</span>
        <span>{{ formatCurrency(subTotal) }}</span>
      </div>
      <div class="d-flex justify-content-between">
        <span>Phí vận chuyển:</span>
        <span>{{ formatCurrency(shippingFee) }}</span>
      </div>
      <div class="d-flex justify-content-between">
        <span>Khuyến mãi:</span>
        <span>{{ formatCurrency(-discountAmount) }}</span>
      </div>
      <hr>
      <div class="d-flex justify-content-between fw-bold fs-5">
        <span>Tổng thanh toán:</span>
        <span>{{ formatCurrency(grandTotal) }}</span>
      </div>
    </div>

    <!-- Nút thanh toán -->
    <div class="text-end mt-4">
      <button class="btn btn-success px-4" @click="createBill">Thanh toán</button>
    </div>
  </div>
  
  <!-- Popup chọn địa chỉ -->
  <div v-if="showAddressOverlay" @click="handleOverlayClick"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
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
          <span class="position-absolute top-0 end-0 me-2 mt-2 text-primary text-decoration-underline small"
            role="button">Cập nhật</span>
        </div>


        <div class="form-check border rounded p-3 mb-3 position-relative">
          <input class="form-check-input" type="radio" name="selectedAddress" value="2">
          <label class="form-check-label ms-2">
            <strong>Bùi Văn A</strong><br>
            <span class="text-muted small">(+84) 123 456 789</span><br>
            <span class="small">1 Bưu Điện, Xã An Lư<br>Huyện Thủy Nguyên, Hải Phòng</span>
          </label>
          <span class="position-absolute top-0 end-0 me-2 mt-2 text-primary text-decoration-underline small"
            role="button">Cập nhật</span>
        </div>

        <button type="button" class="btn btn-success w-100 mb-2" @click="openAddAddressOverlay">+ Thêm Địa Chỉ
          Mới</button>
        <div class="text-end">
          <button type="button" class="btn btn-secondary me-2" @click="closeAddressOverlay">Huỷ</button>
          <button type="submit" class="btn btn-success">Xác nhận</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Popup thêm địa chỉ -->
  <div v-if="showAddAddressOverlay" @click="handleOverlayClick"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
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
  </div>

</template>

<style scoped>
.disabled-link {
  pointer-events: none;
  opacity: 0.8;
}
</style>
