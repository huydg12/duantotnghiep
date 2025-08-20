\<template>
  <div class="order-detail container mt-4">
    <!-- Thông tin đơn hàng -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <h4 class="card-title mb-3">Chi tiết đơn hàng</h4>
        <p><strong>Mã đơn:</strong> {{ order.id }}</p>
        <p>
          <strong>Trạng thái:</strong>
          <span
            class="badge"
            :class="order.status === 'Chờ xác nhận' ? 'bg-warning text-dark' : 'bg-success'"
          >
            {{ order.status }}
          </span>
        </p>
        <p><strong>Ngày đặt:</strong> {{ order.date }}</p>
      </div>
    </div>

    <!-- Thông tin địa chỉ -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <h5 class="mb-3">Địa chỉ nhận hàng</h5>
        <div v-if="isEditingAddress">
          <input v-model="editAddress" class="form-control mb-2" />
          <button class="btn btn-success btn-sm me-2" @click="saveAddress">Lưu</button>
          <button class="btn btn-secondary btn-sm" @click="cancelEditAddress">Hủy</button>
        </div>
        <div v-else>
          <p>{{ order.address }}</p>
          <button
            v-if="order.status === 'Chờ xác nhận'"
            class="btn btn-outline-primary btn-sm"
            @click="editAddressMode"
          >
            Cập nhật địa chỉ
          </button>
        </div>
      </div>
    </div>

    <!-- Danh sách sản phẩm -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <h5 class="mb-3">Sản phẩm</h5>
        <table class="table table-hover align-middle">
          <thead>
            <tr>
              <th>#</th>
              <th>Sản phẩm</th>
              <th>Đơn giá</th>
              <th>Số lượng</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in order.items" :key="item.id">
              <td>{{ index + 1 }}</td>
              <td>{{ item.name }}</td>
              <td>{{ formatPrice(item.price) }}</td>
              <td>
                <div v-if="order.status === 'Chờ xác nhận'" class="d-flex align-items-center">
                  <input
                    type="number"
                    v-model.number="item.quantity"
                    class="form-control form-control-sm w-50"
                    min="1"
                  />
                </div>
                <span v-else>{{ item.quantity }}</span>
              </td>
              <td>{{ formatPrice(item.price * item.quantity) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tổng tiền -->
    <div class="card shadow-sm">
      <div class="card-body d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Tổng cộng:</h5>
        <h4 class="text-danger mb-0">{{ formatPrice(totalAmount) }}</h4>
      </div>
    </div>

    <!-- Nút cập nhật -->
    <div v-if="order.status === 'Chờ xác nhận'" class="text-end mt-3">
      <button class="btn btn-success" @click="updateOrder">Cập nhật đơn hàng</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

// Dữ liệu demo
const order = ref({
  id: "DH123456",
  status: "Chờ xác nhận", // Có thể đổi sang "Đã xác nhận" để test
  date: "19/08/2025",
  address: "123 Trần Hưng Đạo, Hải Phòng",
  items: [
    { id: 1, name: "Giày Sneaker Trắng", price: 650000, quantity: 2 },
    { id: 2, name: "Giày Thể Thao Đen", price: 720000, quantity: 1 },
  ],
});

// Địa chỉ
const isEditingAddress = ref(false);
const editAddress = ref(order.value.address);

const editAddressMode = () => {
  isEditingAddress.value = true;
};
const saveAddress = () => {
  order.value.address = editAddress.value;
  isEditingAddress.value = false;
};
const cancelEditAddress = () => {
  editAddress.value = order.value.address;
  isEditingAddress.value = false;
};

// Tổng tiền
const totalAmount = computed(() =>
  order.value.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

const formatPrice = (value) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    value
  );

const updateOrder = () => {
  alert("Đơn hàng đã được cập nhật (demo)!");
};
</script>

<style scoped>
.order-detail {
  max-width: 800px;
}

.card {
  border-radius: 12px;
  border: none;
}

.card-title {
  font-size: 1.3rem;
  font-weight: 600;
}

.table th {
  background-color: #f8f9fa;
}

.btn {
  border-radius: 8px;
}
</style>
