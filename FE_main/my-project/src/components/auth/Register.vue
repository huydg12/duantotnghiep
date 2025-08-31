<script setup>
import { reactive, watch, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

const form = reactive({
    fullname: '',
    phone: '',
    email: '',
    username: '',
    password: '',
})


const retypePassword = ref('')
const confirmError = ref('')
// hàm so sánh
const equals = (a, b) => String(a ?? '') === String(b ?? '')
// tự xoá lỗi khi người dùng gõ lại
watch([() => form.password, () => form.confirmPassword], () => {
  if (confirmError.value && form.confirmPassword === form.password) {
    confirmError.value = ''
  }
})
const handleRegister = async () => {
  confirmError.value = ''
  if (!equals(form.password, retypePassword.value)) {
    confirmError.value = 'Mật khẩu nhập lại không khớp.'
    return
  }

  try {
    const res = await axios.post('http://localhost:8080/auth/register', form)
    const customerId = res.data.id
    await axios.post('http://localhost:8080/cart/add', { customerId })

    await Swal.fire({
      icon: 'success',
      title: 'Đăng ký thành công',
      timer: 1500,
      showConfirmButton: false
    })
    router.push('/auth/login')
  } catch (e) {
    alert('Đăng ký không thành công')
  }
}

</script>

<template>
    <form @submit.prevent="handleRegister">
        <div class="mb-3">
            <label class="form-label fw-semibold">HỌ VÀ TÊN</label>
            <input v-model="form.fullname" type="text" class="form-control" placeholder="Nhập họ và tên" required />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">SỐ ĐIỆN THOẠI</label>
            <input v-model="form.phone" type="tel" class="form-control" placeholder="Nhập số điện thoại" pattern="^(0[0-9]{9})$" required/>
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">EMAIL</label>
            <input v-model="form.email" type="email" class="form-control" placeholder="Nhập địa chỉ email" required />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">TÀI KHOẢN</label>
            <input v-model="form.username" type="Text" class="form-control" placeholder="Nhập tài khoản" required />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">MẬT KHẨU</label>
            <input v-model="form.password" type="password" class="form-control" placeholder="Nhập Mật khẩu" required />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">XÁC NHẬN MẬT KHẨU</label>
            <input v-model="retypePassword" type="password" class="form-control" placeholder="Nhập lại xác nhận mật khẩu" required />
            <span v-if="confirmError" class="text-danger small">{{ confirmError }}</span>
        </div>
        <button type="submit" class="btn btn-dark w-100 fw-semibold">
            TẠO TÀI KHOẢN
        </button>
    </form>
</template>

<style scoped></style>
