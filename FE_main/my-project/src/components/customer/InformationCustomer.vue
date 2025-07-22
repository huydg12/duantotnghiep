<script setup>
import { ref, reactive, onMounted, computed, } from "vue";
import axios from "axios";
const showAddAddressOverlay = ref(false);
const showUpdateAddressOverlay = ref(false);
let customerId = null;
const userJson = localStorage.getItem("user");
const addressList = ref([]);
const defaultAddress = ref(null);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const selectedProvinceCode = ref(null);
const selectedDistrictCode = ref(null);
const selectedWardCode = ref(null);
const newAddressForm = ref(null);

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
// Khai báo reactive cho tab đang active
const activeTab = ref("info");


// Khai báo userInfo rỗng trước
const userInfo = reactive({
  fullName: "",
  email: "",
  phone: "",
  birthDate: ""
});
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
// Hàm fetch dữ liệu từ API
const fetchUserInfo = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/customer/showInfoCustomer/${customerId}`);
    const data = response.data;

    userInfo.fullName = data.fullName;
    userInfo.email = data.email;
    userInfo.phone = data.numberPhone; // key phải trùng với DTO
    userInfo.birthDate = data.birthOfDate?.slice(0, 10); // cắt yyyy-MM-dd
  } catch (error) {
    console.error("Lỗi khi fetch thông tin khách hàng:", error);
  }
};



const fetchAddressList = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/address/showById/${customerId}`);
    addressList.value = response.data;
        // Gán default address
    defaultAddress.value = addressList.value.find(addr => addr.default === true);

    // 👉 Đóng popup và reset form
    closeAddAddressOverlay();
  } catch (error) {
    console.error('Lỗi khi lấy địa chỉ:', error);
  }
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
  // Hàm normalize để so sánh tên không dấu
const normalize = (str) => {
  return str
    ?.normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
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
    await fetchAddressList();

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
// Mở popup thêm địa chỉ
const openAddAddressOverlay = () => {
  showAddAddressOverlay.value = true;
};

// Đóng popup thêm địa chỉ
const closeAddAddressOverlay = () => {
  showAddAddressOverlay.value = false;
  // if (newAddressForm.value) newAddressForm.value.reset();
};
// Đóng popup khi click bên ngoài
const handleOverlayClick = (e) => {
  if (e.target.classList.contains('overlay-background')) {
    showAddAddressOverlay.value = false;
    showUpdateAddressOverlay.value = false;
    if (newAddressForm.value) newAddressForm.value.reset();
  }
};
const deleteAddress = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xoá địa chỉ này?')) return;

  try {
    console.log("ID: " + id)
    await axios.delete(`http://localhost:8080/address/delete/${id}`);
    // Xoá thành công, cập nhật lại danh sách
    addressList.value = addressList.value.filter(addr => addr.id !== id);
  } catch (error) {
    console.error('Lỗi khi xoá địa chỉ:', error);
    alert('Xoá địa chỉ thất bại. Vui lòng thử lại!');
  }
};
const passwordData = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const orders = ref([
  {
    id: "#12345",
    date: "15/06/2025",
    address: "867 Trường Chinh, Kiến An",
    total: "1.200.000đ",
    status: "Đã thanh toán",
  },
  {
    id: "#12346",
    date: "18/06/2025",
    address: "29 Hào Khê, Lê Chân",
    total: "850.000đ",
    status: "Đang giao",
  },
]);

const addresses = ref([
  {
    id: 1,
    name: "Đào Gia Huy",
    address: "867 Trường Chinh, Kiến An, Hải Phòng",
    phone: "0123456789",
    isDefault: true,
  },
  {
    id: 2,
    name: "Đào Gia Huy",
    address: "29 Hào Khê, Lê Chân, Hải Phòng",
    phone: "0987654321",
    isDefault: false,
  },
]);

const newAddress = reactive({
  name: "",
  phone: "",
  address: "",
});

