<script setup>
import { ref, onMounted, computed, reactive, onUnmounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2'

const showAddressOverlay = ref(false);
const showAddAddressOverlay = ref(false);
const showUpdateAddressOverlay = ref(false);
const checkoutItems = ref([]);
const CustomerData = ref(null);
const note = ref(""); // Ghi chú đơn hàng
const userJson = localStorage.getItem("user");
let customerId = null;
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
const discountAmountList = ref([]);
const promotionCode = ref(""); // input của người dùng
const selectedPromotion = ref(null); // khuyến mãi tìm được
const errorMessage = ref(""); // nếu mã sai

const discountAmount = computed(() => {
  if (!selectedPromotion.value) return 0;

  const promo = selectedPromotion.value;
  const totalBeforeDiscount = subTotal.value + shippingFee.value;

  if (promo.type === 1) {
    // Giảm theo phần trăm
    const percent = Number(promo.value || 0);
    const discount = (totalBeforeDiscount * percent) / 100;
    return Math.floor(discount);
  } else if (promo.type === 2) {
    // Giảm số tiền cố định
    return Math.floor(Number(promo.value || 0));
  }

  return 0;
});

function handlePageShow(event) {
  if (event.persisted || performance.getEntriesByType("navigation")[0]?.type === "back_forward") {
    console.log('Reload triggered from pageshow')
    window.location.reload()
  }
}
const fetchPromotion = async () => {
  try {
    const response = await axios.get('http://localhost:8080/promotion/show');
    discountAmountList.value = response.data;
    console.log("dữ liệu khuyến mãi:", response.data);
  } catch (err) {
    console.error("Lỗi khuyến mãi", err);
  }
};
const applyPromotionCode = () => {
  const code = promotionCode.value.trim().toLowerCase();
  const now = new Date();
  now.setHours(0, 0, 0, 0); // so sánh theo ngày, bỏ giờ

  console.log("Danh sách khuyến mãi hiện có:", discountAmountList.value);
  console.log("Mã bạn nhập:", code);

  const promo = discountAmountList.value.find(p => {
    const start = new Date(p.startDate);
    const end = new Date(p.endDate);
    end.setHours(23, 59, 59, 999); // kết thúc trong ngày

    return (
      p.promotionCode.toLowerCase() === code &&
      p.status === 1 &&
      start <= now &&
      end >= now
    );
  });

  if (promo) {
    selectedPromotion.value = promo;
    errorMessage.value = "";
  } else {
    selectedPromotion.value = null;
    errorMessage.value = "Mã không hợp lệ hoặc đã hết hạn";
  }
};

const shippingFee = computed(() => {
  if (!defaultAddress.value || !defaultAddress.value.fullAddress) {
    return 0; // Nếu chưa chọn địa chỉ thì mặc định 0
  }

  const addressText = defaultAddress.value.fullAddress.toLowerCase();
  const isInHaiPhong = addressText.includes("hải phòng") || addressText.includes("haiphong");

  return isInHaiPhong ? 30000 : 50000;
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
    const fullAddress = `${detailAddress.value}, ${ward.name}, ${district.name}, ${province.name}`;

    if (!customerId) {
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

    resetAddressForm();
    closeAddAddressOverlay();
    await fetchAddressList();

    // Nếu cần, load lại danh sách địa chỉ
    // await fetchAddressList();

  } catch (error) {
    console.error(error);
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

  } catch (error) {
    console.error('Lỗi khi đặt mặc định:', error);

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

const grandTotal = computed(() => {
  const discount = discountAmount.value;
  return Math.max(subTotal.value + shippingFee.value - discount, 0);
});

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

    await fetchAddressList();
    closeUpdateAddressOverlay();
  } catch (err) {
    console.error('❌ Lỗi cập nhật địa chỉ:', err);

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
function base64EncodeUnicode(obj) {
  const jsonString = JSON.stringify(obj);
  const utf8Bytes = new TextEncoder().encode(jsonString);
  let binary = '';
  utf8Bytes.forEach((b) => binary += String.fromCharCode(b));
  return btoa(binary);
}

const createMoMoPayment = async () => {
  try {
    const extraData = base64EncodeUnicode(generateBillPayload());

    const response = await axios.post("http://localhost:5000/payment", {
      amount: grandTotal.value,
      extraData: extraData,
    });

    const payUrl = response.data.payUrl; // ✅ Lấy URL thanh toán từ phản hồi

    if (payUrl) {
      window.location.href = payUrl; // ✅ Chuyển hướng sang MoMo
    } else {
      alert("Không nhận được link thanh toán từ MoMo.");
    }
  } catch (error) {
    console.error("Lỗi tạo thanh toán MoMo:", error);
    alert("Không thể kết nối tới cổng thanh toán MoMo.");
  }
};
// Mapping phương thức thanh toán sang ID trong DB
const paymentMethodMapping = {
  CASH: 1,
  MOMO: 2,
  QR: 3
};

const getValidPromotion = () => {
  const today = new Date().toISOString().split("T")[0];

  return discountAmount.value.find(promo => {
    return promo.startDate <= today &&
      promo.endDate >= today &&
      promo.status === 1 &&
      promo.applyAll === true;
  });
};

const billCode = ref(`HD${Date.now()}`)  // ví dụ: "HD1721810123980"
// Hàm tạo billPayload động
const generateBillPayload = () => {
  const _subTotal = subTotal.value
  const _discountAmount = discountAmount.value || 0 // lấy từ computed
  const _shippingFee = shippingFee.value
  const _grandTotal = grandTotal.value

  const today = new Date().toISOString().split("T")[0]
  const getEstimatedDeliveryDate = () => {
    const addressText = defaultAddress.value?.fullAddress?.toLowerCase() || "";
    const isInHaiPhong = addressText.includes("hải phòng") || addressText.includes("haiphong");
    const daysToAdd = isInHaiPhong ? 2 : 5;
    const estimated = new Date();
    estimated.setDate(estimated.getDate() + daysToAdd);
    return estimated.toISOString().split("T")[0];
  };

  const billDetails = checkoutItems.value.map(item => ({
    productDetailId: item.productDetailId,
    quantity: item.quantity,
    price: item.price,
    status: 1,
    productImage: item.images,
    color: item.color,
    size: item.size,
    productName: item.productName
  }))

  // ✅ Thêm phần này để server callback có thể xóa cart
  const cartItems = checkoutItems.value.map(item => ({
    cartDetailId: item.cartDetailId
  }));

  return {
    customerId: customerId,
    employeeId: null,
    ptttId: paymentMethodMapping[selectedPaymentMethod.value] || 1, // Mặc định là CASH
    code: billCode.value, // auto code
    billType: "ONLINE",
    promotion: 2,
    status: 1,
    createdBy: customerId,
    createdDate: today,
    shippingDate: today,
    dateOfPayment: null,
    recipientName: defaultAddress.value?.fullName,
    recipientPhoneNumber: defaultAddress.value?.numberPhone,
    receiverAddress: defaultAddress.value?.fullAddress,
    addressMethod: "GIAO_TAN_NOI",
    estimatedDeliveryDate: getEstimatedDeliveryDate(),
    modifiedBy: null,
    modifiedDate: today,
    note: note.value,
    statusPayment: "CHUA_THANH_TOAN",
    subTotal: _subTotal,
    discountAmount: _discountAmount,
    shippingFee: _shippingFee,
    grandTotal: _grandTotal,
    billDetails: billDetails,
    cartItems: cartItems
  }
}

const qrJustCreated = ref(false);

const createBill = async () => {
  // ✅ Hiển thị popup loading bằng Swal
  Swal.fire({
    title: 'Đang xử lý thanh toán...',
    html: 'Vui lòng chờ trong giây lát...',
    allowOutsideClick: false,
    showConfirmButton: false,
    didOpen: () => {
      Swal.showLoading();
    }
  });

  localStorage.setItem("paymentSuccessFlag", "1");
  if (selectedPaymentMethod.value === 'QR') {
    amount.value = grandTotal.value;
    addInfo.value = billCode.value;
    await createQR();           // tạo QR
    qrJustCreated.value = true;
    Swal.close();
    return;                     // chưa gửi đơn hàng
  }
  if (selectedPaymentMethod.value === 'MOMO') {
    await new Promise(resolve => setTimeout(resolve, 5000));
    await createMoMoPayment(); // 🚀 Gọi MoMo
    Swal.close();
    return; // Không gọi sendBill vì sẽ xử lý ở callback
  }
  // Gửi đơn hàng như bình thường
  await sendBill();
  Swal.close();
};

const sendBill = async () => {
  try {
    const payload = generateBillPayload();
    const response = await axios.post("http://localhost:8080/bill/add", payload);
    const savedBill = response.data;

    // ✅ Trừ kho dựa vào billDetails
    if (Array.isArray(savedBill.billDetails)) {
      for (const detail of savedBill.billDetails) {
        if (detail.productDetailId && detail.quantity) {
          await axios.put(`http://localhost:8080/inventory/updateQuantityByPayMent/${detail.productDetailId}`, {
            quantity: detail.quantity
          });
        }
      }
    }

    // Xoá cart
    for (const item of checkoutItems.value) {
      if (item.cartDetailId !== undefined && item.cartDetailId !== null) {
        await axios.delete(`http://localhost:8080/cartDetail/delete/${item.cartDetailId}`);
      }
    }
    sessionStorage.removeItem("checkoutItems");

      window.location.href = "/invoicecustomer";  // Chuyển về trang invoicecustomer

  } catch (err) {
    console.error("Lỗi tạo đơn hàng:", err);
  }
};

const handleCloseQR = async () => {
  qrImage.value = null;

  // Nếu vừa tạo QR xong, thì giờ mới tạo đơn hàng
  if (qrJustCreated.value) {
    qrJustCreated.value = false;
    await sendBill();
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
const deleteAddress = async (id) => {
  const addressToDelete = addressList.value.find(addr => addr.id === id)

  // Nếu là mặc định thì không cho xóa
  if (addressToDelete.default) {
    alert("❌ Không thể xoá địa chỉ mặc định.\nVui lòng chọn địa chỉ khác làm mặc định trước.")
    return
  }

  if (!confirm('🗑️ Bạn có chắc chắn muốn xoá địa chỉ này?')) return;

  try {
    await axios.delete(`http://localhost:8080/address/delete/${id}`);
    addressList.value = addressList.value.filter(addr => addr.id !== id);
    alert("✅ Xoá địa chỉ thành công.")
  } catch (error) {
    console.error('❌ Lỗi khi xoá địa chỉ:', error);
    alert("Đã xảy ra lỗi khi xoá địa chỉ.")
  }
}

const qrImage = ref(null);
const amount = ref(0);
const addInfo = ref('')

const createQR = async () => {
  try {
    qrImage.value = null; // reset trước
    const res = await fetch('http://localhost:8081/api/generate-qr', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        bankCode: 'MB',
        accountNo: '0337030134',
        accountName: 'BUI VAN HIEU',
        amount: amount.value,
        addInfo: addInfo.value,
        acqId: '970422'
      })
    });

    const data = await res.json();

    // ✅ Nếu có ảnh QR
    if (data.qrImage) {
      qrImage.value = data.qrImage;
      console.log("🟢 QR Image set:", qrImage.value);
    } else {
      console.warn("⚠️ Không nhận được ảnh QR từ server");
    }

  } catch (err) {
    console.error(err);
  }
};

onMounted(() => {
  window.addEventListener('pageshow', handlePageShow)
  const stored = sessionStorage.getItem("checkoutItems");
  if (stored) {
    checkoutItems.value = JSON.parse(stored);
  }
  if (customerId) {
    findCustomerByAccountId();
    fetchAddressList();
  }
  fetchPromotion();
  fetchProvinces();
});
onUnmounted(() => {
  window.removeEventListener('pageshow', handlePageShow)
})

</script>

<template>
  <div class="container bg-white p-4 rounded shadow-sm">
    <!-- Địa chỉ nhận hàng -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="fw-semibold">Địa chỉ nhận hàng</h4>
      <button @click="openAddressOverlay" class="btn btn-outline-primary btn-sm">Thay đổi</button>
    </div>
    <div v-if="defaultAddress" class="border rounded bg-light p-3">
      <strong>{{ defaultAddress.fullName }}</strong> - {{ defaultAddress.numberPhone }}
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
      <div v-for="item in checkoutItems" :key="item.cartDetailId" class="row align-items-center border-bottom py-3">
        <div class="col-6 d-flex">
          <img :src="item.images" :alt="item.productName" class="img-thumbnail me-3" style="width: 80px;" />
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
        <input class="form-check-input" type="radio" name="paymentMethod" value="CASH" id="Cash"
          v-model="selectedPaymentMethod" checked>
        <label class="form-check-label" for="paymentCash">Thanh toán khi nhận hàng</label>
      </div>
      <div class="form-check mb-2">
        <input class="form-check-input" type="radio" name="paymentMethod" value="MOMO" id="momo"
          v-model="selectedPaymentMethod">
        <label class="form-check-label" for="paymentMomo">Ví MoMo</label>
      </div>
      <div class="form-check">
        <input class="form-check-input" type="radio" name="paymentMethod" value="QR" id="qr"
          v-model="selectedPaymentMethod">
        <label class="form-check-label" for="paymentQR">Quét mã QR ngân hàng</label>
      </div>
    </div>
    <div v-if="qrImage" class="qr-popup">
      <h3>Quét mã để thanh toán</h3>
      <img :src="qrImage" alt="QR Code" />
      <p><strong>Số tiền:</strong> {{ amount.toLocaleString() }} đ</p>
      <p><strong>Nội dung:</strong> {{ addInfo }}</p>
      <button @click="handleCloseQR()">Đã chuyển khoản</button>
    </div>

    <h5 class="mt-4 fw-medium">Mã khuyến mãi</h5>
    <div class="input-group mt-2">
      <input type="text" v-model="promotionCode" class="form-control" placeholder="Nhập mã giảm giá" />
      <button class="btn btn-outline-primary" @click="applyPromotionCode">
        Áp dụng
      </button>
    </div>
    <!-- Thông báo lỗi nếu mã không hợp lệ -->
    <p v-if="errorMessage" class="text-danger mt-1">{{ errorMessage }}</p>
    <!-- Ghi chú đơn hàng -->
    <h5 class="fw-semibold mt-4">Ghi chú đơn hàng</h5>
    <div class="border rounded bg-white p-3 mt-2">
      <div class="form-group">
        <label for="note" class="form-label">Ghi chú</label>
        <textarea class="form-control" id="note" name="note" rows="3" placeholder="Nhập ghi chú cho đơn hàng..."
          v-model="note"></textarea>
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
      <div class="d-flex justify-content-between mt-2">
        <span>Khuyến mãi:</span>
        <span>{{ formatCurrency(selectedPromotion ? -discountAmount : 0) }}</span>
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
    <div class="bg-white rounded shadow position-relative w-100 d-flex flex-column"
      style="max-width: 600px; height: 70vh;" @click.stop>
      <!-- Header: cố định -->
      <div class="p-4 border-bottom bg-white position-sticky top-0 z-2">
        <h5 class="fw-semibold m-0">Địa chỉ của tôi</h5>
        <button type="button" class="btn-close position-absolute top-0 end-0 m-3" aria-label="Đóng"
          @click="closeAddressOverlay"></button>
      </div>

      <!-- Body: cuộn -->
      <div class="px-4 pt-3 pb-2 overflow-auto flex-grow-1"> <!-- 👈 Cuộn tại đây -->
        <form @submit.prevent="confirmAddressSelection">
          <!-- Danh sách địa chỉ -->
          <div v-for="address in addressList" :key="address.id" class="border rounded p-3 mb-3 position-relative">
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
      <div class="p-3"> <!-- Giảm padding -->
        <h6 class="fw-semibold mb-3 text-center">Thêm địa chỉ mới</h6>

        <!-- Nút X -->
        <button type="button" class="btn-close position-absolute top-0 end-0 m-2" aria-label="Đóng"
          @click="closeAddAddressOverlay"></button>

        <form @submit.prevent="saveAddress">
          <!-- Họ và tên -->
          <div class="mb-2">
            <label class="form-label">Họ và tên người nhận</label>
            <input type="text" class="form-control form-control-sm" placeholder="Nhập họ tên" v-model="recipientName"
              required />
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
            style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.fullName" required />
        </div>

        <!-- Số điện thoại -->
        <div class="mb-2">
          <label class="form-label">Số điện thoại</label>
          <input type="text" class="form-control form-control-sm"
            style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" v-model="addressBeingEdited.numberPhone"
            required />
        </div>

        <!-- Tỉnh / Thành phố -->
        <div class="mb-2">
          <label class="form-label">Tỉnh / Thành phố</label>
          <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" required
            v-model="addressBeingEdited.cityCode" @change="handleCityChange">
            <option value="" disabled>-- Chọn tỉnh/thành phố --</option>
            <option v-for="province in provinces" :key="province.code" :value="province.code">
              {{ province.name }}
            </option>
          </select>
        </div>

        <!-- Quận / Huyện -->
        <div class="mb-2">
          <label class="form-label">Quận / Huyện</label>
          <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" required
            v-model="addressBeingEdited.districtCode" @change="handleDistrictChange" :disabled="!districts.length">
            <option disabled value="">-- Chọn quận/huyện --</option>
            <option v-for="district in districts" :key="district.code" :value="district.code">
              {{ district.name }}
            </option>
          </select>
        </div>

        <!-- Phường / Xã -->
        <div class="mb-2">
          <label class="form-label">Phường / Xã</label>
          <select class="form-select form-select-sm" style="font-size: 0.7rem; height: 28px; padding: 4px 8px;" required
            v-model="addressBeingEdited.wardCode" :disabled="!wards.length">
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



</template>

<style scoped>
.disabled-link {
  pointer-events: none;
  opacity: 0.8;
}

.qr-popup {
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 16px 20px;
  width: 280px;
  z-index: 9999;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  text-align: center;
  font-family: Arial, sans-serif;
}

.qr-popup h3 {
  font-size: 18px;
  margin-bottom: 12px;
}

.qr-popup img {
  max-width: 200px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.qr-popup p {
  margin: 6px 0;
  font-size: 14px;
}

.qr-popup button {
  margin-top: 10px;
  padding: 6px 16px;
  font-size: 14px;
  background-color: #dc3545;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.qr-popup button:hover {
  background-color: #c82333;
}
</style>
