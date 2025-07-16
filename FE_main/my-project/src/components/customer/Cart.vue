<script setup>
import { ref, computed, onMounted  } from "vue";
import Banner from "../common/Banner.vue";
import axios from "axios";
import { useRouter } from "vue-router";

const router = useRouter(); 
const cartItems = ref([]);
const selectedItems = ref([]);

// Lấy customerId từ localStorage (dữ liệu user lưu khi đăng nhập)
let customerId = null;
const userJson = localStorage.getItem("user");

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

const fetchCartDetail = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/cartDetail/showCartDetail/${customerId}`);
        // Sắp xếp theo modified_date giảm dần (mới nhất lên đầu)
        cartItems.value = response.data.sort((a, b) => new Date(b.modifiedDate) - new Date(a.modifiedDate)); // Sắp xếp theo modifiedDate mới nhất
console.log("response.data:", cartItems.value); // <--- check kỹ dòng này
    console.log("Modified dates:", response.data.map(i => i.modifiedDate));
    console.log("Đã sắp xếp:", cartItems.value.map(i => i.modifiedDate));
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm', error);
  }
}


onMounted(() => {
  if (customerId) {
    fetchCartDetail();
  } else {
    console.error("Không tìm thấy customerId trong localStorage");
  }
});




async function removeItem(cartDetailId) {
  if (!confirm("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")) {
    return;
  }
  try {
    await axios.delete(`http://localhost:8080/cartDetail/delete/${cartDetailId}`);
    cartItems.value = cartItems.value.filter(
      (item) => item.cartDetailId !== cartDetailId
    );
  } catch (error) {
    console.error("Lỗi khi xóa:", error);
    alert("Xóa sản phẩm thất bại.");
  }
}

async function updateQuantity(cartDetailId, quantity) {
  if (quantity < 1) {
    alert("Số lượng tối thiểu là 1");
    return;
  }

  try {
    await axios.put(`http://localhost:8080/cartDetail/updateQuantityByCartDetailID/${cartDetailId}`, {
      quantity: quantity,
    });
    // Bạn có thể thêm thông báo hoặc cập nhật giao diện tại đây nếu cần
  } catch (error) {
    console.error("Lỗi khi cập nhật số lượng:", error);
    alert("Cập nhật số lượng thất bại.");
  }
}

// ✅ Chỉ tính tổng tiền của sản phẩm được chọn
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    if (selectedItems.value.includes(item.cartDetailId)) {
      return total + item.price * item.quantity;
    }
    return total;
  }, 0);
});

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

function handleCheckout() {
  // Lấy danh sách sản phẩm đã chọn
  const dataToPay = cartItems.value
    .filter(item => selectedItems.value.includes(item.cartDetailId))
    .map(item => ({
      cartDetailId: item.cartDetailId,
      productDetailId: item.productDetailId,
      images: item.images,
      productName: item.productName,
      color: item.color,
      size: item.size,
      price: item.price,
      quantity: item.quantity,
    }));
    console.log("Sản phẩm đã chọn để thanh toán:", dataToPay);

  // ✅ Lưu vào sessionStorage
  sessionStorage.setItem("checkoutItems", JSON.stringify(dataToPay));

  // 👉 Điều hướng sang trang thanh toán
  router.push("/payment");
}
// Tính toán trạng thái "chọn tất cả"
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length;
});

// Hàm chọn hoặc bỏ chọn tất cả
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = [];
  } else {
    selectedItems.value = cartItems.value.map(item => item.cartDetailId);
  }
};
function goToProductDetail(productDetailId) {
  router.push(`/productdetail/${productDetailId}`);
  console.log("sp chi tiết: " + productDetailId);
}

</script>
<template>
  <Banner title="Giỏ Hàng" breadcrumb="Giỏ hàng"
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container my-5 bg-white p-4 rounded shadow">
    <h3 class="mb-4 fw-semibold">🛒 Giỏ Hàng Của Bạn</h3>
    <!-- Nút Chọn tất cả -->
    <div class="form-check mb-3">
      <input
        type="checkbox"
        class="form-check-input"
        id="selectAll"
        :checked="isAllSelected"
        @change="toggleSelectAll"
      />
      <label class="form-check-label" for="selectAll">
        Chọn tất cả
      </label>
    </div>
    <div v-if="cartItems && cartItems.length > 0">
      <div id="cartItems">
        <div v-for="item in cartItems" :key="item.cartDetailId" class="row align-items-center g-3 mb-4 cart-item">
                     <!-- ✅ Checkbox -->
          <div class="col-1 text-center">
            <input type="checkbox" :value="item.cartDetailId" v-model="selectedItems" class="form-check-input"/>
          </div> 
          <div class="col-2 text-center">
            <img :src="item.images" 
            :alt="item.productName" 
            class="img-fluid rounded clickable" 
            @click="goToProductDetail(item.productDetailId)"
            style="max-width: 96px; height: auto" />
          </div>
          <div class="col-4">
              <h6
              class="mb-1 fw-medium text clickable"
              @click="goToProductDetail(item.productDetailId)"
              style="cursor: pointer;"
            >{{ item.productName }}</h6>
            <small class="text-muted">Color: {{ item.color }} | Size: {{ item.size }}</small>
          </div>
          <div class="col-2 text-danger fw-bold">
            <p class="text-danger fw-semibold mb-0">
              {{ formatCurrency(item.price) }}
            </p>
          </div>
          <div class="col-1">
            <input
              type="number"
              v-model="item.quantity"
              min="1"
              class="form-control text-center quantity"
              @change="updateQuantity(item.cartDetailId, item.quantity)"
            />
          </div>
        <!-- Nút Xóa -->
        <div class="col-2 d-flex justify-content-end">
          <button class="btn btn-outline-danger btn-sm" @click="removeItem(item.cartDetailId)">Xóa</button>
        </div>

        </div>
      </div>

      <!-- ✅ Tổng tiền của sản phẩm được chọn -->
      <div class="border-top pt-4 mt-4">
        <h5 class="text-end fw-semibold">
          Tổng cộng:
          <span id="totalPrice" class="text-danger fw-bold">
            {{ formatCurrency(totalPrice) }}
          </span>
        </h5>
        <div class="mt-4 d-flex flex-column flex-md-row justify-content-end gap-2">
          <router-link to="/product" class="btn btn-outline-secondary">
            Tiếp tục mua sắm
          </router-link>
        <button class="btn btn-success text-center" @click="handleCheckout()">
          🧾 Thanh toán
        </button>
        </div>
      </div>
    </div>

    <div v-else class="text-center p-5">
      <p class="fs-5">Giỏ hàng của bạn đang trống.</p>
      <router-link to="/product" class="btn btn-primary">Bắt đầu mua sắm</router-link>
    </div>
  </div>
</template>

<style scoped>
.cart-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item img {
  max-height: 96px;
  object-fit: contain;
}

.cart-item .quantity {
  max-width: 80px;
}
.cart-item img {
  object-fit: contain;
}
.cart-item {
  transition: background-color 0.2s ease-in-out;
  border-radius: 6px;
  padding: 12px;
}

.cart-item:hover {
  background-color: rgba(0, 0, 0, 0.03); /* hoặc đổi thành màu khác nếu muốn */
}
</style>