// Hàm đổi tab
function showTab(tabId) {
  activeTab.value = tabId;
}

function updateUserInfo() {
  alert("Đã cập nhật thông tin!");
}

function changePassword() {
  if (passwordData.newPassword !== passwordData.confirmPassword) {
    alert("Mật khẩu mới và xác nhận mật không khớp!");
  }
  alert("Đổi mật khẩu thành công!");
  passwordData.oldPassword = "";
  passwordData.newPassword = "";
  passwordData.confirmPassword = "";
}

function addNewAddress() {
  if (!newAddress.name || !newAddress.address || !newAddress.phone) {
    alert("Vui lòng điền đầy đủ thông tin địa chỉ!");
    return;
  }
  alert("Thêm địa chỉ mới thành công thành công!");
  newAddress.name = "";
  newAddress.address = "";
  newAddress.phone = "";
}

function logout() {
  if (confirm("Bạn có chắc muốn đăng xuất?")) {
    alert("Đã đăng xuất");
  }
}

onMounted(() => {
  fetchUserInfo();
  fetchAddressList();
  fetchProvinces();
});
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <!-- Sidebar -->
      <div class="col-lg-3 col-md-4">
        <div class="sidebar-nav">
          <h2 class="h5 mb-4">TRANG TÀI KHOẢN</h2>
          <p class="mb-4">
            Xin chào,
            <strong class="fw-semibold">{{ userInfo.fullName }}</strong>!
          </p>
          <ul class="nav nav-pills flex-column">
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'info' }"
                @click.prevent="showTab('info')">Thông tin tài khoản</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'orders' }"
                @click.prevent="showTab('orders')">Đơn hàng của bạn</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'password' }"
                @click.prevent="showTab('password')">Đổi mật khẩu</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'address' }"
                @click.prevent="showTab('address')">Địa chỉ nhận hàng</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link text-danger" @click.prevent="logout()">Đăng xuất</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Nội dung chính -->
      <div class="col-lg-7 col-md-7">
        <!-- Thông tin tài khoản -->
        <div v-show="activeTab === 'info'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">THÔNG TIN TÀI KHOẢN</h3>
          <form @submit.prevent="updateUserInfo">
            <div class="row g-3">
              <div class="col-md-6">
                <label for="fullName" class="form-label">Họ tên</label>
                <input type="text" id="fullName" class="form-control" v-model="userInfo.fullName" />
              </div>
              <div class="col-md-6">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" class="form-control" v-model="userInfo.email" />
              </div>
              <div class="col-md-6">
                <label for="phone" class="form-label">Số điện thoại</label>
                <input type="text" id="phone" class="form-control" v-model="userInfo.phone" />
              </div>
              <div class="col-md-6">
                <label for="birthDate" class="form-label">Ngày sinh</label>
                <input type="date" id="birthDate" class="form-control" v-model="userInfo.birthDate" />
              </div>
            </div>
            <button type="submit" class="btn btn-dark mt-4">Cập nhật</button>
          </form>
        </div>

        <!-- Đơn hàng -->
        <div v-show="activeTab === 'orders'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐƠN HÀNG CỦA BẠN</h3>
          <div class="table-responsive">
            <table class="table table-bordered align-middle">
              <thead class="table-light">
                <tr>
                  <th>Đơn hàng</th>
                  <th>Ngày</th>
                  <th>Địa chỉ</th>
                  <th>Giá trị</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="orders.length === 0">
                  <td colspan="6" class="text-center p-4">
                    Bạn chưa có đơn hàng nào
                  </td>
                </tr>
                <tr v-for="order in orders" :key="order.id">
                  <td>{{ order.id }}</td>
                  <td>{{ order.date }}</td>
                  <td>{{ order.address }}</td>
                  <td>{{ order.total }}</td>
                  <td>{{ order.status }}</td>
                  <td>
                    <a href="#" class="btn btn-link p-0 d-block text-start">Chi tiết</a>
                    <a href="#" class="btn btn-link p-0 d-block text-start text-warning" data-bs-toggle="modal"
                      data-bs-target="#trackingModal">Theo dõi</a>
                    <a href="#" class="btn btn-link p-0 d-block text-start text-success">Mua lại</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Đổi mật khẩu -->
        <div v-show="activeTab === 'password'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỔI MẬT KHẨU</h3>
          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <input type="password" id="oldPassword" placeholder="Mật khẩu cũ" class="form-control"
                v-model="passwordData.oldPassword" required />
            </div>
            <div class="mb-3">
              <input type="password" id="newPassword" placeholder="Mật khẩu mới" class="form-control"
                v-model="passwordData.newPassword" required />
            </div>
            <div class="mb-3">
              <input type="password" id="confirmPassword" placeholder="Xác nhận mật khẩu mới" class="form-control"
                v-model="passwordData.confirmPassword" required />
            </div>
            <button type="submit" class="btn btn-dark">Đặt lại mật khẩu</button>
          </form>
        </div>

        <!-- Địa chỉ nhận hàng -->
        <div v-show="activeTab === 'address'" class="card p-4 shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="h5 mb-0">ĐỊA CHỈ NHẬN HÀNG</h3>
            <button
              type="button"
              class="btn btn-success fw-semibold py-1 px-3 shadow-sm rounded-2"
              @click="openAddAddressOverlay"
            >
              <i class="bi bi-plus-circle me-1"></i> Thêm Địa Chỉ
            </button>
          </div>
            <h6 class="fw-semibold mb-3">Danh sách địa chỉ của tôi</h6>
          <!-- Phần danh sách địa chỉ có scroll -->
          <div
            class="bg-white rounded shadow-sm p-3 scroll-address-box"
            style="max-height: 250px; overflow-y: auto;"
          >
            <form @submit.prevent="confirmAddressSelection">
              <div
                v-for="address in addressList"
                :key="address.id"
                class="border rounded p-3 mb-3"
              >
                <div class="mb-2">
                  <strong>{{ address.fullName }}</strong><br />
                  <span class="text-muted small">{{ address.numberPhone }}</span><br />
                  <span class="small">{{ address.fullAddress }}</span>
                </div>

                <div class="d-flex justify-content-between align-items-center mt-2">
                  <div>
                    <span v-if="address.default" class="badge bg-primary">Mặc định</span>
                    <button
                      v-else
                      class="btn btn-outline-primary btn-sm"
                      @click.prevent="setAsDefault(address)"
                    >
                      Chọn làm mặc định
                    </button>
                  </div>
                    <!-- Bên phải: 2 nút Xoá & Cập nhật sát nhau -->
                    <div class="d-flex gap-2">
                      <span
                        class="text-danger text-decoration-underline small"
                        role="button"
                        @click="deleteAddress(address.id)"
                      >
                        Xoá
                      </span>
                      <span
                        class="text-primary text-decoration-underline small"
                        role="button"
                        @click="openUpdateAddressOverlay(address)"
                      >
                        Cập nhật
                      </span>
                    </div>
                </div>
              </div>
            </form>
          </div>

           
          <!-- Popup thêm địa chỉ -->
          <div
            v-if="showAddAddressOverlay"
            @click="handleOverlayClick"
            class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center"
          >
            <div
              class="bg-white rounded shadow position-relative w-100"
              style="max-width: 400px; font-size: 0.800rem;" 
            >
              <div class="p-3"> <!-- Giảm padding -->
                <h6 class="fw-semibold mb-3 text-center">Thêm địa chỉ mới</h6>

                <!-- Nút X -->
                <button
                  type="button"
                  class="btn-close position-absolute top-0 end-0 m-2"
                  aria-label="Đóng"
                  @click="closeAddAddressOverlay"
                ></button>

                <form @submit.prevent="saveAddress">
                  <!-- Họ và tên -->
                  <div class="mb-2">
                    <label class="form-label">Họ và tên người nhận</label>
                    <input
                      type="text"
                      class="form-control form-control-sm"
                      placeholder="Nhập họ tên"
                      v-model="recipientName"
                      required
                    />
                  </div>

                  <!-- Số điện thoại -->
                  <div class="mb-2">
                    <label class="form-label">Số điện thoại</label>
                    <input
                      type="tel"
                      class="form-control form-control-sm"
                      placeholder="Nhập số điện thoại"
                      v-model="phoneNumber"
                      pattern="^(0[0-9]{9})$"
                      title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
                      required
                    />
                  </div>

                  <!-- Tỉnh / Thành phố -->
                  <div class="mb-2">
                    <label class="form-label">Tỉnh / Thành phố</label>
                    <select
                      class="form-select form-select-sm"
                      required
                      v-model="selectedProvinceCode"
                      @change="fetchDistricts(selectedProvinceCode)"
                    >
                      <option value="" disabled selected>-- Chọn tỉnh/thành phố --</option>
                      <option
                        v-for="province in provinces"
                        :key="province.code"
                        :value="province.code"
                      >
                        {{ province.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Quận / Huyện -->
                  <div class="mb-2">
                    <label class="form-label">Quận / Huyện</label>
                    <select
                      class="form-select form-select-sm"
                      required
                      v-model="selectedDistrictCode"
                      @change="fetchWards(selectedDistrictCode)"
                      :disabled="!districts.length"
                    >
                      <option value="" disabled selected>-- Chọn quận/huyện --</option>
                      <option
                        v-for="district in districts"
                        :key="district.code"
                        :value="district.code"
                      >
                        {{ district.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Phường / Xã -->
                  <div class="mb-2">
                    <label class="form-label">Phường / Xã</label>
                    <select
                      class="form-select form-select-sm"
                      required
                      v-model="selectedWardCode"
                      :disabled="!wards.length"
                    >
                      <option value="" disabled selected>-- Chọn phường/xã --</option>
                      <option v-for="ward in wards" :key="ward.code" :value="ward.code">
                        {{ ward.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Địa chỉ chi tiết -->
                  <div class="mb-3">
                    <label class="form-label">Địa chỉ chi tiết</label>
                    <textarea
                      class="form-control form-control-sm"
                      rows="2"
                      placeholder="Nhập địa chỉ cụ thể"
                      v-model="detailAddress"
                      required
                    ></textarea>
                  </div>

                  <!-- Nút lưu -->
                  <div class="text-end">
                    <button type="submit" class="btn btn-sm btn-primary">Lưu địa chỉ</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
          <!-- Popup cập nhật địa chỉ -->
          <div
            v-if="showUpdateAddressOverlay"
            class="position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 d-flex align-items-center justify-content-center"
            style="z-index: 9999"
          >
            <div class="bg-white p-3 rounded shadow position-relative w-100" style="max-width: 400px; font-size: 0.7rem; height: 70vh;">
              <h5 class="fw-bold mb-3" style="font-size: 0.75rem;">Cập nhật địa chỉ</h5>

              <!-- Nút X -->
              <button
                type="button"
                class="btn-close position-absolute top-0 end-0 m-2"
                aria-label="Đóng"
                @click="closeUpdateAddressOverlay"
              ></button>

              <form @submit.prevent="updateAddress">
                <!-- Họ tên -->
                <div class="mb-2">
                  <label class="form-label">Họ và tên</label>
                  <input type="text" class="form-control form-control-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.fullName" required />
                </div>

                <!-- Số điện thoại -->
                <div class="mb-2">
                  <label class="form-label">Số điện thoại</label>
                  <input type="text" class="form-control form-control-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.numberPhone" required />
                </div>

                <!-- Tỉnh / Thành phố -->
                <div class="mb-2">
                  <label class="form-label">Tỉnh / Thành phố</label>
                  <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;"
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
                <div class="mb-2">
                  <label class="form-label">Quận / Huyện</label>
                  <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;"
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
                <div class="mb-2">
                  <label class="form-label">Phường / Xã</label>
                  <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;"
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
                <div class="mb-3 mt-2">
                  <label class="form-label">Địa chỉ chi tiết (số nhà, đường...)</label>
                  <textarea
                    class="form-control form-control-sm"
                    rows="2"
                    style="font-size: 0.7rem; padding: 4px 8px;"
                    placeholder="Nhập địa chỉ cụ thể"
                    v-model="addressBeingEdited.detailAddress"
                    required
                  ></textarea>
                </div>

                <div class="text-end">
                  <button type="submit" class="btn btn-sm btn-primary" style="font-size: 0.7rem; padding: 4px 12px;">
                    Lưu
                  </button>
                </div>
              </form>
            </div>
          </div>


        </div>

      </div>
    </div>
  </div>

  <!-- Modal Theo dõi đơn hàng -->
  <div class="modal fade" id="trackingModal" tabindex="-1" aria-labelledby="trackingModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header border-0">
          <h5 class="modal-title" id="trackingModalLabel">
            Theo dõi đơn hàng #DH123456
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="tracking-progress-bar mb-5">
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-check"></i>
              </div>
              <div class="tracking-label completed">Đã đặt</div>
              <div class="tracking-connector completed"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-box"></i>
              </div>
              <div class="tracking-label completed">Chuẩn bị</div>
              <div class="tracking-connector completed"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-shipping-fast"></i>
              </div>
              <div class="tracking-label completed">Đang giao</div>
              <div class="tracking-connector"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon">
                <i class="fas fa-check-double"></i>
              </div>
              <div class="tracking-label">Hoàn tất</div>
            </div>
          </div>
          <p><strong>Ngày đặt:</strong> 12/06/2025</p>
          <p>
            <strong>Trạng thái:</strong><span class="text-success fw-bold"> Đang giao</span>
          </p>
          <p><strong>Người nhận:</strong> Đào Gia Huy</p>
          <p><strong>Địa chỉ:</strong> 867 Trường Chinh, Kiến An, Hải Phòng</p>
        </div>
      </div>
    </div>
  </div>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
    integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6A=="
    crossorigin="anonymous" referrerpolicy="no-referrer" />
</template>

<style scoped>
body {
  background-color: #f8f9fa;
}

/* Kiểu chung cho các link trong sidebar */
.sidebar-nav .nav-link {
  color: #212529;
  padding: 0.6rem 1rem;
  border-radius: 0.375rem;
  transition: background-color 0.2s ease-in-out;
}

.sidebar-nav .nav-link:not(.active):hover {
  background-color: #e9ecef;
}

.sidebar-nav .nav-link.active {
  background-color: #e0e7ff;
  font-weight: 600;
  color: #212529;
}

.sidebar-nav .nav-link.text-danger {
  font-weight: 500;
}

.sidebar-nav .nav-link.text-danger:hover {
  background-color: #f8d7da;
  color: #58151c !important;
}

.actions-cell .btn {
  display: block;
  margin-bottom: 5px;
  text-align: left;
}

.tracking-progress-bar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.tracking-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  width: 80px;
}

.tracking-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 2;
  background-color: #6c757d;
}

.tracking-icon.completed {
  background-color: #198754;
}

.tracking-label {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #6c757d;
  text-align: center;
}

.tracking-label.completed {
  color: #198754;
}

.tracking-connector {
  height: 4px;
  width: 100%;
  background-color: #dee2e6;
  position: absolute;
  top: 18px;
  left: 50%;
  z-index: 1;
}

.tracking-connector.completed {
  background-color: #198754;
}
.scroll-address-box {
  max-height: 250px;
  overflow-y: auto;
  padding-right: 6px; /* tránh che mất scrollbar */
}
</style>
