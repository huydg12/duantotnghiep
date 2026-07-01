<script setup>
import { ref, computed, reactive, onMounted, watch } from "vue";
import axios from 'axios';
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
import { Modal } from "bootstrap";
import { nextTick } from 'vue';
import Swal from 'sweetalert2'
const store = useCartFavoriteStore()

const tabs = [
  { label: "Tất cả", value: "all" },
  { label: "Chờ xác nhận", value: "Chờ xác nhận" },
  { label: "Đã xác nhận", value: "Đã xác nhận" },
  { label: "Đang giao", value: "Đang giao" },
  { label: "Hoàn thành", value: "Hoàn Thành" },
  { label: "Đã hủy", value: "Đã hủy" },
  // { label: "Trả hàng/Hoàn tiền", value: "Trả hàng/Hoàn tiền" }
];

const currentTab = ref("all");
const searchQuery = ref("");
const orders = ref([]);
const isLoading = ref(false);
const errorMsg = ref("");

let customerId = ref(null);
let cartId = ref(null);
const cancelReasons = [
  'Đặt nhầm sản phẩm',
  'Không muốn mua nữa',
  'Thay đổi size',
  'Thời gian giao hàng lâu',
  'Tìm được giá tốt hơn',
  'Không còn nhu cầu',
  'Khác'
]

const selectedReasons = ref([])       // Các lý do được chọn
const otherReason = ref('')          // Lý do "Khác"
const isCancelSubmitting = ref(false) // Trạng thái submit
let cancelModalInstance = null

const cancelBillId = ref(null)        // ID của Bill
const prevNote = ref('')             // NOTE cũ để nối chuỗi
const isOtherSelected = computed(() => selectedReasons.value.includes('Khác'))

// Mở modal huỷ đơn
const openCancelModal = (order) => {
  // Log dữ liệu order và order.items để kiểm tra
  console.log("Order Data:", order);
  console.log("Order Items:", order.items);
  const resolvedBillId =
    order?.billId ??
    order?.id ??
    order?.BILL_ID ??
    order?.items?.[0]?.billId ?? null

  if (!Number.isFinite(Number(resolvedBillId))) {
    console.error("❌ Không tìm thấy billId hợp lệ trong order")
    Swal.fire('Lỗi', 'Không tìm thấy ID hoá đơn để huỷ.', 'error')
    return
  }

  cancelBillId.value = Number(resolvedBillId)
  prevNote.value = (order?.NOTE ?? order?.note ?? '').toString()
  selectedReasons.value = []
  otherReason.value = ''

  const el = document.getElementById('cancelOrderModal')
  cancelModalInstance = Modal.getOrCreateInstance(el)
  cancelModalInstance.show()
}

// Đóng modal huỷ đơn
const closeCancelModal = () => {
  if (cancelModalInstance) cancelModalInstance.hide()
}

// Tạo text lý do (không dính NOTE cũ)
const buildReasonText = () => {
  const reasons = selectedReasons.value.filter(r => r !== 'Khác')
  const hasOther = isOtherSelected.value
  const other = (otherReason.value || '').trim()

  if (hasOther && reasons.length === 0) return other // chỉ Khác
  if (!hasOther) return reasons.join(', ')          // chỉ các lý do thường
  return [reasons.join(', '), other].filter(Boolean).join(' | ') // cả hai
}

// Nối NOTE cũ + " | Lý do huỷ: ..."
const buildCancelNote = () => {
  const reasonText = buildReasonText()
  const tag = `Lý do huỷ: ${reasonText}`

  const base = (prevNote.value || '').trim()
  if (!base) return tag

  // tránh lặp nếu trước đó đã có "Lý do huỷ:"
  if (base.includes('Lý do huỷ:')) return base

  return `${base} | ${tag}`
}

