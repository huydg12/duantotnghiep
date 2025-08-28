// stores/userStore.js
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null,
  }),
  actions: {
    setUser(user) {
      this.user = user;
    },
    loadUserFromLocalStorage() {
      const u = localStorage.getItem("user");
      if (u) {
        this.user = JSON.parse(u);
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem("user");
      localStorage.removeItem("accessToken");
      this.removePaymentSuccessFlag(); // Xóa flag payment success khi logout
    },
    removePaymentSuccessFlag() {
      localStorage.removeItem("paymentSuccessFlag");
    },
  },
});