<script setup>
import Banner from "../common/Banner.vue";
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { useForgotPasswordStore } from "@/stores/emailStore";

const store = useForgotPasswordStore();
const email = ref("");
const router = useRouter();

const sendRequest = async () => {
  try {
    const response = await axios.post("http://localhost:8080/auth/forgetpassword", {
      email: email.value,
    });
    alert("Gửi OTP thành công, vui lòng kiểm tra email!");

    store.email = email.value;
    router.push("/auth/verifycode");
  } catch (error) {
    alert("Gửi OTP thất bại");
  }
};
</script>
<template>
  <Banner
    title="Quên mật khẩu"
    breadcrumb="Quên mật khẩu"
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg"
  />
  <div class="custom-box">
    <!-- Mũi tên quay lại -->
    <a href="/auth/login" class="back-arrow">&#8592;</a>

    <!-- Tiêu đề -->
    <h2 class="text-center fw-bold text-dark mb-4">Quên mật khẩu</h2>
    <form @submit.prevent="sendRequest">
      <div class="mb-3">
        <label for="emailInput" class="form-label">EMAIL ĐĂNG KÝ</label>
        <input
          v-model="email"
          type="email"
          id="emailInput"
          required
          placeholder="Nhập địa chỉ email"
          class="form-control"
        />
      </div>
      <button type="submit" class="btn btn-dark w-100 mt-3 fw-semibold">
        GỬI YÊU CẦU
      </button>
      <p class="info-text">
        Hệ thống sẽ gửi đường dẫn đặt lại mật khẩu đến email của bạn (nếu có tồn
        tại trong hệ thống).
      </p>
    </form>
  </div>
</template>

<style scoped>
.back-arrow {
  position: absolute;
  top: 1.5rem;
  left: 1.5rem;
  font-size: 1.25rem;
  color: black;
  text-decoration: none;
}

.back-arrow:hover {
  text-decoration: underline;
}

.custom-box {
  max-width: 400px;
  margin: 4rem auto;
  background-color: white;
  padding: 2rem;
  border-radius: 1rem;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  position: relative;
}

.form-label {
  font-weight: 600;
}

.info-text {
  font-size: 0.875rem;
  color: #6c757d;
  text-align: center;
  margin-top: 1rem;
}
</style>
