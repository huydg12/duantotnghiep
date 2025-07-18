<script setup>
import { ref, onMounted,computed,reactive   } from 'vue';
import axios from 'axios';
const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);
const showUpdateAddressOverlay = ref(false);
const checkoutItems = ref([]);
const CustomerData = ref(null);
const note = ref(""); // Ghi chú đơn hàng
const userJson = localStorage.getItem("user");
let customerId = null;
const discountAmount = 100000;
const shippingFee = 30000;
const addressList = ref([]);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const defaultAddress = ref(null);


const selectedProvinceCode = ref(null);
const selectedDistrictCode = ref(null);
const selectedWardCode = ref(null);


const recipientName = ref('');
const phoneNumber = ref('');
const detailAddress = ref('');
const isDefaultAddress = ref(false);
const addressBeingEdited = reactive({
  id: null,
  fullName: '',
  numberPhone: '',
  fullAddress: '',
  detailAddress: '',
  wardCode: '',
  districtCode: '',
  cityCode: '',
  default: false,
});
// Hàm normalize để so sánh tên không dấu
const normalize = (str) => {
  return str
    ?.normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
};
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
// ✅ Lấy danh sách tỉnh/thành phố (và districts cấp 2 luôn)
const fetchProvinces = async () => {
  try {
    const res = await axios.get("https://provinces.open-api.vn/api/?depth=2");
    provinces.value = res.data;
  } catch (err) {
    console.error("❌ Lỗi tải tỉnh/thành:", err);
  }
};

// ✅ Lấy danh sách quận/huyện từ mã tỉnh
const fetchDistricts = async (cityCode) => {
  try {
    const res = await axios.get(`https://provinces.open-api.vn/api/p/${cityCode}?depth=2`);
    const city = res.data;
    districts.value = city.districts || [];
  } catch (err) {
    console.error("❌ Lỗi khi tải quận/huyện:", err);
    districts.value = [];
  }
};

const fetchWards = async (districtCode) => {
  try {
    const response = await axios.get(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
    const data = response.data;

    // ✅ Gán vào wards riêng (nếu cần hiển thị ngoài UI)
    wards.value = data.wards || [];

    // ✅ Đồng thời cập nhật lại vào đúng district trong provinces
    for (const city of provinces.value) {
      const district = city.districts?.find(d => d.code === districtCode);
      if (district) {
        district.wards = data.wards || [];
        break;
      }
    }

    return data.wards || [];
  } catch (err) {
    console.error("❌ Lỗi khi tải phường/xã:", err);
    wards.value = [];
    return [];
  }
};




const saveAddress = async () => {
  try {
    const province = provinces.value.find(p => p.code === selectedProvinceCode.value);
    const district = districts.value.find(d => d.code === selectedDistrictCode.value);
    const ward = wards.value.find(w => w.code === selectedWardCode.value);

    if (!province || !district || !ward) {
      alert('Vui lòng chọn đầy đủ Tỉnh / Quận / Phường');
      return;
    }

    const fullAddress = `${detailAddress.value}, ${ward.name}, ${district.name}, ${province.name}`;

    if (!customerId) {
      alert('Không tìm thấy ID khách hàng');
      return;
    }

    const newAddress = {
      fullAddress: fullAddress,
      numberPhone: phoneNumber.value,
      fullName: recipientName.value,
      customerId: customerId,
      default: isDefaultAddress.value,
      detailAddress: detailAddress.value,
      wardName: ward.name,
      districtName:district.name,
      cityName: province.name
    };

    const response = await fetch('http://localhost:8080/address/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(newAddress)
    });

    if (!response.ok) throw new Error('Lỗi khi thêm địa chỉ!');

    const result = await response.json();
    console.log('Thêm địa chỉ thành công:', result);

    // Đóng popup và làm sạch form
    closeAddAddressOverlay();
    resetAddressForm();

    // Nếu cần, load lại danh sách địa chỉ
    // await fetchAddressList();

  } catch (error) {
    console.error(error);
    alert('Không thể thêm địa chỉ. Vui lòng thử lại!');
  }
};

