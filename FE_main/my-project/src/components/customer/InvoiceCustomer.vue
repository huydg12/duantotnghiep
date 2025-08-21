<script setup>
import { ref, computed } from "vue";
import axios from 'axios';
<<<<<<< HEAD
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";
import { Modal } from "bootstrap";
import { nextTick } from 'vue';

const store = useCartFavoriteStore()
=======
>>>>>>> parent of 8c64eb6 (updateBE)

const tabs = [
  { label: "Tất cả", value: "all" },
  { label: "Chờ xác nhận", value: "Chờ xác nhận" },
  { label: "Đã xác nhận", value: "Đã xác nhận" },
  { label: "Đang giao", value: "Đang giao" },
  { label: "Hoàn thành", value: "Giao hàng thành công" },
  { label: "Đã hủy", value: "Đã hủy" },
  // { label: "Trả hàng/Hoàn tiền", value: "Trả hàng/Hoàn tiền" }
];

const currentTab = ref("all");
const searchQuery = ref("");

const orders = ref([])


// Lấy customerID từ localStorage
const getCustomerID = () => {
  const userJson = localStorage.getItem("user");
  if (!userJson) return null;

  try {
    const user = JSON.parse(userJson);

    return user.customerId; // trả về customerID
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error);
    return null;
  }
};

<<<<<<< HEAD
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
=======
>>>>>>> parent of 8c64eb6 (updateBE)
const fetchOrder = async () => {
  const customerID = getCustomerID();
  if (!customerID) {
    console.error("Không tìm thấy customerID hoặc role không phù hợp");
    return;
  }

  try {
    const response = await axios.get(`http://localhost:8080/bill/invoicecustomer/${customerID}`);
    if (response?.data) {
      orders.value = response.data
      console.log("✅ Dữ liệu hóa đơn của khách hàng:", response.data);
    }
  } catch (error) {
    console.error("❌ Lỗi khi lấy dữ liệu hóa đơn:", error);
  }
};

// Lọc đơn theo tab + search
const filteredOrders = computed(() => {
  return orders.value.filter((order) => {
    const matchTab =
      currentTab.value === "all" || order.status === currentTab.value;
    const matchSearch =
      order.code.includes(searchQuery.value) ||
      order.items.some((i) =>
        i.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    return matchTab && matchSearch;
  });
});

// Phân trang
const currentPage = ref(1);
const pageSize = 2;

const totalPages = computed(() =>
  Math.ceil(filteredOrders.value.length / pageSize)
);

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredOrders.value.slice(start, start + pageSize);
});

const formatCurrency = (v) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    v || 0);

<<<<<<< HEAD
const formatDateTime = (v) => {
  if (!v) return "";
  try {
    const d = new Date(v);
    return d.toLocaleDateString("vi-VN"); // Chỉ lấy ngày
  } catch {
    return v;
  }
};

// Hàm Mua lại sản phẩm
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

// Modal
const selectedInvoice = ref(null);
const invoiceDetails = ref([]);
const modalInstance = ref(null);
const modal = ref(null);
const editingDetail = ref(null);
const editQuantity = ref(1);