// Cập nhật UI local (dùng ID bill và cập nhật status, note)
const updateLocalBill = () => {
  const sources = [orders.value, paginatedOrders.value] // Các mảng cần cập nhật

  for (const list of sources) {
    if (!Array.isArray(list)) continue
    const b = list.find(x => (x.id ?? x.billId ?? x.BILL_ID) === cancelBillId.value)
    if (b) {
      // Cập nhật trạng thái và ghi chú
      b.STATUS = 5
      b.status = 'Đã hủy'
      b.NOTE = buildCancelNote()  // Gắn NOTE mới
    }
  }
}
const updateInventory = async () => {
  try {
    const order = orders.value.find(order => order.id === cancelBillId.value);
    if (!order || !order.items || order.items.length === 0) {
      console.error("❌ Không tìm thấy sản phẩm trong đơn hàng");
      return;
    }
    console.log("Found order:", order); // In ra để kiểm tra đúng order
    console.log("Order items:", order.items); // In ra để kiểm tra các sản phẩm
    // Duyệt qua các sản phẩm trong đơn hàng
    for (const item of order.items) {
      const productDetailId = item.productDetailId;
      const quantityToUpdate = item.quantity;

      if (!productDetailId || !quantityToUpdate) {
        console.error("❌ Sản phẩm không có productDetailId hoặc số lượng không hợp lệ")
        continue
      }

      // Log chi tiết sản phẩm để kiểm tra
      console.log(`Cập nhật kho cho sản phẩm: ${productDetailId}, số lượng: ${quantityToUpdate}`)

      // Gọi API để cập nhật số lượng kho (sử dụng phương thức updateQuantity)
      const response = await axios.put(
        `http://localhost:8080/inventory/updateQuantity/${productDetailId}`,
        { quantity: quantityToUpdate }
      )

      console.log(`Cập nhật kho sản phẩm ${productDetailId}: ${quantityToUpdate} vào kho`, response.data)
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật kho:", error)
  }
}
// Hàm submit huỷ đơn
const submitCancel = async () => {
  const hasOther = isOtherSelected.value
  const reasonsCount = selectedReasons.value.filter(r => r !== 'Khác').length
  const other = (otherReason.value || '').trim()

  // Validate
  if (hasOther && !other) {
    Swal.fire('Thiếu ghi chú', 'Vui lòng nhập lý do ở ô "Khác".', 'warning')
    return
  }
  if (!hasOther && reasonsCount === 0) {
    Swal.fire('Thiếu thông tin', 'Vui lòng chọn ít nhất 1 lý do huỷ.', 'warning')
    return
  }

  if (!Number.isFinite(Number(cancelBillId.value))) {
    Swal.fire('Lỗi', 'ID hoá đơn không hợp lệ.', 'error')
    return
  }

  const confirm = await Swal.fire({
    title: 'Xác nhận huỷ đơn?',
    text: 'Đơn hàng sẽ chuyển sang trạng thái Đã huỷ.',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Huỷ đơn',
    cancelButtonText: 'Không'
  })
  if (!confirm.isConfirmed) return

  try {
    isCancelSubmitting.value = true

    const note = buildCancelNote() // NOTE mới

    // Gọi API để cập nhật trạng thái và NOTE của Bill
    await axios.put(`/bill/${cancelBillId.value}/cancel`, { note })

    // Cập nhật UI local (sử dụng các mảng orders/paginatedOrders)
    updateLocalBill()
    // Cập nhật số lượng kho cho từng sản phẩm trong Bill Detail
    Swal.fire('Đã huỷ đơn', 'Cảm ơn bạn đã cho biết lý do.', 'success')
    closeCancelModal()
  } catch (e) {
    console.error('Cancel error:', e.response?.data || e.message)
    Swal.fire('Lỗi', e.response?.data?.message || 'Không thể huỷ đơn. Vui lòng thử lại.', 'error')
  } finally {
    isCancelSubmitting.value = false
  }
}

// Lấy customerID từ localStorage
const getCustomerID = () => {
  const userJson = localStorage.getItem("user");
  if (!userJson) return null;
  try {
    const user = JSON.parse(userJson);
    return user?.customerId ?? null;
  } catch (error) {
    console.error("❌ Lỗi parse userJson:", error);
    return null;
  }
};
// Lấy cartID từ localStorage
const getCartId = () => {
  cartId = localStorage.getItem("cartId");
  if (!cartId) return null;  // Nếu không có giá trị, trả về null
  try {
    // Nếu cartId là kiểu chuỗi, bạn có thể chuyển nó thành số hoặc giữ nguyên tùy theo dữ liệu
    return cartId;  // Trả về cartId
  } catch (error) {
    console.error("❌ Lỗi khi lấy cartId từ localStorage:", error);
    return null;  // Trả về null nếu có lỗi
  }
};

// API
const fetchOrder = async () => {
  customerId = getCustomerID();
  if (!customerId) {
    errorMsg.value = "Không tìm thấy customerID.";
    return;
  }
  isLoading.value = true;
  errorMsg.value = "";
  try {
    const { data } = await axios.get(
      `http://localhost:8080/bill/invoicecustomer/${customerId}`,
      { withCredentials: true }
    );
    orders.value = Array.isArray(data) ? data : [];
    console.log(orders.value)
    // Option: sort mới nhất trước
    orders.value.sort((a, b) => new Date(b?.date || 0) - new Date(a?.date || 0));
  } catch (err) {
    console.error("❌ Lỗi khi lấy dữ liệu hóa đơn:", err);
    errorMsg.value = "Không thể tải danh sách đơn hàng. Vui lòng thử lại.";
  } finally {
    isLoading.value = false;
  }
};

// Lọc theo tab + search
const filteredOrders = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  return (orders.value || []).filter((order) => {
    const status = order?.status || "";
    const matchTab = currentTab.value === "all" || status === currentTab.value;

    const code = String(order?.code ?? "").toLowerCase();
    const items = Array.isArray(order?.items) ? order.items : [];
    const matchSearch =
      !q ||
      code.includes(q) ||
      items.some((i) =>
        String(i?.name ?? "").toLowerCase().includes(q)
      );
    return matchTab && matchSearch;
  });
});

