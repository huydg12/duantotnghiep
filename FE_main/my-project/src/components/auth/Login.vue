<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios'

const router = useRouter();
import { useUserStore } from '../../stores/userStore';
const username = ref("");
const password = ref("");
const errorMessage = ref("");
const userStore = useUserStore(); // khởi tạo store
const handleLogin = async () => {
    errorMessage.value = ""; // reset lỗi trước khi gửi
  try {
    const response = await axios.post("http://localhost:8080/auth/login", {
      username: username.value,
      password: password.value
    })

    const data = response.data

    // Lưu token vào localStorage
    localStorage.setItem("accessToken", data.accessToken);
    // Lưu thông tin user vào localStorage (nếu cần)
    localStorage.setItem("user", JSON.stringify(data.user));
        userStore.setUser(data.user); // cập nhật reactive
    // Điều hướng dựa trên roleId
if (data.user.roleId === 1 || data.user.roleId === 3) {
  router.push("/manage").then(() => window.location.reload());
} else {
  router.push("/home").then(() => window.location.reload());
}
  } catch (error) {
    console.error(error);
    errorMessage.value = "Tên tài khoản hoặc mật khẩu không đúng!";
  }

};

</script>

<template>
  <form @submit.prevent="handleLogin">
    <div class="mb-3">
      <label class="form-label fw-semibold">TÀI KHOẢN</label>
      <input v-model="username" type="text" class="form-control" placeholder="Nhập tài khoản">
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">MẬT KHẨU</label>
      <input v-model="password" type="password" class="form-control" placeholder="Nhập mật khẩu">
    </div>
    <div class="mb-3 text-end">
      <router-link to="/auth/forgetpassword" class="text-decoration-none text-primary">Quên mật khẩu?</router-link>
    </div>
    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <button type="submit" class="btn btn-dark w-100 fw-semibold">ĐĂNG NHẬP</button>

  </form>
</template>

<style scoped>
</style>
