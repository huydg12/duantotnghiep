<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import axios from 'axios';

const currentCustomerId = ref(null);

const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);
const showUpdateAddressOverlay = ref(false);
const addressList = ref([]);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const defaultAddress = ref(null);
const selectedProvinceCode = ref(null);
const selectedDistrictCode = ref(null);
const selectedWardCode = ref(null);
const customers = ref([]);
const originalCustomer = ref(null);

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

// Lấy danh sách tỉnh/thành phố
const fetchProvinces = async () => {
  try {
    const res = await axios.get("https://provinces.open-api.vn/api/?depth=2");
    provinces.value = res.data;
  } catch (err) {
    console.error("❌ Lỗi tải tỉnh/thành:", err);
  }
};

// Lấy danh sách quận/huyện từ mã tỉnh
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

    const newAddress = {
      fullAddress: fullAddress,
      numberPhone: phoneNumber.value,
      fullName: recipientName.value,
      customerId: currentCustomerId.value,
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
    alert('Thêm địa chỉ thành công');

    await fetchAddressList(currentCustomerId.value);
    resetAddressForm();
    closeAddAddressOverlay();

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

    // Gọi lại fetchAddressList để cập nhật UI
    await fetchAddressList(currentCustomerId.value);

    // Optional: Hiển thị thông báo
    alert('Đã chọn địa chỉ làm mặc định!');
  } catch (error) {
    console.error('Lỗi khi đặt mặc định:', error);
    alert('Không thể đặt địa chỉ làm mặc định!');
  }
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
      // Lấy tên địa giới từ code
      const cityNameNew = getCityNameByCode(m.cityCode) || m.cityName || original.cityName || "";
      const districtNameNew = getDistrictNameByCode(m.districtCode) || m.districtName || original.districtName || "";
      const wardNameNew = getWardNameByCode(m.wardCode) || m.wardName || original.wardName || "";

      const noChange =
          String(m.fullName ?? "") === String(original.fullName ?? "") &&
          String(m.numberPhone ?? "") === String(original.numberPhone ?? "") &&
          String(m.detailAddress ?? "") === String(original.detailAddress ?? "") &&
          cityNameNew === (original.cityName || "") &&
          districtNameNew === (original.districtName || "") &&
          wardNameNew === (original.wardName || "") &&
          (!!m.default === !!original.default);

      if (noChange) {
        alert('Không có thay đổi nào.');
        return;
      }
    }

    // Tên địa giới phục vụ build fullAddress/payload
    const cityName = getCityNameByCode(m.cityCode) || m.cityName || "";
    const districtName = getDistrictNameByCode(m.districtCode) || m.districtName || "";
    const wardName = getWardNameByCode(m.wardCode) || m.wardName || "";

    const data = {
      customerId: currentCustomerId.value,
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
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Cập nhật địa chỉ thất bại');
    }

    await fetchAddressList(currentCustomerId.value); // ✅ truyền id vào
    closeUpdateAddressOverlay();

    alert('Cập nhật địa chỉ thành công');
  } catch (err) {
    console.error('❌ Lỗi cập nhật địa chỉ:', err);
  }
};

const fetchAddressList = async (id) => {
  try {
    const response = await axios.get(`http://localhost:8080/address/showById/${id}`);
    addressList.value = response.data;

    // Gán địa chỉ mặc định nếu có
    defaultAddress.value = addressList.value.find(addr => addr.default === true);

  } catch (error) {
    console.error('Lỗi khi lấy địa chỉ:', error);
  }
};

const newAddressForm = ref(null);

