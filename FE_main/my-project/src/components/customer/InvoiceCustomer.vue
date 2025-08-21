<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from 'axios';
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
import { Modal } from "bootstrap";
import { nextTick } from 'vue';

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
    console.log(invoiceDetails.value)
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

const openModal = async (order) => {
  if (!order || !order.id) {
    console.error("❌ Hóa đơn không có ID");
    return;
  }

  selectedInvoice.value = { ...order };
  statusInvoice = selectedInvoice.value.status
  billId = selectedInvoice.value.id;

  order.items.forEach((item, index) => {
    subTotal = item.subTotal
    shippingFee = item.shippingFee
    discountAmount = item.discountAmount
    grandTotal = subTotal + shippingFee - discountAmount

    quantity = item.quantity
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

onMounted(fetchOrder);
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
          <button class="btn btn-outline" @click="openModal(order)">Xem chi tiết</button>
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
            <div class="row mb-3">
              <div class="col-md-6">
                <label>Mã hóa đơn</label>
                <input v-model="selectedInvoice.code" class="form-control" :readonly="true" />
              </div>
              <div class="col-md-6">
                <label>Người nhận</label>
                <input v-model="selectedInvoice.RECIPIENT_NAME" class="form-control" :readonly="true" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label>SĐT</label>
                <input v-model="selectedInvoice.RECIPIENT_PHONE_NUMBER" class="form-control" :readonly="true" />
              </div>
              <div class="col-md-6">
                <label>Địa chỉ</label>

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
                      class="form-control form-control-sm text-center" style="width: 60px;" :readonly="true"/>
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
            <button v-if="statusInvoice === 'Chờ xác nhận'" class="btn btn-success"
              @click="">Lưu</button>
          </div>
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