const handleCityChange = () => {
  addressBeingEdited.districtCode = '';
  addressBeingEdited.wardCode = '';
  fetchDistricts(addressBeingEdited.cityCode);
};

const handleDistrictChange = () => {
  addressBeingEdited.wardCode = '';
  fetchWards(addressBeingEdited.districtCode);
};
 const setAsDefault = async (address) => {
    try {
      const response = await fetch(`http://localhost:8080/address/set-default/${address.id}`, {
        method: 'PUT',
      });
      console.log("📦 Địa chỉ được chọn để đặt mặc định:", address.id);

      if (!response.ok) {
        throw new Error('Lỗi khi đặt địa chỉ mặc định');
      }

    // ✅ Gọi lại fetchAddressList để cập nhật UI
    await fetchAddressList();    


      // ✅ Optional: Hiển thị thông báo
      alert('Đã chọn địa chỉ làm mặc định!');
    } catch (error) {
      console.error('Lỗi khi đặt mặc định:', error);
      alert('Không thể đặt địa chỉ làm mặc định!');
    }
  }

const resetAddressForm = () => {
  recipientName.value = '';
  phoneNumber.value = '';
  selectedProvinceCode.value = null;
  selectedDistrictCode.value = null;
  selectedWardCode.value = null;
  detailAddress.value = '';
  districts.value = [];
  wards.value = [];
  isDefaultAddress.value = false;
};
const subTotal = computed(() =>
  checkoutItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
);

const grandTotal = computed(() =>
  subTotal.value - discountAmount + shippingFee
);
const getCityNameByCode = (code) => {
  const city = (provinces.value || []).find(p => p.code === code);
  return city ? city.name : '';
};

const getDistrictNameByCode = (code) => {
  for (const city of provinces.value || []) {
    const district = (city.districts || []).find(d => d.code === code);
    if (district) return district.name;
  }
  return '';
};

const getWardNameByCode = (code) => {
  for (const city of provinces.value || []) {
    for (const district of city.districts || []) {
      const ward = (district.wards || []).find(w => w.code === code);
      if (ward) return ward.name;
    }
  }
  return '';
};
const updateAddress = async () => {
  try {
    const data = {
      customerId: customerId,
      fullName: addressBeingEdited.fullName,
      numberPhone: addressBeingEdited.numberPhone,
      fullAddress: `${addressBeingEdited.detailAddress}, ${getWardNameByCode(addressBeingEdited.wardCode)}, 
      ${getDistrictNameByCode(addressBeingEdited.districtCode)}, ${getCityNameByCode(addressBeingEdited.cityCode)}`,
      default: addressBeingEdited.default,
      detailAddress: addressBeingEdited.detailAddress,
      wardName: getWardNameByCode(addressBeingEdited.wardCode) || addressBeingEdited.wardName,
      districtName: getDistrictNameByCode(addressBeingEdited.districtCode) || addressBeingEdited.districtName,
      cityName: getCityNameByCode(addressBeingEdited.cityCode) || addressBeingEdited.cityName,
    };

    console.log("📦 Dữ liệu gửi đi:", data);

    const response = await fetch(`http://localhost:8080/address/update/${addressBeingEdited.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("⚠️ Response status:", response.status);
      console.error("📩 Response body:", errorText);
      throw new Error('Cập nhật địa chỉ thất bại');
    }

    alert('✅ Cập nhật địa chỉ thành công!');
    await fetchAddressList();
        closeUpdateAddressOverlay();
  } catch (err) {
    console.error('❌ Lỗi cập nhật địa chỉ:', err);
    alert('❌ Cập nhật địa chỉ thất bại');
  }
};
const fetchAddressList = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/address/showById/${customerId}`);
    addressList.value = response.data;
        // Gán default address
    defaultAddress.value = addressList.value.find(addr => addr.default === true);
        // 👉 GỌI lại danh sách địa chỉ
    await fetchAddressList();

    // 👉 Đóng popup và reset form
    closeAddAddressOverlay();
  } catch (error) {
    console.error('Lỗi khi lấy địa chỉ:', error);
  }
};



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
    recipientName: defaultAddress.value?.fullName,
    recipientPhoneNumber: defaultAddress.value?.numberPhone,
    receiverAddress: defaultAddress.value?.fullAddress,
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

