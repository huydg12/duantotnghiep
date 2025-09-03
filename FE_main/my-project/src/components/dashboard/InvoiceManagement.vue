<script setup>
import axios from "axios";
import { ref, computed, onMounted, nextTick, watch } from "vue";
import { Modal } from "bootstrap";
import { printReceipt } from "../pos/printReceipt";

// import { nextTick } from 'vue';
const bills = ref([]);
const selectedBill = ref(null);
const billDetails = ref([]);
const modalInstance = ref(null);
const modal = ref(null);

const currentTab = ref("all");
const searchQuery = ref("");

const billTypeFilter = ref("all");
const uniqueBillTypes = computed(() =>
  Array.from(
    new Set(
      (bills.value || [])
        .map(b => String(b?.BILL_TYPE ?? "").trim())
        .filter(v => v.length > 0)
    )
  )
);

const tabs = [
  { label: "Tất cả", value: "all" },
  { label: "Chờ xác nhận", value: "Chờ xác nhận" },
  { label: "Đã xác nhận", value: "Đã xác nhận" },
  { label: "Đang giao", value: "Đang giao" },
  { label: "Hoàn thành", value: "Hoàn thành" },
  { label: "Đã hủy", value: "Đã hủy" },
  // { label: "Trả hàng/Hoàn tiền", value: "Trả hàng/Hoàn tiền" }
];

const TAB_TO_STATUS = {
  all: null,
  "Chờ xác nhận": [1],
  "Đã xác nhận": [2],
  "Đang giao": [3],
  "Hoàn thành": [4],
  "Đã hủy": [5],
};

const parseToMs = (d) => {
  if (d == null) return 0;
  if (typeof d === "number") return d < 1e12 ? d * 1000 : d; // giây->ms
  const t = Date.parse(d);
  return Number.isNaN(t) ? 0 : t;
};

const pendingCount = computed(() =>
  (bills.value || []).filter(b => Number(b.STATUS) === 1).length
);

// Tab dùng để lọc thực tế: nếu đang chọn "Chờ xác nhận" nhưng không còn đơn (và không tìm kiếm) → dùng "all"
const appliedTab = computed(() => {
  const noSearch = (searchQuery.value || "").trim() === "";
  if (currentTab.value === "Chờ xác nhận" && pendingCount.value === 0 && noSearch) {
    return "all";
  }
  return currentTab.value;
});

// Hàm kiểm tra khớp ô tìm kiếm + loại HĐ (dùng chung cho disable tab)
const matchesSearch = (bill, q, typeFilter) => {
  const items = Array.isArray(bill?.items) ? bill.items : [];
  const searchOk =
    !q ||
    String(bill?.CODE ?? "").toLowerCase().includes(q) ||
    items.some((i) => String(i?.name ?? "").toLowerCase().includes(q));

  const typeLc = (typeFilter || "all").toLowerCase();
  const billTypeLc = String(bill?.BILL_TYPE ?? "").toLowerCase();
  const matchType = typeLc === "all" || billTypeLc === typeLc;

  return searchOk && matchType;
};

const filteredOrders = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  const statuses = TAB_TO_STATUS[appliedTab.value]; // null = all
  const typeLc = (billTypeFilter.value || "all").toLowerCase();

  const arr = (bills.value || []).filter((bill) => {
    const matchTab = !statuses || statuses.includes(Number(bill?.STATUS));

    const items = Array.isArray(bill?.items) ? bill.items : [];
    const matchSearch =
      !q ||
      String(bill?.CODE ?? "").toLowerCase().includes(q) ||
      items.some((i) => String(i?.name ?? "").toLowerCase().includes(q));

    const billTypeLc = String(bill?.BILL_TYPE ?? "").toLowerCase();
    const matchType = typeLc === "all" || billTypeLc === typeLc;

    return matchTab && matchSearch && matchType;
  });

  // Mới -> cũ (theo giây)
  arr.sort((a, b) => {
    const ad = a.CREATED_AT ?? parseToMs(a.CREATED_DATE);
    const bd = b.CREATED_AT ?? parseToMs(b.CREATED_DATE);
    if (bd !== ad) return bd - ad;
    return (b.ID ?? 0) - (a.ID ?? 0);
  });

  return arr;
});

