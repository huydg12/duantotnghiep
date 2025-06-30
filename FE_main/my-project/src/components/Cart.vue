<script setup>
import { ref, computed } from "vue";
import Banner from "./Banner.vue";

const cartItems = ref([
  {
    id: "CW2288-001",
    name: "Nike Air Force 1",
    size: 42,
    price: 2000000,
    quantity: 1,
    image: "/images/anh1.webp",
  },
]);

function removeItem(productID) {
  cartItems.value = cartItems.value.filter((item) => item.id !== productID);
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
        <div v-for="item in cartItems" :key="item.id" class="row align-items-center g-3 mb-4 cart-item">
          <div class="col-12 col-md-2 text-center">
            <img src="/images/anh1.webp" :alt="item.name" class="img-fluid rounded" style="max-width: 96px; height: auto" />
          </div>
          <div class="col-12 col-md-4">
            <h6 class="mb-1 fw-medium">{{ item.name }}</h6>
            <small class="text-muted">Mã: {{ item.id }} | Size: {{ item.size }}</small>
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
            <button class="btn btn-outline-danger btn-sm" @click="removeItem(item.id)">
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