const mapInvoiceDetailData = (data) => {
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

const fetchInvoiceDetails = async (order) => {
  try {
    // Gọi API để lấy chi tiết hóa đơn
    const response = await axios.get(
      `http://localhost:8080/billDetail/show/${order.id}` // Sử dụng order.id
    );

    // Map dữ liệu thành định dạng bạn cần (nếu cần)
    invoiceDetails.value = mapInvoiceDetailData(response.data);

  } catch (error) {
    console.error("Lỗi khi lấy chi tiết hóa đơn:", error);
  }
};

let statusInvoice = ref(null)
const openModal = async (order) => {
  if (!order || !order.id) {
    console.error("❌ Hóa đơn không có ID");
    return;
  }

  selectedInvoice.value = { ...order };
  statusInvoice = selectedInvoice.value.status
  await fetchInvoiceDetails(order);

  // Đảm bảo modal được mở sau khi lấy dữ liệu
  await nextTick();

  if (!modalInstance.value) {
    modalInstance.value = new Modal(modal.value); // Khởi tạo modal mỗi lần
  }

  modal.value.classList.remove("fade");

  modalInstance.value.show(); // Hiển thị modal
};


const subTotal = computed(() =>
  invoiceDetails.value.reduce(
    (total, item) => total + item.QUANTITY * item.PRICE,
    0
  )
);

const grandTotal = computed(
  () =>
    subTotal.value -
    (parseFloat(selectedInvoice.value?.DISCOUNT_AMOUNT) || 0) +
    (parseFloat(selectedInvoice.value?.SHIPPING_FEE) || 0)
);

const isPaid = computed(() => selectedInvoice.value?.STATUS >= 3);

function blockMinus(e) {
  if (e.key === '-' || e.key === 'e') {
    e.preventDefault()
  }
}
const cacheOldQuantity = (detail) => {
  detail.oldQuantity = detail.QUANTITY;
};

const handleQuantityChange = async (detail) => {
  try {
    // ✅ Lấy tồn kho hiện tại
    const inventoryRes = await axios.get(`http://localhost:8080/inventory/getQuantity/${detail.PRODUCT_DETAIL_ID}`);
    const quantityInventory = inventoryRes.data.quantityInventory;
    detail.quantityInventory = quantityInventory;

    // ✅ Lấy oldQuantity đúng thời điểm, trước khi thay đổi
    const oldQuantity = detail.oldQuantity !== undefined ? detail.oldQuantity : parseInt(detail.QUANTITY) || 1;

    // ✅ Parse lại QUANTITY người dùng nhập
    detail.QUANTITY = parseInt(detail.QUANTITY);
    if (!detail.QUANTITY || detail.QUANTITY < 1) {
      detail.QUANTITY = 1;
    } else if (detail.QUANTITY > quantityInventory + oldQuantity) {
      detail.QUANTITY = quantityInventory + oldQuantity;
    }

    // ✅ Kiểm tra ID
    if (!detail.ID || !detail.PRODUCT_DETAIL_ID) {
      console.error("❌ Lỗi: ID hoặc PRODUCT_DETAIL_ID bị thiếu:", detail);
      return;
    }

    // ✅ Cập nhật BILL_DETAIL
    await axios.put(`http://localhost:8080/billDetail/updateQuantity/${detail.ID}`,
      { quantity: detail.QUANTITY },
      { headers: { "Content-Type": "application/json" } }
    );

    // ✅ Gửi chênh lệch để cập nhật kho
    await axios.put(`http://localhost:8080/inventory/updateQuantityByBill/${detail.PRODUCT_DETAIL_ID}`,
      {
        quantity: detail.QUANTITY,
        oldQuantity: oldQuantity
      },
      { headers: { "Content-Type": "application/json" } }
    );

    // ✅ Lưu lại oldQuantity mới nhất
    detail.oldQuantity = detail.QUANTITY;
    console.log("Old:", oldQuantity, "New:", detail.QUANTITY, "Chênh lệch:", detail.QUANTITY - oldQuantity);
  } catch (error) {
    console.error("❌ Lỗi khi cập nhật số lượng:", error);
  }
};

onMounted(fetchOrder);
=======
fetchOrder();
>>>>>>> parent of 8c64eb6 (updateBE)
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

    <!-- Danh sách đơn hàng -->
    <div v-for="(order, idx) in paginatedOrders" :key="idx" class="order-card">
      <!-- Header -->
      <div class="order-header">
        <div class="order-meta">
          <span class="order-code">Mã đơn: #{{ order.code }}</span>
          <span class="order-date">• {{ order.date }}</span>
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
          <span class="new">{{ formatCurrency(item.price) }}</span>
        </div>
      </div>

      <!-- Footer -->
      <div class="order-footer">
        <div class="hint"></div>
        <div class="actions">
          <div class="total">
            Thành tiền: <span class="total-number">{{ formatCurrency(order.total) }}</span>
          </div>
<<<<<<< HEAD
          <button v-if="order.status === 'Hoàn Thành'" type="button" class="btn btn-primary" @click="addToCart(order)">
            Mua lại
          </button>
          <button class="btn btn-outline" @click="openModal(order)">Xem chi tiết</button>
=======
          <button class="btn btn-primary">Mua lại</button>
          <button class="btn btn-outline">Xem chi tiết</button>
>>>>>>> parent of 8c64eb6 (updateBE)
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
                <input v-model="selectedInvoice.CODE" class="form-control" :readonly="isPaid" />
              </div>
              <div class="col-md-6">
                <label>Người nhận</label>
                <input v-model="selectedInvoice.RECIPIENT_NAME" class="form-control" :readonly="isPaid" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label>SĐT</label>
                <input v-model="selectedInvoice.RECIPIENT_PHONE_NUMBER" class="form-control" :readonly="isPaid" />
              </div>
              <div class="col-md-6">
                <label>Địa chỉ</label>
                <input v-model="selectedInvoice.RECEIVER_ADDRESS" class="form-control" :readonly="isPaid" />
              </div>
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="selectedInvoice.NOTE" class="form-control" :readonly="isPaid"></textarea>
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
                  <td><img :src="detail.PRODUCT_IMAGE" width="50" /></td>
                  <td>{{ detail.PRODUCT_NAME }}</td>
                  <td>{{ detail.COLOR }}</td>
                  <td>{{ detail.SIZE }}</td>
                  <!-- ✅ Số lượng có nút tăng/giảm -->
                  <td>
                    <input type="number" v-model="detail.QUANTITY" min="1"
                      class="form-control form-control-sm text-center" style="width: 60px;"
                      @focus="cacheOldQuantity(detail)" @input="handleQuantityChange(detail)" @keydown="blockMinus"
                      :readonly="isPaid" />
                  </td>
                  <td>{{ formatCurrency(detail.PRICE) }}</td>
                  <td>{{ formatCurrency(detail.PRICE * detail.QUANTITY) }}</td>
                </tr>
                <tr v-if="invoiceDetails.length === 0">
                  <td colspan="7" class="text-center text-muted">Không có sản phẩm nào</td>
                </tr>
              </tbody>
            </table>

            <!-- Modal sửa chi tiết sản phẩm -->
            <div class="modal fade" id="editDetailModal" tabindex="-1" aria-hidden="true" ref="editDetailModal">
              <div class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <h5 class="modal-title">Chỉnh sửa Sản phẩm</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                  </div>
                  <div class="modal-body" v-if="editingDetail">
                    <p><strong>{{ editingDetail.PRODUCT_NAME }}</strong></p>
                    <div class="mb-3">
                      <label class="form-label">Số lượng</label>
                      <input v-model.number="editQuantity" type="number" class="form-control" min="1" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tổng kết -->
            <h6 class="mt-4">Tổng kết</h6>
            <ul class="list-group">
              <li class="list-group-item d-flex justify-content-between">
                <span>Tiền hàng:</span>
                <strong>{{ formatCurrency(subTotal) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between">
                <span>Giảm giá:</span>
                <strong>{{ formatCurrency(selectedInvoice.DISCOUNT_AMOUNT || 0) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between">
                <span>Phí vận chuyển:</span>
                <strong>{{ formatCurrency(selectedInvoice.SHIPPING_FEE || 0) }}</strong>
              </li>
              <li class="list-group-item d-flex justify-content-between bg-light">
                <span><strong>Tổng cộng:</strong></span>
                <strong>{{ formatCurrency(grandTotal) }}</strong>
              </li>
            </ul>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button v-if="statusInvoice === 'Chờ xác nhận'" class="btn btn-success" @click=""
              :disabled="isPaid">Lưu</button>
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