// Tính “tab nào có dữ liệu (sau khi áp dụng search + loại)”
const tabAvailability = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  const map = {};
  for (const tab of tabs) {
    const statuses = TAB_TO_STATUS[tab.value]; // null = all
    map[tab.value] = (bills.value || []).some((bill) => {
      const inTab = !statuses || statuses.includes(Number(bill?.STATUS));
      return inTab && matchesSearch(bill, q, billTypeFilter.value);
    });
  }
  return map;
});

// ✅ Disable tab nếu KHÔNG có bill trong tab đó (theo search hiện tại + loại)
const isTabDisabled = (value) => !tabAvailability.value[value];

const handleTabClick = (value) => {
  if (isTabDisabled(value)) return; // chặn click tab rỗng
  currentTab.value = value;
  currentPage.value = 1;
};

watch(pendingCount, (cnt) => {
  const noSearch = (searchQuery.value || "").trim() === "";
  if (currentTab.value === "Chờ xác nhận" && cnt === 0 && noSearch) {
    currentTab.value = "all";
  }
});

// Phân trang + bảo toàn currentPage hợp lệ
const currentPage = ref(1);
const pageSize = 8;

const totalPages = computed(() => Math.max(1, Math.ceil(filteredOrders.value.length / pageSize)));

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredOrders.value.slice(start, start + pageSize);
});

