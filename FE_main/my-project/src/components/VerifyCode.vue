<script setup>
import Banner from "./Banner.vue";
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useForgotPasswordStore } from "../stores/emailStore";

const store = useForgotPasswordStore();
const router = useRouter();
const otpCode = ref("");

const handleCode = async () => {
  try {
    const response = await axios.post("http://localhost:8080/auth/verifycode", {
      otpCode: otpCode.value,
      email: store.email,
    });

    if (response.status === 200) {
      alert("Xác nhận mã thành công");
      router.push("/auth/resetpassword");
    } else {
      alert("Mã xác nhận không hợp lệ");
    }
  } catch (error) {
    console.error("Lỗi xác nhận mã:", error);
    alert("Mã xác nhận không đúng hoặc đã hết hạn.");
  }
};
</script>
<template>
  <Banner title="Xác nhận mã" breadcrumb="Xác nhận mã"
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
  <div class="container">
    <div class="xacnhan-container mx-auto">
      <router-link to="/auth/forgetpassword" class="back-arrow">&#8592;</router-link>

      <h1 class="text-center fw-bold text-dark mb-4">Xác nhận mã</h1>

      <form @submit.prevent="handleCode">
        <div class="mb-3">
          <label class="form-label fw-semibold">NHẬP MÃ XÁC NHẬN</label>
          <input v-model="otpCode" type="text" required placeholder="******" class="form-control code-input" />
        </div>
        <button type="submit" class="btn btn-dark w-100 confirm-btn mt-2">
          XÁC NHẬN
        </button>

        <p class="info-text">
          Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.xacnhan-container {
  max-width: 420px;
  margin-top: 4rem;
  padding: 2rem;
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  position: relative;
}

.back-arrow {
  position: absolute;
  top: 1.5rem;
  left: 1rem;
  font-size: 1.25rem;
  color: black;
  text-decoration: none;
}

.back-arrow:hover {
  text-decoration: underline;
}

.code-input {
  letter-spacing: 0.4em;
  font-size: 1.25rem;
  text-align: center;
}

.confirm-btn {
  font-weight: 600;
}

.info-text {
  font-size: 0.875rem;
  color: #6c757d;
  text-align: center;
  margin-top: 1rem;
}
</style>
