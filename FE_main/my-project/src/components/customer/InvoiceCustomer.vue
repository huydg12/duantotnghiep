<script setup>
import { ref, computed } from "vue";
import axios from 'axios';

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

fetchOrder();
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
          <button class="btn btn-primary">Mua lại</button>
          <button class="btn btn-outline">Xem chi tiết</button>
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
  background: #fff;
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