// Mở popup chọn địa chỉ
const openAddressOverlay = (id) => {
  currentCustomerId.value = id;
  fetchAddressList(id);          // gọi API lấy địa chỉ
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
      const wardList = await fetchWards(districtCode);

      if (Array.isArray(wardList)) {
        const ward = wardList.find(w =>
            normalize(w.name) === normalize(address.wardName)
        );
        console.table(wardList.map(w => ({code: w.code, name: w.name})));
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

const deleteAddress = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xoá địa chỉ này?')) return;

  try {
    console.log("ID: " + id);
    await axios.delete(`http://localhost:8080/address/delete/${id}`);
    // Xoá thành công, cập nhật lại danh sách
    addressList.value = addressList.value.filter(addr => addr.id !== id);
  } catch (error) {
    console.error('Lỗi khi xoá địa chỉ:', error);
    alert('Xoá địa chỉ thất bại. Vui lòng thử lại!');
  }
};

onMounted(() => {
  fetchProvinces();
});

const form = ref({
  id: null,
  accountId: null,
  fullName: '',
  gender: true,
  email: '',
  numberPhone: '',
  birthOfDate: '',
  active: true,
  createdDate: getVietnamDateTimeLocalFormat()
});
const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const search = reactive({
  keyword: "",     // tên / email / SĐT / accountId
  gender: "all",   // 'all' | 'true' | 'false'
  status: "all",   // 'all' | 'true' | 'false'  (active)
});

const filteredCustomers = computed(() => {
  const kw = normalize(String(search.keyword || ""));
  return (customers.value || []).filter((c) => {
    const haystacks = [
      String(c?.fullName ?? ""),
      String(c?.email ?? ""),
      String(c?.numberPhone ?? ""),
    ].map((v) => normalize(v));

    const matchKw = !kw || haystacks.some((v) => v.includes(kw));
    const matchGender =
        search.gender === "all" || String(c?.gender) === search.gender;
    const matchStatus =
        search.status === "all" || String(c?.active) === search.status;

    return matchKw && matchGender && matchStatus;
  });
});

// Khi thay đổi tìm kiếm thì quay về trang 1
watch(
    () => [search.keyword, search.gender, search.status],
    () => {
      currentPage.value = 1;
    },
    {deep: true}
);

// CRUD khách hàng
const fetchCustomer = async () => {
  try {
    const res = await axios.get('http://localhost:8080/customer/show');
    customers.value = res.data;
  } catch (error) {
    console.error('Lỗi lấy khách hàng:', error);
  }
};

function editCustomer(customer) {
  form.value = {
    ...customer,
    gender: customer.gender != null ? String(customer.gender) : "0", // mặc định Nam
    status: customer.status != null ? String(customer.status) : "1", // mặc định Hoạt động
    birthOfDate: customer.birthOfDate
        ? new Date(customer.birthOfDate).toISOString().split("T")[0]
        : ""
  };
  isEditing.value = true;
  originalCustomer.value = {...toComparable(form.value)};
}

const resetForm = () => {
  form.value = {
    id: null,
    accountId: null,
    fullName: '',
    gender: true,
    email: '',
    numberPhone: '',
    birthOfDate: '',
    active: true,
  };
  isEditing.value = false;
};

const toComparable = (x) => ({
  fullName: (x?.fullName ?? '').trim(),
  gender: String(x?.gender ?? ''),
  email: (x?.email ?? '').trim().toLowerCase(),
  numberPhone: String(x?.numberPhone ?? '').replace(/\s+/g, ''),
  birthOfDate: x?.birthOfDate ?? '',
  accountId: x?.accountId ?? null,
  active: typeof x?.active === 'boolean' ? x.active : (String(x?.active) === 'true')
});

const isSameCustomer = (a, b) => {
  if (!a || !b) return false;
  const keys = Object.keys(a);
  return keys.every(k => a[k] === b[k]);
};

const saveCustomer = async () => {
  try {
    console.log('Đang lưu khách hàng:', form.value);

    if (isEditing.value) {
      const before = originalCustomer.value;
      const now = toComparable(form.value);
      if (isSameCustomer(before, now)) {
        alert('Không có thay đổi nào để lưu.');
        return;
      }

      const res = await axios.put(`http://localhost:8080/customer/update/${form.value.id}`, form.value);
      alert('Cập nhật thành công');
      console.log('Kết quả update:', res.data);
    } else {
      const rep = await axios.post('http://localhost:8080/customer/add', form.value);
      alert('Thêm mới thành công');
      console.log('Kết quả thêm:', rep.data);
    }

    await fetchCustomer();
    resetForm();
  } catch (error) {
    console.log('Lỗi khi lưu khách hàng:', error);
    if (error.response) {
      console.log('Chi tiết lỗi:', error.response.data);
    }
  }
};

const changeStatus = async (id) => {
  if (!confirm('Bạn có chắc muốn chuyển trạng thái khách hàng này?')) return;

  const updateEmployee = {id};

  try {
    await axios.put(`http://localhost:8080/customer/updateStatus/${id}`, updateEmployee);
    alert("Đã chuyển trạng thái khách hàng");
    await fetchCustomer();
  } catch (error) {
    console.error('Lỗi chuyển trạng thái khách hàng:', error.response ? error.response.data : error.message);
    alert('Không thể chuyển trạng thái khách hàng');
  }
};

const formatDateTime = (str) => {
  const d = new Date(str);
  if (isNaN(d.getTime())) return '';
  return `${d.getDate().toString().padStart(2, '0')}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getFullYear()}`;
};

function getVietnamDateTimeLocalFormat() {
  const now = new Date();
  const vietnamOffset = 7 * 60;
  const localOffset = now.getTimezoneOffset();
  const totalOffset = vietnamOffset + localOffset;
  const vietnamTime = new Date(now.getTime() + totalOffset * 60000);
  return vietnamTime.toISOString().slice(0, 16);
}

const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredCustomers.value.length / pageSize))
);
const paginatedCustomers = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredCustomers.value.slice(start, start + pageSize);
});
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

