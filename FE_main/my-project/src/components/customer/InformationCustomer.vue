<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import axios from "axios";
import { RouterLink, useRouter } from "vue-router";
import Swal from 'sweetalert2'
import { useUserStore } from "@/stores/userStore";

const router = useRouter();
const showAddAddressOverlay = ref(false);
const showUpdateAddressOverlay = ref(false);

let customerId = null;
let accountId = null

const userJson = localStorage.getItem("user");
const addressList = ref([]);
const defaultAddress = ref(null);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const originalInfo = ref(null);
const selectedProvinceCode = ref(null);
const selectedDistrictCode = ref(null);
const selectedWardCode = ref(null);
const newAddressForm = ref(null);
const userStore = useUserStore();
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
    accountId = user.id;
    console.log("✅ Customer ID:", customerId);
    console.log("✅ Account ID:", accountId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
} else {
  console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user");
}

// Hàm đổi tab
function showTab(tabId) {
  activeTab.value = tabId;
}

// Khai báo reactive cho tab đang active
const activeTab = ref("info");

// Khai báo userInfo rỗng trước
const userInfo = reactive({
  fullName: "",
  gender: "",
  email: "",
  phone: "",
  birthDate: ""
});

// Hàm fetch dữ liệu từ API
const fetchUserInfo = async () => {
  try {
    const { data } = await axios.get(`http://localhost:8080/customer/showInfoCustomer/${customerId}`);

    const normalized = {
      fullName: data.fullName ?? "",
      gender: data.gender ?? "",
      email: data.email ?? "",
      phone: data.numberPhone ?? "",
      birthDate: data.birthOfDate ? data.birthOfDate.slice(0, 10) : ""
    };

    Object.assign(userInfo, normalized);      // đổ vào form
    originalInfo.value = { ...normalized };   // lưu bản gốc để so sánh
  } catch (error) {
    console.error("Lỗi khi fetch thông tin khách hàng:", error);
  }
};