const openUpdateAddressOverlay = async (address) => {
  console.log("🔍 Đang mở popup sửa địa chỉ:", address);

  // Tìm tỉnh/thành phố
  const city = provinces.value.find(p =>
    normalize(p.name) === normalize(address.cityName)
  );
  const cityCode = city?.code || null;
  console.log("📍 Mã tỉnh (cityCode):", cityCode, "| Tên tỉnh:", address.cityName);

  let districtCode = null;
  let wardCode = null;

  if (cityCode) {
    await fetchDistricts(cityCode); // Cập nhật danh sách quận/huyện

    // Tìm quận/huyện
    const district = (city?.districts || []).find(d =>
      normalize(d.name) === normalize(address.districtName)
    );
    districtCode = district?.code || null;
    console.log("🏙️ Mã quận (districtCode):", districtCode, "| Tên quận:", address.districtName);

    if (districtCode) {
      const wardList = await fetchWards(districtCode); // <-- CHỜ WARD THỰC SỰ TRẢ VỀ

      if (Array.isArray(wardList)) {
        const ward = wardList.find(w =>
          normalize(w.name) === normalize(address.wardName)
        );
        console.table(wardList.map(w => ({ code: w.code, name: w.name })));
        wardCode = ward?.code || null;
        console.log("🏡 Mã phường (wardCode):", wardCode, "| Tên phường:", address.wardName);
      } else {
        console.error("❌ wards không phải là mảng:", wardList);
      }
    }
  }

  // Gán dữ liệu vào form đang chỉnh sửa
  addressBeingEdited.id = address.id;
  addressBeingEdited.fullName = address.fullName;
  addressBeingEdited.numberPhone = address.numberPhone;
  addressBeingEdited.fullAddress = address.fullAddress;
  addressBeingEdited.cityCode = cityCode;
  addressBeingEdited.detailAddress = address.detailAddress;
  addressBeingEdited.districtCode = districtCode;
  addressBeingEdited.wardCode = wardCode;
  addressBeingEdited.default = address.default;

  // Hiển thị popup
  showUpdateAddressOverlay.value = true;
};
// Đóng popup sửa địa chỉ
const closeUpdateAddressOverlay = () => {
  showUpdateAddressOverlay.value = false;
  if (newAddressForm.value) newAddressForm.value.reset();
};

// Đóng popup khi click bên ngoài
const handleOverlayClick = (e) => {
  if (e.target.classList.contains('overlay-background')) {
    showAddressOverlay.value = false;
    showAddAddressOverlay.value = false;
    showUpdateAddressOverlay.value = false;
    if (newAddressForm.value) newAddressForm.value.reset();
  }
};
// Hàm format tiền VND
const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
};


onMounted(() => {
  const stored = sessionStorage.getItem("checkoutItems");
  if (stored) {
    checkoutItems.value = JSON.parse(stored);
  }
    if (customerId) {
    findCustomerByAccountId();
    fetchAddressList();
  }
    fetchProvinces();
});

</script>

