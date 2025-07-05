<script setup>
import { ref, computed, onMounted  } from "vue";
import Banner from "./Banner.vue";
import axios from "axios";






const cartItems = ref([]);

// Lấy customerId từ localStorage (dữ liệu user lưu khi đăng nhập)
let customerId = 1;
// const userJson = localStorage.getItem("user");

// if (userJson) {
//   try {
//     const user = JSON.parse(userJson);
//     customerId = user.id;
//     console.log("✅ Customer ID:", customerId);
//   } catch (error) {
//     console.error("❌ Lỗi khi parse userJson:", error);
//   }
// } else {
//   console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user");
// }

const fetchCartDetail = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/cartDetail/showCartDetail/${customerId}`);
    cartItems.value = response.data;
console.log("response.data:", response.data); // <--- check kỹ dòng này
    console.log('IMG', cartItems.value.map(item => item.images));
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




function removeItem(cartDetailId) {
  cartItems.value = cartItems.value.filter((item) => item.cartDetailId !== cartDetailId);
}

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + item.price * item.quantity;
  }, 0);
});

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}
</script>
<template>
  <Banner title="Giỏ Hàng" breadcrumb="Giỏ hàng"
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container my-5 bg-white p-4 rounded shadow">
    <h3 class="mb-4 fw-semibold">🛒 Giỏ Hàng Của Bạn</h3>

    <div v-if="cartItems && cartItems.length > 0">
      <div id="cartItems">
        <div v-for="item in cartItems" :key="item.cartDetailId" class="row align-items-center g-3 mb-4 cart-item">
          <div class="col-12 col-md-2 text-center">
            <img :src="item.images" :alt="item.productName" class="img-fluid rounded" style="max-width: 96px; height: auto" />
          </div>
          <div class="col-12 col-md-4">
            <h6 class="mb-1 fw-medium">{{ item.productName }}</h6>
            <small class="text-muted">Color: {{ item.color }} | Size: {{ item.size }}</small>
          </div>
          <div class="col-6 col-md-2">
            <p class="text-danger fw-semibold mb-0">
              {{ formatCurrency(item.price) }}
            </p>
          </div>
          <div class="col-6 col-md-2">
            <input type="number" v-model="item.quantity" min="1" class="form-control text-center quantity" />
          </div>
          <div class="col-12 col-md-2 text-md-end">
            <button class="btn btn-outline-danger btn-sm" @click="removeItem(item.cartDetailId)">
              Xóa
            </button>
          </div>
        </div>
      </div>

      <div class="border-top pt-4 mt-4">
        <h5 class="text-end fw-semibold">
          Tổng cộng:
          <span id="totalPrice" class="text-danger fw-bold">{{formatCurrency(totalPrice)}}</span>
        </h5>
        <div class="mt-4 d-flex flex-column flex-md-row justify-content-end gap-2">
          <router-link to="/product" class="btn btn-outline-secondary">
            Tiếp tục mua sắm
          </router-link>
          <router-link to="/payment" class="btn btn-success text-center">
            🧾 Thanh toán
          </router-link>
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
</style>
