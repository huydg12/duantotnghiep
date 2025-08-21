// src/stores/cartFavoriteStore.js
import { defineStore } from "pinia";
import { ref } from "vue";
import axios from "axios";

export const useCartFavoriteStore = defineStore("cartFavorite", () => {
  const cartItems = ref([]);
  const favoriteItems = ref([]);

  const fetchCartItems = async (customerId) => {
    if (!customerId) return;
    try {
      const res = await axios.get(
        `http://localhost:8080/cartDetail/showCartDetail/${customerId}`
      );
      console.log("📦 Dữ liệu xem cartDetail:", res.data); // Log dữ liệu trả về từ API
      cartItems.value = res.data;
    } catch (err) {
      console.error("❌ Lỗi lấy cart", err);
    }
  };

  const fetchFavoriteItems = async (customerId) => {
    if (!customerId) return;
    try {
      const res = await axios.get(
        `http://localhost:8080/favorite/show/${customerId}`
      );
      favoriteItems.value = res.data;
    } catch (err) {
      console.error("❌ Lỗi lấy favorite", err);
    }
  };

  const refreshAll = async (customerId) => {
    await Promise.all([
      fetchCartItems(customerId),
      fetchFavoriteItems(customerId),
    ]);
  };

  return {
    cartItems,
    favoriteItems,
    fetchCartItems,
    fetchFavoriteItems,
    refreshAll,
  };
});