// Phân trang + bảo toàn currentPage hợp lệ
const currentPage = ref(1);
const pageSize = 4;

const totalPages = computed(() => Math.max(1, Math.ceil(filteredOrders.value.length / pageSize)));


const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredOrders.value.slice(start, start + pageSize);
});

// Reset trang khi thay đổi filter/search
watch([currentTab, searchQuery, filteredOrders], () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = 1;
  }
});

// Debounce input search để mượt
let searchTimer;
watch(
  () => searchQuery.value,
  () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      currentPage.value = 1;
    }, 200);
  }
);

// Format
const formatCurrency = (v) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(v || 0);

const formatDateTime = (v) => {
  if (!v) return "";
  try {
    const d = new Date(v);
    return d.toLocaleDateString("vi-VN"); // Chỉ lấy ngày
  } catch {
    return v;
  }
};

// Mua lại sản phẩm
const addToCart = async (order) => {
  cartId = getCartId();

  try {
    if (!cartId || typeof cartId !== 'string') {
      console.error("❌ cartId không hợp lệ:", cartId);
      alert("Giỏ hàng không hợp lệ.");
      return;
    }

    // Kiểm tra đơn hàng có sản phẩm không
    if (!order || !order.items || order.items.length === 0) {
      alert("Đơn hàng không có sản phẩm để mua lại.");
      return;
    }

    // Log thông tin về sản phẩm đã chọn khi ấn "Mua lại"
    console.log("🛒 Sản phẩm trong đơn hàng:", order.items);
    order.items.forEach((item, index) => {
      console.log(`🔍 Sản phẩm ${index + 1}:`, item);
      console.log("🆔 productDetailId:", item.productDetailId);
      console.log("🔢 quantity:", item.quantity);
    });

    // Duyệt qua tất cả sản phẩm trong đơn hàng để tạo payload cho giỏ hàng
    const payloads = order.items.map(item => ({
      cartId: cartId, // Sử dụng cartId đã lấy
      productDetailId: item.productDetailId,
      quantity: item.quantity
    }));

    console.log("📦 Payload gửi lên /cartDetail/add:", payloads);

    // Kiểm tra từng phần tử riêng biệt
    payloads.forEach(payload => {
      // Kiểm tra có dữ liệu nào là undefined/null không
      if (!payload.cartId || !payload.productDetailId || !payload.quantity || payload.quantity <= 0) {
        alert("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
        return;
      }
    });

    // // Tiếp tục các bước xử lý khác (gọi API, thêm vào giỏ hàng...)
    for (let payload of payloads) {
      // Gọi API kiểm tra xem productDetail đã có trong giỏ chưa
      const checkUrl = `http://localhost:8080/cartDetail/exists?cartId=${encodeURIComponent(payload.cartId)}&productDetailId=${payload.productDetailId}`;
      const checkResponse = await axios.get(checkUrl);

      if (checkResponse.data === true) {
        // Đã tồn tại → cập nhật số lượng mới
        console.log("🔍 checkResponse.data:", checkResponse.data);
        const updatePayload = {
          cartId: payload.cartId,
          productDetailId: payload.productDetailId,
          quantity: payload.quantity
        };
        await axios.put('http://localhost:8080/cartDetail/updateQuantity', updatePayload);
        console.log("✅ Đã cập nhật số lượng trong giỏ");
      } else {
        // Chưa tồn tại → thêm mới
        await axios.post('http://localhost:8080/cartDetail/add', payload);
        console.log("✅ Đã thêm mới vào giỏ hàng");
      }
    }
    await store.fetchCartItems(customerId);
  } catch (error) {
    console.error("❌ Lỗi khi mua lại sản phẩm:", error);
    alert("Mua lại sản phẩm thất bại.");
  }
};

// Modal chi tiết hoá đơn
const selectedInvoice = ref(null);
const invoiceDetails = ref([]);
const modalInstance = ref(null);
const modal = ref(null);

const fetchInvoiceDetails = async (order) => {
  try {
    // Gọi API để lấy chi tiết hóa đơn
    const response = await axios.get(
      `http://localhost:8080/billDetail/show/${order.id}` // Sử dụng order.id
    );

    // Map dữ liệu thành định dạng bạn cần (nếu cần)
    invoiceDetails.value = response.data;
    console.log("bill cho tiết " + invoiceDetails.value)
  } catch (error) {
    console.error("Lỗi khi lấy chi tiết hóa đơn:", error);
  }
};

let statusInvoice = ref(null)
let billId = ref(null)
let quantity = ref(null)

let subTotal = ref(null)
let shippingFee = ref(null)
let discountAmount = ref(null)
let grandTotal = ref(null)
const selectedAddress = ref(null);
const openModal = async (order) => {
  if (!order || !order.id) {
    console.error("❌ Hóa đơn không có ID");
    return;
  }

  selectedInvoice.value = { ...order };
  statusInvoice = selectedInvoice.value.status;
  billId.value = selectedInvoice.value.id;  // Gán billId cho biến toàn cục
  console.log("Mã hoá đơn được chọn:", billId.value);
  order.items.forEach((item, index) => {
    subTotal = item.subTotal;
    shippingFee = item.shippingFee;
    discountAmount = item.discountAmount;
    grandTotal = subTotal + shippingFee - discountAmount;

    quantity = item.quantity;
  });

  await fetchInvoiceDetails(order);

  // Đảm bảo modal được mở sau khi lấy dữ liệu
  await nextTick();

  if (!modalInstance.value) {
    modalInstance.value = new Modal(modal.value); // Khởi tạo modal mỗi lần
  }

  modal.value.classList.remove("fade");

  modalInstance.value.show(); // Hiển thị modal
};

// Hàm lưu địa chỉ đã chọn
const saveSelectedAddress = async () => {
  if (!selectedAddress.value) {
    Swal.fire('Lỗi', 'Vui lòng chọn một địa chỉ', 'warning');
    return;
  }

  const address = addressList.value.find(addr => addr.id === selectedAddress.value); // Sửa thành addr.id === selectedAddress.value
  if (address) {
    console.log('Địa chỉ đã chọn:', address);

    // Gửi yêu cầu cập nhật địa chỉ vào hóa đơn
    const payload = {
      recipientName: address.fullName,
      recipientPhoneNumber: address.numberPhone,
      receiverAddress: address.fullAddress,
    };

    console.log("Payload gửi đi:", payload);

    try {
      const response = await axios.put(`http://localhost:8080/bill/updateAddressByBill/${billId.value}`, payload); // Sử dụng billId.value
      console.log("Response:", response);
      if (response.status === 200) {
        Swal.fire('Thành công', 'Địa chỉ đã được cập nhật!', 'success');
        closeAddressOverlay();
        await fetchOrder();  // Làm mới danh sách địa chỉ sau khi lưu thành công
      } else {
        console.log("Không có phản hồi thành công từ server", response);
      }
    } catch (error) {
      console.error("Lỗi cập nhật địa chỉ:", error);
      Swal.fire('Lỗi', 'Không thể cập nhật địa chỉ. Vui lòng thử lại.', 'error');
    }
  }
};



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


onMounted(() => {
  fetchOrder();
  if (customerId) {

    fetchAddressList();
  }
  fetchProvinces();


});
</script>

<template>
  <div class="orders">
    <!-- Tabs trạng thái -->
    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.value" class="tab" :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value; currentPage = 1">
        {{ tab.label }}
      </button>
    </div>

    <!-- Ô tìm kiếm -->
    <div class="search">
      <input type="text" v-model="searchQuery" placeholder="Tìm theo ID đơn hàng hoặc tên sản phẩm" />
    </div>

    <!-- Trạng thái tải/lỗi -->
    <div v-if="isLoading" class="order-card" style="padding:12px 14px;">
      Đang tải danh sách đơn hàng…
    </div>
    <div v-else-if="errorMsg" class="order-card" style="padding:12px 14px;color:#d0011b;">
      {{ errorMsg }}
    </div>

    <!-- Rỗng -->
    <div v-else-if="!paginatedOrders.length" class="order-card" style="padding:12px 14px;">
      Bạn chưa có đơn hàng nào !!!
    </div>

    <!-- Danh sách đơn hàng -->
    <div v-for="(order, idx) in paginatedOrders" :key="idx" class="order-card">
      <!-- Header -->
      <div class="order-header">
        <div class="order-meta">
          <span class="order-code">Mã đơn: #{{ order.code }}</span>
          <span class="order-date">• {{ formatDateTime(order.date) }}</span>
        </div>
        <div class="order-status">{{ order.status }}</div>
      </div>

      <!-- Sản phẩm -->
      <div v-for="(item, i) in order.items" :key="i" class="order-item">
        <div class="item-left">
          <img :src="item.image" alt="" />
          <div class="item-info">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-variant">Phân loại: Size: {{ item.size }} - Màu: {{ item.color }}</div>
            <div class="item-qty">x{{ item.quantity }}</div>
          </div>
        </div>
        <div class="item-price">
          <span v-if="item.oldPrice" class="old">{{ formatCurrency(item.oldPrice) }}</span>
          <span class="new">{{ formatCurrency(item.price * item.quantity) }}</span>
        </div>
      </div>

      <!-- Footer -->
      <div class="order-footer">
        <div class="hint"></div>
        <div class="actions">
          <div class="total">
            Thành tiền: <span class="total-number">{{ formatCurrency(order.total) }}</span>
          </div>
          <button v-if="order.status === 'Hoàn Thành'" type="button" class="btn btn-primary" @click="addToCart(order)">
            Mua lại
          </button>
          <button v-if="order.status === 'Chờ xác nhận'" type="button" class="btn btn-danger"
            @click="openCancelModal(order)">
            Huỷ đơn
          </button>
          <button class="btn btn-outline" @click="openModal(order)">Xem chi tiết</button>
        </div>
      </div>
    </div>
    <!-- Modal Huỷ đơn -->
    <div class="modal fade" id="cancelOrderModal" tabindex="-1" aria-labelledby="cancelOrderLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 id="cancelOrderLabel" class="modal-title">Chọn lý do huỷ đơn</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" :disabled="isCancelSubmitting"></button>
          </div>

          <div class="modal-body">
            <p class="mb-2 text-muted">Bạn có thể chọn nhiều lý do:</p>

            <div class="form-check" v-for="(reason, idx) in cancelReasons" :key="idx">
              <input class="form-check-input" type="checkbox" :id="'cancel-reason-' + idx" :value="reason"
                v-model="selectedReasons">
              <label class="form-check-label" :for="'cancel-reason-' + idx">{{ reason }}</label>
            </div>

            <!-- Ô "Khác" chỉ hiện khi đã tick Khác -->
            <div class="mt-3" v-if="isOtherSelected">
              <label class="form-label">Lý do khác</label>
              <textarea class="form-control" rows="3" v-model.trim="otherReason"
                placeholder="Mô tả chi tiết lý do huỷ..."></textarea>
              <div class="form-text">* Bắt buộc nhập nếu chọn “Khác”.</div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-outline-secondary" data-bs-dismiss="modal"
              :disabled="isCancelSubmitting">Đóng</button>
            <button class="btn btn-danger" @click="submitCancel" :disabled="isCancelSubmitting">
              <span v-if="isCancelSubmitting" class="spinner-border spinner-border-sm me-1"></span>
              Xác nhận huỷ
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- Phân trang -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
        ‹
      </button>

      <button v-for="p in totalPages" :key="p" class="page-btn" :class="{ active: currentPage === p }"
        @click="currentPage = p">
        {{ p }}
      </button>

      <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
        ›
      </button>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="billModal" tabindex="-1" aria-labelledby="billModalLabel" aria-hidden="true"
      ref="modal">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="billModalLabel">Chi tiết Hóa Đơn</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body" v-if="selectedInvoice">
            <!-- Thông tin hóa đơn -->
            <div class="border rounded bg-light p-3">
              <div class="row mb-3 align-items-center">
                <div class="col-md-9">
                  <strong>{{ selectedInvoice.recipientName }}</strong> - {{ selectedInvoice.recipientNumberPhone }}<br>
                  {{ selectedInvoice.receiverAddress }}
                </div>
                <div class="col-md-3 text-end">
                  <button v-if="selectedInvoice.status === 'Chờ xác nhận'" @click="openAddressOverlay"
                    class="btn btn-outline-primary btn-sm">Thay đổi</button>
                </div>
              </div>
            </div>

            <div class="row mb-3">
              <div class="col-md-6">
                <label>Mã hóa đơn</label>
                <input v-model="selectedInvoice.code" class="form-control" :readonly="true" />
              </div>
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="selectedInvoice.NOTE" class="form-control" :readonly="true"></textarea>
            </div>

            <!-- Chi tiết sản phẩm -->
            <h6>Chi tiết sản phẩm</h6>
            <table class="table table-sm">
              <thead>
                <tr>
                  <th>Ảnh</th>
                  <th>Tên SP</th>
                  <th>Màu</th>
                  <th>Kích cỡ</th>
                  <th>SL</th>
                  <th>Giá</th>
                  <th>Tổng</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(detail, index) in invoiceDetails" :key="detail.ID">
                  <td><img :src="detail.productImage" width="50" /></td>
                  <td>{{ detail.productName }}</td>
                  <td>{{ detail.color }}</td>
                  <td>{{ detail.size }}</td>
                  <!-- ✅ Số lượng có nút tăng/giảm -->
                  <td>
                    <input type="number" v-model="detail.quantity" min="1"
                      class="form-control form-control-sm text-center" style="width: 60px;" :readonly="true" />
                  </td>
                  <td>{{ formatCurrency(detail.price) }}</td>
                  <td>{{ formatCurrency(detail.price * detail.quantity) }}</td>
                </tr>
                <tr v-if="invoiceDetails.length === 0">
                  <td colspan="7" class="text-center text-muted">Không có sản phẩm nào</td>
                </tr>
              </tbody>
            </table>

            <!-- Tổng kết -->
            <h6 class="mt-4">Tổng kết</h6>
            <ul class="list-group">
              <li class="list-group-item d-flex justify-content-between">
                <span>Tiền hàng:</span>
                <strong>{{ formatCurrency(subTotal) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between">
                <span>Giảm giá:</span>
                <strong>{{ formatCurrency(discountAmount) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between">
                <span>Phí vận chuyển:</span>
                <strong>{{ formatCurrency(shippingFee) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between bg-light">
                <span><strong>Tổng cộng:</strong></span>
                <strong>{{ formatCurrency(grandTotal) }}</strong>
              </li>
            </ul>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button v-if="statusInvoice === 'Chờ xác nhận'" class="btn btn-success" @click="">Lưu</button>
          </div>

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

                <!-- Khung chọn địa chỉ -->
                <div class="d-flex justify-content-between align-items-center mt-2">
                  <div class="form-check">
                    <input class="form-check-input" type="radio" :id="'address-' + address.id" v-model="selectedAddress"
                      :value="address.id">
                    <label class="form-check-label" :for="'address-' + address.id">Chọn địa chỉ</label>
                  </div>

                  <!-- Bên phải: nút Cập nhật và Lưu -->
                  <div class="d-flex gap-2">
                    <span class="text-primary text-decoration-underline small" role="button"
                      @click="openUpdateAddressOverlay(address)">
                      Cập nhật
                    </span>
                    <span class="text-danger text-decoration-underline small" role="button"
                      @click="deleteAddress(address.id)">
                      Xoá
                    </span>
                  </div>
                </div>
              </div>
            </form>
          </div>

          <!-- Footer: cố định -->
          <div class="p-4 border-top bg-white position-sticky bottom-0 z-2">
            <button type="button" class="btn btn-outline-primary w-100" @click="saveSelectedAddress">
              Lưu thay đổi
            </button>
            <button type="button" class="btn btn-success w-100 mt-2" @click="openAddAddressOverlay">
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
                required />
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




  </div>
</template>

<style scoped>
.orders {
  max-width: 990px;
  margin: 0 auto;
  padding: 16px;
  background: #f5f5f5;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.tab {
  padding: 8px 12px;
  border: 1px solid #eee;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.tab.active {
  color: #d0011b;
  border-color: #ee4d2d33;
}

.search {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 12px;
}

.search input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 14px;
}

.order-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #eee;
}

.order-meta {
  color: #333;
  font-weight: 500;
}

.order-code {
  margin-right: 6px;
}

.order-date {
  color: #888;
  font-weight: 400;
}

.order-status {
  color: #2eb872;
  font-weight: 600;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #eee;
}

.item-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.item-left img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #eee;
}

.item-info .item-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
}

.item-info .item-variant {
  font-size: 12px;
  color: #777;
  margin-bottom: 4px;
}

.item-info .item-qty {
  font-size: 12px;
  color: #555;
}

.item-price {
  text-align: right;
}

.item-price .old {
  text-decoration: line-through;
  color: #999;
  margin-right: 6px;
}

.item-price .new {
  color: #d0011b;
  font-weight: 600;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
}

.hint {
  color: #888;
  font-size: 13px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total {
  margin-right: 8px;
  font-size: 14px;
}

.total-number {
  color: #d0011b;
  font-weight: 700;
}

.btn {
  padding: 8px 14px;
  border-radius: 6px;
  border: 1px solid #ddd;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary {
  background: #d0011b;
  color: #fff;
  border-color: #d0011b;
}

.btn-primary:hover {
  filter: brightness(0.95);
}

.btn-outline:hover {
  background: #f7f7f7;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  gap: 6px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn.active {
  background: #d0011b;
  color: #fff;
  border-color: #d0011b;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