const updateUserInfo = async () => {
  try {
    if (!customerId) {
      await Swal.fire({
        icon: "warning",
        title: "Thiếu thông tin đăng nhập",
        text: "Vui lòng đăng nhập lại.",
        didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
      });
      return;
    }

    const s = (v) => String(v ?? "").trim();
    const toISO = (v) => {
      if (!v) return "";
      const d = new Date(v);
      return isNaN(d) ? "" : d.toISOString().slice(0, 10);
    };

    const orig = originalInfo.value || {};

    // So sánh: đã validate từng trường ở chỗ khác rồi
    const noChange =
      s(userInfo.fullName) === s(orig.fullName) &&
      String(userInfo.gender ?? "") === String(orig.gender ?? "") &&
      s(userInfo.email) === s(orig.email) &&
      s(userInfo.phone) === s(orig.phone) &&
      toISO(userInfo.birthDate) === toISO(orig.birthDate);

    if (noChange) {
      await Swal.fire({
        icon: "info",
        title: "Không có thay đổi",
        text: "Bạn chưa cập nhật trường nào.",
        didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
      });
      return;
    }

    const payload = {
      fullName: s(userInfo.fullName),
      gender: userInfo.gender,
      email: s(userInfo.email),
      numberPhone: s(userInfo.phone),
      birthOfDate: toISO(userInfo.birthDate)
    };

    await axios.put(
      `http://localhost:8080/customer/updateInfoCustomer/${customerId}`,
      payload
    );

    // Cập nhật lại snapshot để lần sau so sánh chuẩn
    originalInfo.value = {
      fullName: payload.fullName,
      gender: payload.gender,
      email: payload.email,
      phone: payload.numberPhone,
      birthDate: payload.birthOfDate
    };

    await Swal.fire({
      icon: "success",
      title: "Cập nhật thành công",
      timer: 1500,
      showConfirmButton: false,
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
  } catch (error) {
    console.error("Lỗi khi cập nhật thông tin khách hàng:", error);
    await Swal.fire({
      icon: "error",
      title: "Cập nhật thất bại",
      text: error?.response?.data?.message || error?.message || "Vui lòng thử lại.",
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
  }
};

const fetchProvinces = async () => {
  try {
    const res = await axios.get("https://provinces.open-api.vn/api/?depth=2");
    provinces.value = res.data;
  } catch (err) {
    console.error("❌ Lỗi tải tỉnh/thành:", err);
  }
};

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

    // Gán vào wards riêng (nếu cần hiển thị ngoài UI)
    wards.value = data.wards || [];

    // Đồng thời cập nhật lại vào đúng district trong provinces
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

const fetchAddressList = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/address/showById/${customerId}`);
    addressList.value = response.data;
    // Gán default address
    defaultAddress.value = addressList.value.find(addr => addr.default === true);

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

    await fetchAddressList();

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
      districtName: district.name,
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
    await fetchAddressList();

    await Swal.fire({
      icon: "success",
      title: "Thêm địa chỉ thành công",
      timer: 1500,
      showConfirmButton: false,
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });

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
    const m = addressBeingEdited;
    const original = (addressList.value || []).find(a => a.id === m.id);

    if (original) {
      // Lấy tên địa giới từ code (fallback sang tên cũ nếu có)
      const cityNameNew = getCityNameByCode(m.cityCode) || m.cityName || original.cityName || "";
      const districtNameNew = getDistrictNameByCode(m.districtCode) || m.districtName || original.districtName || "";
      const wardNameNew = getWardNameByCode(m.wardCode) || m.wardName || original.wardName || "";

      // So sánh TRỰC TIẾP, không normalize
      const noChange =
        String(m.fullName ?? "") === String(original.fullName ?? "") &&
        String(m.numberPhone ?? "") === String(original.numberPhone ?? "") &&
        String(m.detailAddress ?? "") === String(original.detailAddress ?? "") &&
        cityNameNew === (original.cityName || "") &&
        districtNameNew === (original.districtName || "") &&
        wardNameNew === (original.wardName || "") &&
        (!!m.default === !!original.default);

      if (noChange) {
        await Swal.fire({
          icon: "info",
          title: "Không có thay đổi",
          text: "Bạn chưa thay đổi trường nào.",
          didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
        });
        return;
      }
    }

    // Tên địa giới phục vụ build fullAddress/payload
    const cityName = getCityNameByCode(m.cityCode) || m.cityName || "";
    const districtName = getDistrictNameByCode(m.districtCode) || m.districtName || "";
    const wardName = getWardNameByCode(m.wardCode) || m.wardName || "";

    const data = {
      customerId: customerId,
      fullName: m.fullName,
      numberPhone: m.numberPhone,
      fullAddress: `${m.detailAddress}, ${wardName}, ${districtName}, ${cityName}`,
      default: !!m.default,
      detailAddress: m.detailAddress,
      wardName: wardName,
      districtName: districtName,
      cityName: cityName,
    };

    const response = await fetch(`http://localhost:8080/address/update/${m.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Cập nhật địa chỉ thất bại');
    }

    await fetchAddressList();
    closeUpdateAddressOverlay();

    await Swal.fire({
      icon: "success",
      title: "Cập nhật địa chỉ thành công",
      timer: 1500,
      showConfirmButton: false,
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
  } catch (err) {
    console.error('❌ Lỗi cập nhật địa chỉ:', err);
    await Swal.fire({
      icon: "error",
      title: "Có lỗi xảy ra",
      text: err?.message || "Không thể cập nhật địa chỉ.",
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
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
  const item = addressList.value.find(addr => addr.id === id);

  if (!item) {
    await Swal.fire({
      icon: "warning",
      title: "Không tìm thấy địa chỉ",
      text: "Địa chỉ này không tồn tại hoặc đã bị xoá.",
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
    return;
  }

  // Không cho xoá địa chỉ mặc định
  if (item.default) {
    await Swal.fire({
      icon: "error",
      title: "Không thể xoá địa chỉ mặc định",
      text: "Vui lòng chọn địa chỉ khác làm mặc định trước.",
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
    return;
  }

  // Xác nhận
  const { isConfirmed } = await Swal.fire({
    icon: "warning",
    title: "Xoá địa chỉ này?",
    html: `
      <div class="text-start">
        <div><strong>${item.fullName}</strong> - ${item.numberPhone}</div>
        <div class="small text-muted mt-1">${item.fullAddress}</div>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: "Xoá",
    cancelButtonText: "Huỷ",
    reverseButtons: true,
    focusCancel: true,
    didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
  });

  if (!isConfirmed) return;

  try {
    await axios.delete(`http://localhost:8080/address/delete/${id}`);
    addressList.value = addressList.value.filter(addr => addr.id !== id);

    await Swal.fire({
      icon: "success",
      title: "Đã xoá địa chỉ",
      timer: 1500,
      showConfirmButton: false,
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
  } catch (error) {
    console.error("❌ Lỗi khi xoá địa chỉ:", error);
    await Swal.fire({
      icon: "error",
      title: "Xoá địa chỉ thất bại",
      text: error?.response?.data?.message || "Đã xảy ra lỗi khi xoá địa chỉ.",
      didOpen: () => { Swal.getContainer().style.zIndex = "20000"; }
    });
  }
};

const passwordData = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// state hiển thị message bằng <span> 
const formMsg = ref({ type: "", text: "" }); // type: success|error|warning
const setMsg = (type, text) => (formMsg.value = { type, text });

// validate 
const passwordMismatch = computed(
  () =>
    passwordData.newPassword !== "" &&
    passwordData.confirmPassword !== "" &&
    passwordData.newPassword !== passwordData.confirmPassword
);

const tooShort = computed(
  () => passwordData.newPassword.length > 0 && passwordData.newPassword.length < 6
);

const canSubmit = computed(
  () =>
    !!passwordData.currentPassword &&
    !!passwordData.newPassword &&
    !!passwordData.confirmPassword &&
    !passwordMismatch.value &&
    !tooShort.value
);

const submitting = ref(false);

const changePassword = async () => {
  if (passwordMismatch.value) {
    setMsg("warning", "Xác nhận mật khẩu không khớp. Vui lòng nhập lại.");
    return;
  }
  if (tooShort.value) {
    setMsg("warning", "Mật khẩu mới tối thiểu 6 ký tự.");
    return;
  }
  if (!accountId) {
    setMsg("error", "Thiếu thông tin tài khoản. Vui lòng đăng nhập lại.");
    return;
  }

  try {
    submitting.value = true;
    const payload = {
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword,
      confirmPassword: passwordData.confirmPassword,
    };
    await axios.put(
      `http://localhost:8080/account/changePassword/${accountId}`,
      payload
    );
    setMsg("success", "Đổi mật khẩu thành công!");
    // reset form
    passwordData.currentPassword = "";
    passwordData.newPassword = "";
    passwordData.confirmPassword = "";
    handleLogout();
  } catch (error) {
    const msg =
      error?.response?.data?.message ||
      error?.response?.data ||
      "Cập nhật thất bại. Vui lòng thử lại.";
    setMsg("error", String(msg));
  } finally {
    submitting.value = false;
  }
};

const handleLogout = () => {
  // Xóa localStorage và store
  userStore.logout();
  userInfo.value = null;

  // Điều hướng bằng replace để không quay lại được
  router.replace("/auth/login").then(() => {
    // Reload để clear cache nội dung đã xem
    window.location.reload();
  });
};

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
              <a href="#" class="nav-link" :class="{ active: activeTab === 'address' }"
                @click.prevent="showTab('address')">Địa chỉ nhận hàng</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'password' }"
                @click.prevent="showTab('password')">Đổi mật khẩu</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link text-danger" @click.prevent="handleLogout()">Đăng xuất</a>
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
                <input type="text" id="fullName" class="form-control" v-model="userInfo.fullName" required />
              </div>
              <div class="col-md-6">
                <label class="form-label d-block">Giới tính</label>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" value="true" v-model="userInfo.gender" required />
                  <label class="form-check-label">Nam</label>
                </div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" value="false" v-model="userInfo.gender" required />
                  <label class="form-check-label">Nữ</label>
                </div>
              </div>
              <div class="col-md-6">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" class="form-control" v-model="userInfo.email" required />
              </div>
              <div class="col-md-6">
                <label for="phone" class="form-label">Số điện thoại</label>
                <input type="text" id="phone" class="form-control" v-model="userInfo.phone" required
                  pattern="^(0[0-9]{9})$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0" />
              </div>
              <div class="col-md-6">
                <label for="birthDate" class="form-label">Ngày sinh</label>
                <input type="date" id="birthDate" class="form-control" v-model="userInfo.birthDate" required />
              </div>
            </div>
            <button type="submit" class="btn btn-dark mt-4">Cập nhật</button>
          </form>
        </div>

        <!-- Địa chỉ nhận hàng -->
        <div v-show="activeTab === 'address'" class="card p-4 shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="h5 mb-0">ĐỊA CHỈ NHẬN HÀNG</h3>
            <button type="button" class="btn btn-success fw-semibold py-1 px-3 shadow-sm rounded-2"
              @click="openAddAddressOverlay">
              <i class="bi bi-plus-circle me-1"></i> Thêm Địa Chỉ
            </button>
          </div>
          <h6 class="fw-semibold mb-3">Danh sách địa chỉ của tôi</h6>
          <!-- Phần danh sách địa chỉ có scroll -->
          <div class="bg-white rounded shadow-sm p-3 scroll-address-box" style="max-height: 250px; overflow-y: auto;">
            <form @submit.prevent="confirmAddressSelection">
              <div v-for="address in addressList" :key="address.id" class="border rounded p-3 mb-3">
                <div class="mb-2">
                  <strong>{{ address.fullName }}</strong><br />
                  <span class="text-muted small">{{ address.numberPhone }}</span><br />
                  <span class="small">{{ address.fullAddress }}</span>
                </div>

                <div class="d-flex justify-content-between align-items-center mt-2">
                  <div>
                    <span v-if="address.default" class="badge bg-primary">Mặc định</span>
                    <button v-else class="btn btn-outline-primary btn-sm" @click.prevent="setAsDefault(address)">
                      Chọn làm mặc định
                    </button>
                  </div>
                  <!-- Bên phải: 2 nút Xoá & Cập nhật sát nhau -->
                  <div class="d-flex gap-2">
                    <span class="text-danger text-decoration-underline small" role="button"
                      @click="deleteAddress(address.id)">
                      Xoá
                    </span>
                    <span class="text-primary text-decoration-underline small" role="button"
                      @click="openUpdateAddressOverlay(address)">
                      Cập nhật
                    </span>
                  </div>
                </div>
              </div>
            </form>
          </div>

          <!-- Popup thêm địa chỉ -->
          <div v-if="showAddAddressOverlay" @click="handleOverlayClick"
            class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
            <div class="bg-white rounded shadow position-relative w-100" style="max-width: 400px; font-size: 0.800rem;">
              <div class="p-3"> <!-- Giảm padding -->
                <h6 class="fw-semibold mb-3 text-center">Thêm địa chỉ mới</h6>

                <!-- Nút X -->
                <button type="button" class="btn-close position-absolute top-0 end-0 m-2" aria-label="Đóng"
                  @click="closeAddAddressOverlay"></button>

                <form @submit.prevent="saveAddress">
                  <!-- Họ và tên -->
                  <div class="mb-2">
                    <label class="form-label">Họ và tên người nhận</label>
                    <input type="text" class="form-control form-control-sm" placeholder="Nhập họ tên"
                      v-model="recipientName" required />
                  </div>

                  <!-- Số điện thoại -->
                  <div class="mb-2">
                    <label class="form-label">Số điện thoại</label>
                    <input type="tel" class="form-control form-control-sm" placeholder="Nhập số điện thoại"
                      v-model="phoneNumber" pattern="^(0[0-9]{9})$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
                      required />
                  </div>

                  <!-- Tỉnh / Thành phố -->
                  <div class="mb-2">
                    <label class="form-label">Tỉnh / Thành phố</label>
                    <select class="form-select form-select-sm" required v-model="selectedProvinceCode"
                      @change="fetchDistricts(selectedProvinceCode)">
                      <option value="" disabled selected>-- Chọn tỉnh/thành phố --</option>
                      <option v-for="province in provinces" :key="province.code" :value="province.code">
                        {{ province.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Quận / Huyện -->
                  <div class="mb-2">
                    <label class="form-label">Quận / Huyện</label>
                    <select class="form-select form-select-sm" required v-model="selectedDistrictCode"
                      @change="fetchWards(selectedDistrictCode)" :disabled="!districts.length">
                      <option value="" disabled selected>-- Chọn quận/huyện --</option>
                      <option v-for="district in districts" :key="district.code" :value="district.code">
                        {{ district.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Phường / Xã -->
                  <div class="mb-2">
                    <label class="form-label">Phường / Xã</label>
                    <select class="form-select form-select-sm" required v-model="selectedWardCode"
                      :disabled="!wards.length">
                      <option value="" disabled selected>-- Chọn phường/xã --</option>
                      <option v-for="ward in wards" :key="ward.code" :value="ward.code">
                        {{ ward.name }}
                      </option>
                    </select>
                  </div>

                  <!-- Địa chỉ chi tiết -->
                  <div class="mb-3">
                    <label class="form-label">Địa chỉ chi tiết</label>
                    <textarea class="form-control form-control-sm" rows="2" placeholder="Nhập địa chỉ cụ thể"
                      v-model="detailAddress" required></textarea>
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
          <div v-if="showUpdateAddressOverlay"
            class="position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 d-flex align-items-center justify-content-center"
            style="z-index: 9999">
            <div class="bg-white p-3 rounded shadow position-relative w-100"
              style="max-width: 400px; font-size: 0.7rem; height: 70vh;">
              <h5 class="fw-bold mb-3" style="font-size: 0.75rem;">Cập nhật địa chỉ</h5>

              <!-- Nút X -->
              <button type="button" class="btn-close position-absolute top-0 end-0 m-2" aria-label="Đóng"
                @click="closeUpdateAddressOverlay"></button>

              <form @submit.prevent="updateAddress">
                <!-- Họ tên -->
                <div class="mb-2">
                  <label class="form-label">Họ và tên</label>
                  <input type="text" class="form-control form-control-sm"
                    style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.fullName"
                    required />
                </div>

                <!-- Số điện thoại -->
                <div class="mb-2">
                  <label class="form-label">Số điện thoại</label>
                  <input type="text" class="form-control form-control-sm"
                    style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.numberPhone"
                    pattern="^(0[0-9]{9})$" required />
                </div>

                <!-- Tỉnh / Thành phố -->
                <div class="mb-2">
                  <label class="form-label">Tỉnh / Thành phố</label>
                  <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;"
                    required v-model="addressBeingEdited.cityCode" @change="handleCityChange">
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
                    required v-model="addressBeingEdited.districtCode" @change="handleDistrictChange"
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
                    required v-model="addressBeingEdited.wardCode" :disabled="!wards.length">
                    <option disabled value="">-- Chọn phường/xã --</option>
                    <option v-for="ward in wards" :key="ward.code" :value="ward.code">
                      {{ ward.name }}
                    </option>
                  </select>
                </div>

                <!-- Địa chỉ chi tiết -->
                <div class="mb-3 mt-2">
                  <label class="form-label">Địa chỉ chi tiết (số nhà, đường...)</label>
                  <textarea class="form-control form-control-sm" rows="2" style="font-size: 0.7rem; padding: 4px 8px;"
                    placeholder="Nhập địa chỉ cụ thể" v-model="addressBeingEdited.detailAddress" required></textarea>
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

        <!-- Đổi mật khẩu -->
        <div v-show="activeTab === 'password'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỔI MẬT KHẨU</h3>
          <!-- dòng thông báo tổng -->

          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <input type="password" id="oldPassword" placeholder="Mật khẩu hiện tại" class="form-control"
                v-model="passwordData.currentPassword" required />
            </div>
            <div class="mb-3">
              <input type="password" id="newPassword" placeholder="Mật khẩu mới" class="form-control"
                v-model="passwordData.newPassword" required />
              <span v-if="tooShort" class="text-danger small">Mật khẩu tối thiểu 6 ký tự.</span>
            </div>
            <div class="mb-3">
              <input type="password" id="confirmPassword" placeholder="Xác nhận mật khẩu mới" class="form-control"
                v-model="passwordData.confirmPassword" required />
              <!-- span không khớp -->
              <span v-if="passwordMismatch" class="text-danger small">Xác nhận mật khẩu không khớp.</span>
            </div>
            <button type="submit" class="btn btn-dark" :disabled="!canSubmit || submitting">
              {{ submitting ? "Đang xử lý..." : "Đặt lại mật khẩu" }}
            </button>
            <div class="mb-2" v-if="formMsg.text">
              <span :class="{
                'text-success': formMsg.type === 'success',
                'text-danger': formMsg.type === 'error',
                'text-warning': formMsg.type === 'warning'
              }" class="fw-semibold">
                {{ formMsg.text }}
              </span>
            </div>
          </form>
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
  padding-right: 6px;
  /* tránh che mất scrollbar */
}

.swal2-container {
  z-index: 20000 !important;
}
</style>
