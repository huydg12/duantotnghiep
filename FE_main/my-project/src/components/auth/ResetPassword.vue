<script setup>
import Banner from "../common/Banner.vue";
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useForgotPasswordStore } from "../../stores/emailStore";

const store = useForgotPasswordStore();
const newPassword = ref('');
const confirmPassword = ref('');
const router = useRouter();

const handleChangePassword = async () => {
  if (!newPassword.value || !confirmPassword.value) {
    alert("Vui lòng nhập đầy đủ mật khẩu.");
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    alert("Mật khẩu xác nhận không khớp.");
    return;
  }

  try {
    const response = await axios.post("http://localhost:8080/auth/resetpassword", {
      email: store.email,
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value
    })

    if (response.status === 200) {
      alert("Đổi mật khẩu thành công!");
      router.push("/auth/login");
    }
  } catch (error) {
    console.error("Lỗi đổi mật khẩu:", error);
    alert("Đổi mật khẩu thất bại.");
  }

}
</script>

<template>
  <Banner title="Đổi mật khẩu" breadcrumb="Đổi mật khẩu"
    backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
  <div class="container">
    <div class="bg-white shadow rounded p-4 mt-5 mx-auto" style="max-width: 480px;">
      <div class="position-relative mb-4 text-center">
        <router-link to="/auth/login"
          class="back-arrow position-absolute start-0 top-50 translate-middle-y text-dark fs-5">
          &#8592;
        </router-link>
        <h2 class="fs-4 fw-bold mb-0">Đổi Mật Khẩu</h2>
      </div>

      <form @submit.prevent="handleChangePassword">
        <div class="mb-3">
          <label for="newPassword" class="form-label fw-semibold">Mật khẩu mới:</label>
          <input v-model="newPassword" type="password" id="newPassword" required class="form-control"
            placeholder="Nhập mật khẩu mới" />
        </div>
        <div class="mb-3">
          <label for="confirmPassword" class="form-label fw-semibold">Xác nhận mật khẩu:</label>
          <input v-model="confirmPassword" type="password" id="confirmPassword" required class="form-control"
            placeholder="Xác nhận mật khẩu mới" />
        </div>

        <button type="submit" class="btn btn-dark w-100 fw-semibold">Xác nhận</button>

        <p class="text-center small text-muted mt-3">
          Mật khẩu mới phải đủ mạnh và dễ nhớ đối với bạn.
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.back-arrow {
  text-decoration: none;
  padding-left: 4px;
}
</style>
