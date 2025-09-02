<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '../../stores/userStore';

const router = useRouter();
const userStore = useUserStore();

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const loading = ref(false);

const toBool = (v) => v === true || v === 'true' || v === 1 || v === '1';

const extractActiveFlag = (u) => {
  if (!u) return undefined;
  if (u.active !== undefined) return u.active;
  if (u.isActive !== undefined) return u.isActive;
  if (u.enabled !== undefined) return u.enabled;
  if (u.status !== undefined) return u.status;
  if (u.account?.active !== undefined) return u.account.active;
  if (u.employee?.active !== undefined) return u.employee.active;
  return undefined;
};

const isActiveUser = (u) => {
  const flag = extractActiveFlag(u);
  return flag === undefined ? true : toBool(flag);
};

const handleLogin = async () => {
  errorMessage.value = '';
  loading.value = true;

  try {
    const { data } = await axios.post('http://localhost:8080/auth/login', {
      username: username.value,
      password: password.value
    });

    if (!data?.user) {
      errorMessage.value = 'Phản hồi đăng nhập không hợp lệ từ máy chủ.';
      return;
    }

    if (!isActiveUser(data.user)) {
      errorMessage.value = 'Tài khoản của bạn đang không hoạt động. Vui lòng liên hệ quản trị viên.';
      return;
    }

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('user', JSON.stringify(data.user));
    userStore.setUser(data.user);

    if (data.user.roleId === 1 || data.user.roleId === 3) {
      await router.push('/manage');
    } else {
      await router.push('/home');
    }

    window.location.reload();

  } catch (error) {
    if (error?.response?.status === 403 && error?.response?.data?.code === 'INACTIVE') {
      errorMessage.value = 'Tài khoản đã bị vô hiệu hoá. Vui lòng liên hệ quản trị viên.';
    } else {
      errorMessage.value =
        error?.response?.data?.message ||
        'Tên tài khoản hoặc mật khẩu không đúng!';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <form @submit.prevent="handleLogin" novalidate>
    <div class="mb-3">
      <label class="form-label fw-semibold">TÀI KHOẢN</label>
      <input v-model="username" type="text" class="form-control" placeholder="Nhập tài khoản" required
        autocomplete="username">
    </div>

    <div class="mb-3">
      <label class="form-label fw-semibold">MẬT KHẨU</label>
      <input v-model="password" type="password" class="form-control" placeholder="Nhập mật khẩu" required
        autocomplete="current-password">
    </div>

    <div class="mb-3 text-end">
      <router-link to="/auth/forgetpassword" class="text-decoration-none text-primary">
        Quên mật khẩu?
      </router-link>
    </div>

    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <button type="submit" class="btn btn-dark w-100 fw-semibold" :disabled="loading">
      <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
      ĐĂNG NHẬP
    </button>
  </form>
</template>

<style scoped>
.spinner-border {
  vertical-align: text-bottom;
}
</style>
