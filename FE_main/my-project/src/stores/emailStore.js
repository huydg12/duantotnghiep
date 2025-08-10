import { defineStore } from "pinia";
import { ref } from "vue";

export const useForgotPasswordStore = defineStore("forgotPassword", () => {
  const email = ref("");

  return {
    email,
  };
});
