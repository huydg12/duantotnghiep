<script setup>

import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios'

const router = useRouter();

const username = ref("");
const password = ref("");

const handleLogin = async () => {
  try {
    const response = await axios.post("http://localhost:8080/auth/login", {
      username: username.value,
      password: password.value
    })

    const account = response.data
    if (account.roleId === 1 || account.roleId === 2) {
      router.push("/manage");
    } else {
      // router.push("/home");
    }
  } catch (error) {
    console.log(username) 
  }
  
}

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
      <router-link to="/resetPass" class="text-decoration-none text-primary">Quên mật khẩu?</router-link>
    </div>
    <button type="submit" class="btn btn-dark w-100 fw-semibold">ĐĂNG NHẬP</button>

  </form>
</template>

<style scoped></style>