<template>
  <div class="container bg-white p-4 rounded shadow-sm">
    <!-- Địa chỉ nhận hàng -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="fw-semibold">Địa chỉ nhận hàng</h4>
      <button @click="openAddressOverlay" class="btn btn-outline-primary btn-sm">Thay đổi</button>
    </div>
    <div v-if="defaultAddress" class="border rounded bg-light p-3">
      <strong>{{ defaultAddress.fullName }}</strong> -  {{ defaultAddress.numberPhone }}
      <span v-if="defaultAddress.default" class="badge bg-primary disabled-link">Mặc định</span><br>
      {{ defaultAddress.fullAddress }}
    </div>
    <div v-else class="alert alert-light d-flex justify-content-between align-items-center">
      <span>Không có địa chỉ mặc định.</span>
      <button class="btn btn-sm btn-primary" @click="openAddressOverlay">
        Thêm mới
      </button>
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
  <div v-if="showAddressOverlay" @click.self="closeAddressOverlay"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
    <div class="bg-white rounded p-4 shadow position-relative w-100" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
      <h5 class="fw-semibold mb-3">Địa chỉ của tôi</h5>

      <!-- Nút X -->
      <button
        type="button"
        class="btn-close position-absolute top-0 end-0 m-3"
        aria-label="Đóng"
        @click="closeAddressOverlay"
      ></button>

      <form @submit.prevent="confirmAddressSelection">

        <!-- Danh sách địa chỉ -->
        <div v-for="address in addressList" :key="address.id"
          class="border rounded p-3 mb-3 position-relative">
          <div class="mb-2">
            <strong>{{ address.fullName }}</strong><br>
            <span class="text-muted small">{{ address.numberPhone }}</span><br>
            <span class="small">{{ address.fullAddress }}</span>
          </div>

          <!-- Hiển thị Mặc định hoặc nút chọn -->
          <div class="d-flex justify-content-between align-items-center mt-2">
            <div>
              <span v-if="address.default" class="badge bg-primary">Mặc định</span>
              <button v-else class="btn btn-outline-primary btn-sm"
                @click.prevent="setAsDefault(address)">Chọn làm mặc định</button>
            </div>

            <span class="text-primary text-decoration-underline small" role="button"
              @click="openUpdateAddressOverlay(address)">Cập nhật</span>
          </div>
        </div>

        <!-- Nút thêm địa chỉ -->
        <button type="button" class="btn btn-success w-100 mb-2" @click="openAddAddressOverlay">
          + Thêm Địa Chỉ Mới
        </button>
      </form>
    </div>
  </div>

  <!-- Popup thêm địa chỉ -->
  <div
    v-if="showAddAddressOverlay"
    @click="handleOverlayClick"
    class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center"
  >
    <div class="bg-white rounded p-4 shadow position-relative w-100" style="max-width: 500px;">
      <h5 class="fw-semibold mb-3">Thêm địa chỉ mới</h5>

        <!-- Nút X -->
        <button
          type="button"
          class="btn-close position-absolute top-0 end-0 m-3"
          aria-label="Đóng"
          @click="closeAddAddressOverlay"
        ></button>
      <form @submit.prevent="saveAddress">
              <!-- Họ và tên -->
      <div class="mb-3">
        <label class="form-label">Họ và tên người nhận</label>
        <input
          type="text"
          class="form-control"
          placeholder="Nhập họ tên"
          v-model="recipientName"
          required
        />
      </div>

      <!-- Số điện thoại -->
      <div class="mb-3">
        <label class="form-label">Số điện thoại</label>
        <input
          type="tel"
          class="form-control"
          placeholder="Nhập số điện thoại"
          v-model="phoneNumber"
          pattern="^(0[0-9]{9})$"
          title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
          required
        />
      </div>
      <!-- Tỉnh / Thành phố -->
      <div class="mb-3">
        <label class="form-label">Tỉnh / Thành phố</label>
        <select class="form-select" required v-model="selectedProvinceCode" @change="fetchDistricts(selectedProvinceCode)">
          <option value="" disabled selected>-- Chọn tỉnh/thành phố --</option>
          <option v-for="province in provinces" :key="province.code" :value="province.code">
            {{ province.name }}
          </option>
        </select>
      </div>

      <!-- Quận / Huyện -->
      <div class="mb-3">
        <label class="form-label">Quận / Huyện</label>
        <select class="form-select" required v-model="selectedDistrictCode" @change="fetchWards(selectedDistrictCode)" :disabled="!districts.length">
          <option value="" disabled selected>-- Chọn quận/huyện --</option>
          <option v-for="district in districts" :key="district.code" :value="district.code">
            {{ district.name }}
          </option>
        </select>
      </div>

      <!-- Phường / Xã -->
      <div class="mb-3">
        <label class="form-label">Phường / Xã</label>
        <select class="form-select" required v-model="selectedWardCode" :disabled="!wards.length">
          <option value="" disabled selected>-- Chọn phường/xã --</option>
          <option v-for="ward in wards" :key="ward.code" :value="ward.code">
            {{ ward.name }}
          </option>
        </select>
      </div>

        <!-- Địa chỉ chi tiết -->
        <div class="mb-3 mt-3">
          <label class="form-label">Địa chỉ chi tiết (số nhà, đường...)</label>
          <textarea
            class="form-control"
            rows="2"
            placeholder="Nhập địa chỉ cụ thể"
            v-model="detailAddress"
            required
          ></textarea>
        </div>

        <!-- Nút lưu -->
        <div class="text-end">
          <button type="submit" class="btn btn-primary">Lưu địa chỉ</button>
        </div>
      </form>
    </div>
  </div>