// Reset trang khi thay đổi filter/search
watch([currentTab, searchQuery, billTypeFilter], () => {
  currentPage.value = 1;
});
watch(filteredOrders, () => {
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

const mapBillData = (data) => {
  return data.map((item) => ({
    ID: item.id,
    CODE: item.code,
    PTTT_ID: item.ptttId,
    EMPLOYEE_ID: item.employeeId,
    BILL_TYPE: item.billType,
    CREATED_DATE: item.createdDate,
    RECIPIENT_NAME: item.recipientName,
    RECIPIENT_PHONE_NUMBER: item.recipientPhoneNumber,
    RECEIVER_ADDRESS: item.receiverAddress,
    STATUS: Number(item.status),
    STATUS_PAYMENT: item.statusPayment,
    GRAND_TOTAL: item.grandTotal,
    SUB_TOTAL: item.subTotal,
    DISCOUNT_AMOUNT: item.discountAmount,
    SHIPPING_FEE: item.shippingFee,
    NOTE: item.note,
  }));
};

const setDefaultTab = () => {
  currentTab.value = pendingCount.value > 0 ? "Chờ xác nhận" : "all";
};

const fetchBills = async () => {
  try {
    const response = await axios.get("http://localhost:8080/bill/show");
    bills.value = mapBillData(response.data);
    console.log(bills.value);
    setDefaultTab();
  } catch (error) {
    console.log("Lỗi lấy hoá đơn", error);
  }
};

const mapBillDetailData = (data) => {
  return data.map((item) => ({
    ID: item.id,
    PRODUCT_DETAIL_ID: item.PRODUCT_DETAIL_ID || item.productDetailId,
    PRODUCT_NAME: item.PRODUCT_NAME || item.productName,
    SIZE: item.SIZE || item.size,
    QUANTITY: item.QUANTITY || item.quantity,
    PRICE: item.PRICE || item.price,
    PRODUCT_IMAGE: item.PRODUCT_IMAGE || item.productImage,
    COLOR: item.COLOR || item.color,
  }));
};

const fetchBillDetails = async (billId) => {
  try {
    const response = await axios.get(
      `http://localhost:8080/billDetail/show/${billId}`
    );
    billDetails.value = mapBillDetailData(response.data);
    console.log("Bill Details:", billDetails.value);
  } catch (error) {
    console.log("Lỗi lấy chi tiết hoá đơn", error);
  }
};

const updateBillStatus = async (bill, newStatus) => {
  try {
    await axios.put(`http://localhost:8080/bill/updateStatus/${bill.ID}`, { status: newStatus });
    // Cập nhật lạc quan trên UI
    bill.STATUS = newStatus;
    // (tuỳ chọn) refresh danh sách:
    await fetchBills();
  } catch (error) {
    console.error("❌ Lỗi khi cập nhật trạng thái:", error);
    alert("Cập nhật trạng thái thất bại");
  }
};

const updateBillStatus4 = async (bill, newStatus) => {
  try {
    await axios.put(`http://localhost:8080/bill/updateBillStatus4/${bill.ID}`, { status: newStatus, statusPayment: "Đã thanh toán" });
    // Cập nhật lạc quan trên UI
    bill.STATUS = newStatus;
    // (tuỳ chọn) refresh danh sách:
    await fetchBills();
  } catch (error) {
    console.error("❌ Lỗi khi cập nhật trạng thái:", error);
    alert("Cập nhật trạng thái thất bại");
  }
};

// Lấy thông tin khách hàng
const userJson = localStorage.getItem("user");
let employeeId = ref(null);
if (userJson) {
  try {
    const user = JSON.parse(userJson);
    employeeId = user?.employeeId;
    console.log("✅ Customer ID:", employeeId);
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
  }
}

const updateInventory = async (bill, newStatus) => {
  try {
    // Lấy các sản phẩm trong hóa đơn
    const response = await axios.get(`http://localhost:8080/billDetail/show/${bill.ID}`);
    const billDetails = response.data;

    // Kiểm tra nếu không có sản phẩm trong hóa đơn
    if (!billDetails || billDetails.length === 0) {
      console.error("❌ Không có sản phẩm trong hóa đơn");
      return;
    }
    await axios.put(`http://localhost:8080/bill/updateStatus/${bill.ID}`, {
      status: bill.STATUS
    });

    // Duyệt qua từng sản phẩm trong hóa đơn để cập nhật lại kho
    for (const detail of billDetails) {
      const productDetailId = detail.productDetailId;
      const quantityToUpdate = detail.quantity;

      // Kiểm tra nếu có productDetailId và số lượng hợp lệ
      if (!productDetailId || !quantityToUpdate) {
        console.error(`❌ Lỗi: Thiếu thông tin sản phẩm (ID: ${productDetailId}) hoặc số lượng không hợp lệ`);
        continue;
      }

      // Gửi yêu cầu API để cập nhật số lượng trong kho (sử dụng phương thức updateQuantityTru)
      await axios.put(
        `http://localhost:8080/inventory/updateQuantityTru/${productDetailId}`, {
        quantity: quantityToUpdate // Trả lại số lượng vào kho
      });

      await axios.put(`http://localhost:8080/bill/updateStatus/${bill.ID}`, { status: newStatus });

      const updateEmployee = {
        employeeId: employeeId,
      };
      await axios.put(`http://localhost:8080/bill/updateEmployee/${bill.ID}`, updateEmployee);

      bill.STATUS = newStatus;
      console.log(`✅ Đã cập nhật kho cho sản phẩm ID: ${productDetailId} với số lượng: ${quantityToUpdate}`);
      window.dispatchEvent(new CustomEvent('bill:changed'));
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật kho hoặc thay đổi trạng thái hóa đơn:", error);
  }
};

const employee = ref(null);
const employeeName = ref('—');

async function fetchEmployeeNameForBill(bill) {
  try {
    // lấy employeeId từ bill tại thời điểm click
    const id =
      bill?.EMPLOYEE_ID ??
      bill?.employeeId ??
      bill?.employeeID ??
      bill?.employee?.id;

    if (!id) {
      employee.value = null;
      employeeName.value = '—';
      return null;
    }

    const { data } = await axios.get(`http://localhost:8080/employee/showById/${id}`);
    const payload = Array.isArray(data) ? data[0] : data;

    employee.value = payload || null;
    employeeName.value =
      payload?.fullName ?? payload?.FULL_NAME ?? payload?.name ?? '—';

    console.log('employeeName:', employeeName.value);
    return employeeName.value;
  } catch (e) {
    console.error("Fetch employee failed:", e);
    employee.value = null;
    employeeName.value = '—';
    return null;
  }
}

const toVND = (n) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" })
    .format(Number(n || 0));

const printInvoice = async (bill) => {
  try {
    // Lấy chi tiết hoá đơn
    const { data } = await axios.get(`http://localhost:8080/billDetail/show/${bill.ID}`);
    const details = mapBillDetailData(data); // đã có sẵn trong file của bạn
    const name = await fetchEmployeeNameForBill(bill);

    // Build payload theo schema của printReceipt
    const payload = {
      billCode: bill.CODE,
      createdAt: bill.CREATED_DATE,
      employeeName: name,
      customerName: bill.RECIPIENT_NAME,
      customerPhone: bill.RECIPIENT_PHONE_NUMBER,
      items: details.map(d => ({
        productName: d.PRODUCT_NAME,
        color: d.COLOR,
        size: d.SIZE,
        quantity: d.QUANTITY,
        // Giá & thành tiền định dạng sẵn để in đẹp
        price: toVND(d.PRICE),
        lineTotal: toVND((Number(d.PRICE) || 0) * (Number(d.QUANTITY) || 0)),
      })),
      // Tổng tiền: ưu tiên dùng từ bill (đã tính sẵn)
      subTotal: toVND(bill.SUB_TOTAL),
      discountAmount: toVND(bill.DISCOUNT_AMOUNT),
      grandTotal: toVND(bill.GRAND_TOTAL),
      // (tuỳ chọn) paid, change nếu bạn muốn hiển thị
    };

    await printReceipt(payload); // ✅ Mở tab mới với PDF
  } catch (e) {
    console.error("In hoá đơn lỗi:", e);
    alert("Không thể in hoá đơn. Vui lòng thử lại.");
  }
};

const isPositiveInt = (v) => Number.isInteger(Number(v)) && Number(v) > 0;

const handleQuantityChange = (detail) => {
  let n = parseInt(detail.QUANTITY);
  if (!Number.isFinite(n) || n < 1) n = 1;
  detail.QUANTITY = n;
};
const lastTriggerEl = ref(null);

const openModal = async (bill, ev) => {
  selectedBill.value = { ...bill };
  lastTriggerEl.value = ev?.currentTarget || null;
  await fetchBillDetails(bill.ID);

  // NEW: chụp snapshot ghi chú & số lượng ban đầu để so sánh
  selectedBill.value.oldNote = (selectedBill.value.NOTE ?? '').trim();
  billDetails.value = billDetails.value.map(d => {
    const q = parseInt(d.QUANTITY) || 1;
    return { ...d, QUANTITY: q, oldQuantity: q }
  });

  await nextTick();
  if (!modalInstance.value) {
    modalInstance.value = new Modal(modal.value);
  }
  modalInstance.value.show();
};

const closeModalSafely = () => {
  // nếu focus còn nằm trong modal -> blur trước khi ẩn
  if (modal.value?.contains(document.activeElement)) {
    (document.activeElement)?.blur?.();
  }
  modalInstance.value?.hide();
  // trả lại focus cho nút đã mở modal (nếu có)
  lastTriggerEl.value?.focus?.();
};

const formatCurrency = (value) => {
  const num = parseFloat(value) || 0;
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const subTotal = computed(() =>
  billDetails.value.reduce(
    (total, item) => total + item.QUANTITY * item.PRICE,
    0
  )
);

const grandTotal = computed(
  () =>
    subTotal.value -
    (parseFloat(selectedBill.value?.DISCOUNT_AMOUNT) || 0) +
    (parseFloat(selectedBill.value?.SHIPPING_FEE) || 0)
);

const isPaid = computed(() => selectedBill.value?.STATUS >= 3);

const cacheOldQuantity = (detail) => {
  if (!detail) return;
  detail.oldQuantity = parseInt(detail.QUANTITY) || 1;
};

const saveAllChanges = async () => {
  try {

    if (!billDetails.value.length) {
      alert('Hóa đơn chưa có sản phẩm.');
      return;
    }
    for (const d of billDetails.value) {
      const q = parseInt(d.QUANTITY);
      if (!isPositiveInt(q)) {
        alert(`Vui lòng nhập số lượng hợp lệ cho "${d.PRODUCT_NAME}".`);
        return;
      }
    }

    // NEW: phát hiện "không có thay đổi" (số lượng & ghi chú)
    const noteChanged =
      (selectedBill.value.NOTE ?? '').trim() !== (selectedBill.value.oldNote ?? '').trim();
    const qtyChanged = billDetails.value.some(d => {
      const now = parseInt(d.QUANTITY) || 1;
      const before = Number.isFinite(d.oldQuantity) ? d.oldQuantity : now;
      return now !== before;
    });
    if (!noteChanged && !qtyChanged) {
      alert('Không có thay đổi nào để lưu.');
      return;
    }

    const updates = [];
    let updated = false;
    let quantityExceedsStock = false;

    for (const d of billDetails.value) {
      const oldQ = Number.isFinite(d.oldQuantity) ? d.oldQuantity : (parseInt(d.QUANTITY) || 1);
      const newQ = parseInt(d.QUANTITY) || 1;

      if (newQ !== oldQ) {
        updated = true;

        // kiểm tra tồn kho
        const invRes = await axios.get(`http://localhost:8080/inventory/getQuantity/${d.PRODUCT_DETAIL_ID}`);
        const inv = parseInt(invRes?.data?.quantityInventory) || 0;
        const max = inv + oldQ;

        if (newQ > max) {
          quantityExceedsStock = true;
          alert(`Số lượng sản phẩm "${d.PRODUCT_NAME}" vượt quá số lượng tồn kho. Tồn kho hiện tại: ${inv}`);
          break;
        }

        const safeQ = Math.min(Math.max(newQ, 1), max);
        if (safeQ !== newQ) d.QUANTITY = safeQ;

        updates.push(
          axios.put(`http://localhost:8080/billDetail/updateQuantity/${d.ID}`, { quantity: d.QUANTITY }),
          axios.put(`http://localhost:8080/inventory/updateQuantityByBill/${d.PRODUCT_DETAIL_ID}`, {
            quantity: d.QUANTITY,
            oldQuantity: oldQ
          })
        );
        d.oldQuantity = d.QUANTITY; // cập nhật snapshot sau khi hợp lệ
      }
    }

    // NEW: nếu có lỗi vượt kho → dừng ngay, KHÔNG áp dụng cập nhật dở dang
    if (quantityExceedsStock) return;

    if (updates.length) {
      await Promise.all(updates);
    }

    const patch = {};

    // chỉ patch NOTE nếu đổi
    if ((selectedBill.value.NOTE ?? '').trim() !== (selectedBill.value.oldNote ?? '').trim()) {
      patch.note = (selectedBill.value.NOTE ?? '').trim();
      updated = true;
    }

    // nếu có thay đổi (SL/NOTE) → cập nhật tổng
    if (updated) {
      patch.subTotal = Number(subTotal.value) || 0;
      patch.grandTotal = Number(grandTotal.value) || 0;
    }

    if (Object.keys(patch).length > 0) {
      await axios.put(`http://localhost:8080/bill/updateBill/${selectedBill.value.ID}`, patch);
    }

    // đồng bộ bảng ngoài
    const idx = bills.value.findIndex(b => b.ID === selectedBill.value.ID);
    if (idx !== -1) {
      bills.value[idx] = { ...bills.value[idx], ...patch };
    }

    await fetchBills();
    closeModalSafely();
  } catch (err) {
    console.error("❌ Lỗi khi lưu thay đổi:", err);
    alert("Lưu thất bại, vui lòng thử lại.");
  }
};
function blockMinus(e) { if (e.key === '-' || e.key === 'e') { e.preventDefault() } }
// Hàm chuyển đổi trạng thái từ số sang chữ
const getStatusText = (status) => {
  const statusMap = {
    1: "Chờ xác nhận",
    2: "Đã xác nhận",
    3: "Đang giao",
    4: "Hoàn thành",
    5: "Đã hủy",
  };
  return statusMap[status] || "Không xác định"; // Trả về nếu không có trạng thái phù hợp
};

const PTTT_LABELS = {
  1: 'Thanh toán khi nhận hàng',
  2: 'Thanh toán MoMo',
};

onMounted(() => {
  fetchBills();
});
</script>

<template>
  <div class="container mt-5">
    <h2 class="mb-4">Quản lý Hóa Đơn</h2>
    <!-- Tabs -->
    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.value" class="tab"
        :class="{ active: currentTab === tab.value, disabled: isTabDisabled(tab.value) }"
        :disabled="isTabDisabled(tab.value)" :title="isTabDisabled(tab.value) ? 'Không có đơn trong mục này' : ''"
        @click="handleTabClick(tab.value)">
        {{ tab.label }}
      </button>
    </div>

    <!-- Tìm kiếm + Loại HĐ -->
    <div class="row g-2 align-items-center mb-3">
      <div class="col-md-8">
        <input v-model="searchQuery" type="text" class="form-control"
          placeholder="Tìm kiếm theo mã hóa đơn hoặc tên sản phẩm..." />
      </div>
      <div class="col-md-4">
        <select v-model="billTypeFilter" class="form-select">
          <option value="all">Tất cả loại HĐ</option>
          <option v-for="t in uniqueBillTypes" :key="t" :value="t">{{ t }}</option>
        </select>
      </div>
    </div>

    <table class="table table-bordered table-hover">
      <thead class="thead-dark">
        <tr>
          <th>Mã HĐ</th>
          <th>Ngày tạo</th>
          <th>Loại HĐ</th>
          <th>Người nhận</th>
          <th>Phương thức thanh toán</th>
          <th>Trạng thái</th>
          <th>Thanh toán</th>
          <th>Tổng tiền</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="bill in paginatedOrders" :key="bill.ID">
          <td>{{ bill.CODE }}</td>
          <td>{{ bill.CREATED_DATE }}</td>
          <td>{{ bill.BILL_TYPE }}</td>
          <td>{{ bill.RECIPIENT_NAME }}</td>
          <td>{{ PTTT_LABELS[bill.PTTT_ID] }}</td>
          <td>{{ getStatusText(bill.STATUS) }}</td>

          <td>{{ bill.STATUS_PAYMENT }}</td>
          <td>{{ formatCurrency(bill.GRAND_TOTAL || 0) }}</td>
          <td>
            <button class="btn btn-sm btn-primary me-2" @click="openModal(bill, $event)">Chi tiết</button>

            <!-- Ở đây chỉ là các nút, KHÔNG có thêm <td> -->
            <button v-if="+bill.STATUS === 1" class="btn btn-sm btn-secondary me-1" @click="updateInventory(bill, 2)">
              Xác nhận
            </button>
            <button v-if="+bill.STATUS === 2" class="btn btn-sm btn-warning me-1" @click="updateBillStatus(bill, 3)">
              Đang giao
            </button>
            <button v-if="+bill.STATUS === 3" class="btn btn-sm btn-success me-1" @click="updateBillStatus4(bill, 4)">
              Hoàn thành
            </button>
            <button v-if="+bill.STATUS === 1" class="btn btn-sm btn-danger me-1" @click="updateBillStatus(bill, 5)">
              Huỷ
            </button>

            <button v-if="+bill.STATUS !== 5" class="btn btn-sm btn-outline-dark me-1" @click="printInvoice(bill)">
              In hoá đơn
            </button>
          </td>
        </tr>
      </tbody>
    </table>

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

          <div class="modal-body" v-if="selectedBill">
            <!-- Thông tin hóa đơn -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label>Mã hóa đơn</label>
                <input v-model="selectedBill.CODE" class="form-control" :readonly="isPaid || true" />
              </div>
              <div class="col-md-6">
                <label>Người nhận</label>
                <input v-model="selectedBill.RECIPIENT_NAME" class="form-control" :readonly="isPaid || true" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label>SĐT</label>
                <input v-model="selectedBill.RECIPIENT_PHONE_NUMBER" class="form-control" :readonly="isPaid || true" />
              </div>
              <div class="col-md-6">
                <label>Địa chỉ</label>
                <input v-model="selectedBill.RECEIVER_ADDRESS" class="form-control" :readonly="isPaid || true" />
              </div>
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="selectedBill.NOTE" class="form-control" :readonly="isPaid"></textarea>
            </div>

            <!-- Chi tiết sản phẩm -->
            <h6>Chi tiết sản phẩm</h6>
            <table class="table table-sm">
              <thead>
                <tr>  
                  <th>Tên SP</th>
                  <th>Màu</th>
                  <th>Kích cỡ</th>
                  <th>SL</th>
                  <th>Giá</th>
                  <th>Tổng</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(detail, index) in billDetails" :key="detail.ID">
                  <td>{{ detail.PRODUCT_NAME }}</td>
                  <td>{{ detail.COLOR }}</td>
                  <td>{{ detail.SIZE }}</td>
                  <!-- ✅ Số lượng có nút tăng/giảm -->
                  <td>
                    <input type="number" v-model="detail.QUANTITY" min="1"
                      class="form-control form-control-sm text-center" style="width: 60px;" @keydown="blockMinus"
                      @focus="cacheOldQuantity(detail)" @input="handleQuantityChange(detail)" :readonly="isPaid" />
                  </td>
                  <td>{{ formatCurrency(detail.PRICE) }}</td>
                  <td>{{ formatCurrency(detail.PRICE * detail.QUANTITY) }}</td>
                </tr>
                <tr v-if="billDetails.length === 0">
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
                <strong>{{ formatCurrency(selectedBill.DISCOUNT_AMOUNT || 0) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between">
                <span>Phí vận chuyển:</span>
                <strong>{{ formatCurrency(selectedBill.SHIPPING_FEE || 0) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between bg-light">
                <span><strong>Tổng cộng:</strong></span>
                <strong>{{ formatCurrency(grandTotal) }}</strong>
              </li>
            </ul>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button class="btn btn-success" @click="saveAllChanges" :disabled="isPaid">Lưu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tab:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: auto;
  /* vẫn cho hover để thấy title */
}

img {
  border-radius: 4px;
}

option:disabled {
  background-color: #f8f9fa;
  color: #999;
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
