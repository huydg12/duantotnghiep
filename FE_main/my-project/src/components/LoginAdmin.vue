<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')
const emailReset = ref('')
const errorMessage = ref(false)

function submitLogin() {
  if (username.value === 'admin' && password.value === '123') {
    errorMessage.value = false
    // Xử lý đăng nhập thành công (chuyển trang, gọi API, lưu token,...)
    alert('Đăng nhập thành công!')
  } else {
    errorMessage.value = true
  }
}

function sendResetRequest() {
  if (emailReset.value) {
    alert(`Yêu cầu khôi phục đã gửi đến: ${emailReset.value}`)
    emailReset.value = ''
  }
}
</script>

<template>
  <div class="d-flex justify-content-center align-items-center vh-100 bg-light">
    <div class="login-card p-4 bg-white rounded-4 shadow w-100" style="max-width: 400px">
      <h3 class="text-center mb-4">Đăng nhập</h3>
      <form @submit.prevent="submitLogin">
        <div class="mb-3">
          <label for="username" class="form-label">Tài khoản</label>
          <input type="text" id="username" class="form-control" v-model="username" required />
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">Mật khẩu</label>
          <input type="password" id="password" class="form-control" v-model="password" required />
        </div>
        <div class="mb-3 text-end">
          <a href="#" class="forgot-link" data-bs-toggle="modal" data-bs-target="#forgotModal">Quên mật khẩu?</a>
        </div>
        <div class="d-grid">
          <button type="submit" class="btn btn-primary">Đăng nhập</button>
        </div>
        <div v-if="errorMessage" class="text-danger text-center mt-3">
          Tài khoản hoặc mật khẩu không đúng!
        </div>
      </form>
    </div>

    <!-- Modal Quên mật khẩu -->
    <div class="modal fade" id="forgotModal" tabindex="-1" aria-labelledby="forgotModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="forgotModalLabel">Quên mật khẩu</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <label for="emailReset" class="form-label">Nhập email khôi phục:</label>
            <input type="email" class="form-control" id="emailReset" v-model="emailReset" />
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
            <button class="btn btn-primary" @click="sendResetRequest" data-bs-dismiss="modal">Gửi yêu cầu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forgot-link {
  font-size: 0.9rem;
}
</style>

<!-- Thêm Bootstrap vào index.html hoặc main.js -->
<!-- <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"> -->
<!-- <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script> -->