<!-- Popup cập nhật địa chỉ -->
<div
  v-if="showUpdateAddressOverlay"
  class="position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 d-flex align-items-center justify-content-center"
  style="z-index: 9999"
>
  <div class="bg-white p-4 rounded shadow position-relative w-100" style="max-width: 500px;">
    <h5 class="fw-bold mb-3">Cập nhật địa chỉ</h5>

    <!-- Nút X -->
    <button
      type="button"
      class="btn-close position-absolute top-0 end-0 m-3"
      aria-label="Đóng"
      @click="closeUpdateAddressOverlay"
    ></button>

    <form @submit.prevent="updateAddress">
      <!-- Họ tên -->
      <div class="mb-3">
        <label class="form-label">Họ và tên</label>
        <input type="text" class="form-control" v-model="addressBeingEdited.fullName" required />
      </div>

      <!-- Số điện thoại -->
      <div class="mb-3">
        <label class="form-label">Số điện thoại</label>
        <input type="text" class="form-control" v-model="addressBeingEdited.numberPhone" required />
      </div>

      <!-- Tỉnh / Thành phố -->
      <div class="mb-3">
        <label class="form-label">Tỉnh / Thành phố</label>
        <select class="form-select"
                required
                v-model="addressBeingEdited.cityCode"
                @change="handleCityChange">
          <option value="" disabled>-- Chọn tỉnh/thành phố --</option>
          <option v-for="province in provinces" :key="province.code" :value="province.code">
            {{ province.name }}
          </option>
        </select>
      </div>

      <!-- Quận / Huyện -->
      <div class="mb-3">
        <label class="form-label">Quận / Huyện</label>
        <select class="form-select"
                required
                v-model="addressBeingEdited.districtCode"
                @change="handleDistrictChange"
                :disabled="!districts.length">
          <option disabled value="">-- Chọn quận/huyện --</option>
          <option v-for="district in districts" :key="district.code" :value="district.code">
            {{ district.name }}
          </option>
        </select>
      </div>

      <!-- Phường / Xã -->
      <div class="mb-3">
        <label class="form-label">Phường / Xã</label>
        <select class="form-select"
                required
                v-model="addressBeingEdited.wardCode"
                :disabled="!wards.length">
          <option disabled value="">-- Chọn phường/xã --</option>
          <option v-for="ward in wards" :key="ward.code" :value="ward.code">
            {{ ward.name }}
          </option>
        </select>
      </div>
      <!-- Địa chỉ chi tiết -->
      <div class="mb-3 mt-3">
        <label class="form-label">Địa chỉ chi tiết (số nhà, đường...)</label>
        <textarea
          class="form-control"
          rows="2"
          placeholder="Nhập địa chỉ cụ thể"
          v-model="addressBeingEdited.detailAddress"
          required
        ></textarea>
      </div>

      <div class="text-end">
        <button type="submit" class="btn btn-primary">Lưu</button>
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