onMounted(fetchCustomer);
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center mb-4 fw-bold">Quản Lý Khách Hàng</h2>

    <!-- Form khách hàng -->
    <form @submit.prevent="saveCustomer" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">Họ tên</label>
        <input v-model="form.fullName" required class="form-control"/>
      </div>
      <div class="mb-3">
        <label class="form-label d-block">Giới tính</label>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="gender" value="true" v-model="form.gender"/>
          <label class="form-check-label">Nam</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="gender" value="false" v-model="form.gender"/>
          <label class="form-check-label">Nữ</label>
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label">Email</label>
        <input v-model="form.email" type="email" required class="form-control"/>
      </div>
      <div class="mb-3">
        <label class="form-label">Số điện thoại</label>
        <input v-model="form.numberPhone" required class="form-control" pattern="^(0[0-9]{9})$"
               title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"/>
      </div>
      <div class="mb-3">
        <label class="form-label">Ngày sinh</label>
        <input type="date" v-model="form.birthOfDate" required class="form-control"/>
      </div>
      <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">{{ isEditing ? "Cập nhật" : "Thêm" }}</button>
        <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
      </div>
    </form>

    <!-- Tìm kiếm / Lọc -->
    <div class="border p-3 rounded bg-white mb-3">
      <div class="row g-2 align-items-end">
        <div class="col-sm-5">
          <label class="form-label small">Từ khóa</label>
          <input v-model="search.keyword" class="form-control" placeholder="Tìm theo tên, email, SĐT..."/>
        </div>

        <div class="col-sm-3">
          <label class="form-label small">Giới tính</label>
          <select v-model="search.gender" class="form-select">
            <option value="all">Tất cả</option>
            <option value="true">Nam</option>
            <option value="false">Nữ</option>
          </select>
        </div>

        <div class="col-sm-3">
          <label class="form-label small">Trạng thái</label>
          <select v-model="search.status" class="form-select">
            <option value="all">Tất cả</option>
            <option value="true">Hoạt động</option>
            <option value="false">Không hoạt động</option>
          </select>
        </div>

        <div class="col-sm-1 d-flex">
          <button type="button" class="btn btn-outline-secondary ms-auto"
                  @click="search.keyword = ''; search.gender = 'all'; search.status = 'all'">
            Xóa
          </button>
        </div>
      </div>

      <p class="text-muted small mb-0 mt-2">
        Tìm thấy {{ filteredCustomers.length }} khách hàng.
      </p>
    </div>

    <!-- Bảng khách hàng -->
    <table class="table table-bordered table-hover align-middle">
      <thead class="table-secondary text-center">
      <tr>
        <th>ID</th>
        <th>Account ID</th>
        <th>Họ tên</th>
        <th>Giới tính</th>
        <th>Email</th>
        <th>SĐT</th>
        <th>Ngày sinh</th>
        <th>Trạng thái</th>
        <th>Ngày tạo</th>
        <th>Hành động</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="customer in paginatedCustomers" :key="customer.id">
        <td class="text-center">{{ customer.id }}</td>
        <td class="text-center">{{ customer.accountId }}</td>
        <td class="text-center">{{ customer.fullName }}</td>
        <td class="text-center">{{ customer.gender === true ? 'Nam' : 'Nữ' }}</td>
        <td class="text-center">{{ customer.email }}</td>
        <td class="text-center">{{ customer.numberPhone }}</td>
        <td class="text-center">{{ formatDateTime(customer.birthOfDate) }}</td>
        <td class="text-center">
            <span class="badge" :class="customer.active === true ? 'bg-success' : 'bg-danger'">
              {{ customer.active === true ? 'Hoạt động' : 'Không hoạt động' }}
            </span>
        </td>
        <td class="text-center">{{ formatDateTime(customer.createdDate) }}</td>
        <td>
          <button class="btn btn-info btn-sm me-2" @click="openAddressOverlay(customer.id)">Xem địa chỉ</button>
          <button class="btn btn-success btn-sm me-2" @click="editCustomer(customer)">Sửa</button>
          <button class="btn btn-danger btn-sm" @click="changeStatus(customer.id)">Chuyển trạng thái</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Phân trang -->
    <nav>
      <ul class="pagination justify-content-center mt-4">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="goToPage(currentPage - 1)">«</button>
        </li>
        <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === currentPage }">
          <button class="page-link" @click="goToPage(page)">{{ page }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="goToPage(currentPage + 1)">»</button>
        </li>
      </ul>
    </nav>

    <!-- Popup chọn địa chỉ -->
    <div v-if="showAddressOverlay" @click.self="closeAddressOverlay"
         class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
      <div class="bg-white rounded shadow position-relative w-100 d-flex flex-column"
           style="max-width: 600px; height: 70vh;" @click.stop>
        <!-- Header: cố định -->
        <div class="p-4 border-bottom bg-white position-sticky top-0 z-2">
          <h5 class="fw-semibold m-0">Địa chỉ của tôi</h5>
          <button type="button" class="btn-close position-absolute top-0 end-0 m-3" aria-label="Đóng"
                  @click="closeAddressOverlay"></button>
        </div>

        <!-- Body: cuộn -->
        <div class="px-4 pt-3 pb-2 overflow-auto flex-grow-1">
          <form @submit.prevent="confirmAddressSelection">
            <!-- Danh sách địa chỉ -->
            <div v-for="address in addressList" :key="address.id" class="border rounded p-3 mb-3 position-relative">
              <div class="mb-2">
                <strong>{{ address.fullName }}</strong><br/>
                <span class="text-muted small">{{ address.numberPhone }}</span><br/>
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

        <!-- Footer: cố định -->
        <div class="p-4 border-top bg-white position-sticky bottom-0 z-2">
          <button type="button" class="btn btn-success w-100" @click="openAddAddressOverlay">
            + Thêm Địa Chỉ Mới
          </button>
        </div>
      </div>
    </div>

    <!-- Popup thêm địa chỉ -->
    <div v-if="showAddAddressOverlay" @click="handleOverlayClick"
         class="overlay-background position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 zindex-tooltip d-flex align-items-center justify-content-center">
      <div class="bg-white rounded shadow position-relative w-100" style="max-width: 400px; font-size: 0.800rem;">
        <div class="p-3">
          <h6 class="fw-semibold mb-3 text-center">Thêm địa chỉ mới</h6>

          <!-- Nút X -->
          <button type="button" class="btn-close position-absolute top-0 end-0 m-2" aria-label="Đóng"
                  @click="closeAddAddressOverlay"></button>

          <form @submit.prevent="saveAddress">
            <!-- Họ và tên -->
            <div class="mb-2">
              <label class="form-label">Họ và tên người nhận</label>
              <input type="text" class="form-control form-control-sm" placeholder="Nhập họ tên" v-model="recipientName"
                     required/>
            </div>

            <!-- Số điện thoại -->
            <div class="mb-2">
              <label class="form-label">Số điện thoại</label>
              <input type="tel" class="form-control form-control-sm" placeholder="Nhập số điện thoại"
                     v-model="phoneNumber" pattern="^(0[0-9]{9})$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
                     required/>
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
              <select class="form-select form-select-sm" required v-model="selectedWardCode" :disabled="!wards.length">
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
                   required/>
          </div>

          <!-- Số điện thoại -->
          <div class="mb-2">
            <label class="form-label">Số điện thoại</label>
            <input type="text" class="form-control form-control-sm"
                   style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.numberPhone"
                   pattern="^(0[0-9]{9})$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0" required/>
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
</template>

<style scoped>
.table-container {
  min-height: 300px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.5);
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 10px;
  width: 90%;
  max-width: 900px;
  max-height: 80vh;
  overflow-y: auto;
}

.pagination .active .page-link {
  background-color: #198754;
  border-color: #198754;
  color: #fff;
}
</style>
