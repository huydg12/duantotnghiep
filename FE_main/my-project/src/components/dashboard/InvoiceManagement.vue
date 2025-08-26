<script setup>
import axios from "axios";
import { ref, computed, onMounted, nextTick,watch } from "vue";
import { Modal } from "bootstrap";
// import { nextTick } from 'vue';
const bills = ref([]);
const selectedBill = ref(null);
const billDetails = ref([]);
const modalInstance = ref(null);
const modal = ref(null);

const currentTab = ref("all");
const searchQuery = ref("");
const tabs = [
    { label: "Chờ xác nhận", value: "Chờ xác nhận" },
  { label: "Tất cả", value: "all" },
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
const filteredOrders = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  const statuses = TAB_TO_STATUS[appliedTab.value]; // null = all

  const arr = (bills.value || []).filter((bill) => {
    const matchTab = !statuses || statuses.includes(Number(bill?.STATUS));
    const items = Array.isArray(bill?.items) ? bill.items : [];
    const matchSearch =
      !q ||
      String(bill?.CODE ?? "").toLowerCase().includes(q) ||
      items.some((i) => String(i?.name ?? "").toLowerCase().includes(q));
    return matchTab && matchSearch;
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
// Hàm kiểm tra khớp ô tìm kiếm (giữ nguyên cách thầy đang filter)
const matchesSearch = (bill, q) => {
  const items = Array.isArray(bill?.items) ? bill.items : [];
  return (
    !q ||
    String(bill?.CODE ?? "").toLowerCase().includes(q) ||
    items.some((i) => String(i?.name ?? "").toLowerCase().includes(q))
  );
};

// Tính “tab nào có dữ liệu (sau khi áp dụng search)”
const tabAvailability = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  const map = {};
  for (const tab of tabs) {
    const statuses = TAB_TO_STATUS[tab.value]; // null = all
    map[tab.value] = (bills.value || []).some((bill) => {
      const inTab = !statuses || statuses.includes(Number(bill?.STATUS));
      return inTab && matchesSearch(bill, q);
    });
  }
  return map;
});

// ✅ Disable tab nếu KHÔNG có bill trong tab đó (theo search hiện tại)
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
const mapBillData = (data) => {
  return data.map((item) => ({
    ID: item.id,
    CODE: item.code,
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
    setDefaultTab(); // ✅
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
    // await fetchBills();
  } catch (error) {
    console.error("❌ Lỗi khi cập nhật trạng thái:", error);
    alert("Cập nhật trạng thái thất bại");
  }
};
const updateInventory = async (bill,newStatus) => {
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

      // Gửi yêu cầu API để cập nhật số lượng trong kho (sử dụng phương thức UDQuantity)
      await axios.put(
        `http://localhost:8080/inventory/updateQuantity/${productDetailId}`, {
          quantity: quantityToUpdate // Trả lại số lượng vào kho
        });
          await axios.put(`http://localhost:8080/bill/updateStatus/${bill.ID}`, { status: newStatus });
          // Cập nhật lạc quan trên UI
          bill.STATUS = newStatus;  
      console.log(`✅ Đã cập nhật kho cho sản phẩm ID: ${productDetailId} với số lượng: ${quantityToUpdate}`);
      window.dispatchEvent(new CustomEvent('bill:changed'));
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật kho hoặc thay đổi trạng thái hóa đơn:", error);
  }
};


const lastTriggerEl = ref(null);

const openModal = async (bill, ev) => {
  selectedBill.value = { ...bill };
  lastTriggerEl.value = ev?.currentTarget || null;   // lưu nút đã mở modal
  await fetchBillDetails(bill.ID);
  await nextTick();
  if (!modalInstance.value) {
    modalInstance.value = new Modal(modal.value); // { focus: true } là mặc định
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
    const updates = [];
    let updated = false;
        let quantityExceedsStock = false; // Flag để kiểm tra nếu có vượt kho
    for (const d of billDetails.value) {
      const oldQ = Number.isFinite(d.oldQuantity) ? d.oldQuantity : parseInt(d.QUANTITY) || 1;
      const newQ = parseInt(d.QUANTITY) || 1;
      if (newQ !== oldQ) {
        updated = true; // Đánh dấu là có thay đổi
        const invRes = await axios.get(`http://localhost:8080/inventory/getQuantity/${d.PRODUCT_DETAIL_ID}`);
        const inv = parseInt(invRes?.data?.quantityInventory) || 0;
        const max = inv + oldQ;
              if (newQ > max) {
        quantityExceedsStock = true;
        alert(`Số lượng sản phẩm "${d.PRODUCT_NAME}" vượt quá số lượng tồn kho. Tồn kho hiện tại: ${inv}`);
        break; // Dừng lại nếu có lỗi
      }

        const safeQ = Math.min(Math.max(newQ, 1), max);
        if (safeQ !== newQ) d.QUANTITY = safeQ;

        updates.push(
          axios.put(`http://localhost:8080/billDetail/updateQuantity/${d.ID}`, { quantity: d.QUANTITY }),
          axios.put(`http://localhost:8080/inventory/updateQuantityByBill/${d.PRODUCT_DETAIL_ID}`, { quantity: d.QUANTITY, oldQuantity: oldQ })
        );
        d.oldQuantity = d.QUANTITY;
      }
    }

    if (updates.length) {
      await Promise.all(updates);
    }

    const patch = {};

    // Chỉ cập nhật giá trị của NOTE nếu có thay đổi
    if ((selectedBill.value.NOTE ?? "").trim() !== (selectedBill.value.oldNote ?? "").trim()) {
      patch.note = (selectedBill.value.NOTE ?? "").trim();
      updated = true;
    }

    // Cập nhật tổng tiền nếu có sự thay đổi trong số lượng hoặc thông tin liên quan
    if (updated) {
      patch.subTotal = Number(subTotal.value) || 0;
      patch.grandTotal = Number(grandTotal.value) || 0;
    }

    if (Object.keys(patch).length > 0) {
      await axios.put(`http://localhost:8080/bill/updateBill/${selectedBill.value.ID}`, patch);
    }

    // Đồng bộ lại dữ liệu trong bảng ngoài
    const idx = bills.value.findIndex(b => b.ID === selectedBill.value.ID);
    if (idx !== -1) {
      bills.value[idx] = { ...bills.value[idx], ...patch };
    }

    console.log("✅ Lưu thành công");
    await fetchBills();  // Làm mới dữ liệu hóa đơn từ server
    closeModalSafely();  // Đóng modal và xử lý focus an toàn
  } catch (err) {
    console.error("❌ Lỗi khi lưu thay đổi:", err);
    alert("Lưu thất bại, vui lòng thử lại.");
  }
};
function blockMinus(e) { if (e.key === '-' || e.key === 'e') { e.preventDefault() }}
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
onMounted(() => {
  fetchBills();
});
</script>

<template>
  <div class="container mt-5">
    <h2 class="mb-4">Quản lý Hóa Đơn</h2>
        <!-- Tabs -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: currentTab === tab.value, disabled: isTabDisabled(tab.value) }"
        :disabled="isTabDisabled(tab.value)"
        :title="isTabDisabled(tab.value) ? 'Không có đơn trong mục này' : ''"
        @click="handleTabClick(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>
        <!-- Ô tìm kiếm -->
    <div class="mb-3">
      <input 
        v-model="searchQuery" 
        type="text" 
        class="form-control" 
        placeholder="Tìm kiếm theo mã hóa đơn..."
      />
    </div>

    <table class="table table-bordered table-hover">
      <thead class="thead-dark">
        <tr>
          <th>Mã HĐ</th>
          <th>Ngày tạo</th>
          <th>Người nhận</th>
          <th>Trạng thái</th>
          <th>Thanh toán</th>
          <th>Tổng tiền</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="bill in paginatedOrders"
          :key="bill.ID">
          <td>{{ bill.CODE }}</td>
          <td>{{ bill.CREATED_DATE }}</td>
          <td>{{ bill.RECIPIENT_NAME }}</td>
          <td>{{ getStatusText(bill.STATUS) }}</td>

          <td>{{ bill.STATUS_PAYMENT }}</td>
          <td>{{ formatCurrency(bill.GRAND_TOTAL || 0) }}</td>
          <td>
            <button class="btn btn-sm btn-primary me-2" @click="openModal(bill, $event)">Chi tiết</button>

            <!-- Ở đây chỉ là các nút, KHÔNG có thêm <td> -->
            <button v-if="+bill.STATUS === 1" class="btn btn-sm btn-secondary me-1" @click="updateBillStatus(bill, 2)">
              xác nhận
            </button>
            <button v-if="+bill.STATUS === 2" class="btn btn-sm btn-warning me-1" @click="updateBillStatus(bill, 3)">
              Đang giao
            </button>
            <button v-if="+bill.STATUS === 3" class="btn btn-sm btn-success me-1" @click="updateBillStatus(bill, 4)">
              Hoàn thành
            </button>
            <button v-if="+bill.STATUS === 1" class="btn btn-sm btn-danger me-1" @click="updateInventory(bill, 5)">
              Huỷ
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
                <tr v-for="(detail, index) in billDetails" :key="detail.ID">
                  <td><img :src="detail.PRODUCT_IMAGE" width="50" /></td>
                  <td>{{ detail.PRODUCT_NAME }}</td>
                  <td>{{ detail.COLOR }}</td>
                  <td>{{ detail.SIZE }}</td>
                  <!-- ✅ Số lượng có nút tăng/giảm -->
                  <td>
                    <input type="number" v-model="detail.QUANTITY" min="1"
                      class="form-control form-control-sm text-center" style="width: 60px;" @keydown="blockMinus" @focus="cacheOldQuantity(detail)" @input="handleQuantityChange(detail)"
                      :readonly="isPaid" />
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
  pointer-events: auto; /* vẫn cho hover để thấy title */
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
