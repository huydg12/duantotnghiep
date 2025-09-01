<script setup>
import { reactive, watch, ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'
const router = useRouter();

const form = reactive({
  fullname: '',
  phone: '',
  email: '',
  username: '',
  password: '',
})
const customers = ref([])
const emailSet = ref(new Set())
const emailError = ref('')
const normalizeEmail = (s) => String(s || '').trim().toLowerCase()



async function fetchCustomers() {
  try {
    const res = await axios.get('http://localhost:8080/customer/show')
    customers.value = Array.isArray(res.data) ? res.data : []
    emailSet.value = new Set(
      customers.value
        .map(c => normalizeEmail(c.email))
        .filter(e => !!e)
    )
  } catch (err) {
    // Không chặn đăng ký nếu load fail, nhưng log để biết
    console.error('Lỗi load customer:', err)
  }
}
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

// NEW: kiểm tra email trùng khi người dùng gõ
watch(() => form.email, (val) => {
  const exists = emailSet.value.has(normalizeEmail(val))
  emailError.value = exists ? 'Email đã tồn tại' : ''
})


/* ===== Username duplicate (ACCOUNT) ===== */
const accounts = ref([])
const usernameSet = ref(new Set())
const usernameError = ref('')
const normalizeUsername = (s) => String(s || '').trim().toLowerCase()

async function fetchAccounts() {
  try {
    const res = await axios.get('http://localhost:8080/account/show')
    accounts.value = Array.isArray(res.data) ? res.data : []
    usernameSet.value = new Set(
      accounts.value.map(a => normalizeUsername(a.username)).filter(Boolean)
    )
  } catch (err) {
    console.error('Lỗi load account:', err)
  }
}
watch(() => form.username, (val) => {
  usernameError.value = usernameSet.value.has(normalizeUsername(val)) ? 'Tài khoản đã tồn tại' : ''
})

const handleRegister = async () => {
  confirmError.value = ''
  emailError.value = ''
  usernameError.value = ''

  if (!equals(form.password, retypePassword.value)) {
    confirmError.value = 'Mật khẩu nhập lại không khớp.'
    return
  }

  // Double-check trước khi gọi BE
  if (emailSet.value.has(normalizeEmail(form.email))) {
    emailError.value = 'Email đã tồn tại'
    await Swal.fire({ icon: 'error', title: 'Email đã tồn tại', text: 'Vui lòng dùng email khác.' })
    return
  }
  if (usernameSet.value.has(normalizeUsername(form.username))) {
    usernameError.value = 'Tài khoản đã tồn tại'
    await Swal.fire({ icon: 'error', title: 'Tài khoản đã tồn tại', text: 'Vui lòng dùng tên đăng nhập khác.' })
    return
  }

  try {
    const res = await axios.post('http://localhost:8080/auth/register', form)
    const customerId = res.data.id
    await axios.post('http://localhost:8080/cart/add', { customerId })

    // cập nhật cache sau khi tạo thành công
    emailSet.value.add(normalizeEmail(form.email))
    usernameSet.value.add(normalizeUsername(form.username))

    await Swal.fire({ icon: 'success', title: 'Đăng ký thành công', timer: 1500, showConfirmButton: false })
    router.push('/auth/login')
  } catch (e) {
    const status = e?.response?.status
    const msg = (e?.response?.data?.message || '').toString()

    if (status === 409 || /email.+tồn tại/i.test(msg)) {
      await Swal.fire({ icon: 'error', title: 'Email đã tồn tại', text: 'Vui lòng dùng email khác.' })
      return
    }
    if (status === 409 || /username.+tồn tại/i.test(msg) || /account.+exists/i.test(msg)) {
      await Swal.fire({ icon: 'error', title: 'Tài khoản đã tồn tại', text: 'Vui lòng dùng tên đăng nhập khác.' })
      return
    }
    await Swal.fire({ icon: 'error', title: 'Đăng ký không thành công', text: 'Vui lòng thử lại.' })
  }
}
onMounted(async () => {
  await Promise.all([fetchCustomers(), fetchAccounts()])
})
</script>

<template>
  <form @submit.prevent="handleRegister">
    <div class="mb-3">
      <label class="form-label fw-semibold">HỌ VÀ TÊN</label>
      <input v-model="form.fullname" type="text" class="form-control" placeholder="Nhập họ và tên" required />
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">SỐ ĐIỆN THOẠI</label>
      <input v-model="form.phone" type="tel" class="form-control" placeholder="Nhập số điện thoại"
        pattern="^(0[0-9]{9})$" required />
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">EMAIL</label>
      <input v-model="form.email" type="email" class="form-control" placeholder="Nhập địa chỉ email"
        :aria-invalid="!!emailError" required />
      <!-- Lỗi hiển thị bằng span bên dưới input -->
      <span v-if="emailError" class="text-danger small d-block mt-1">
        {{ emailError }}
      </span>
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">TÀI KHOẢN</label>
      <input v-model="form.username" type="Text" class="form-control" placeholder="Nhập tài khoản"
      :aria-invalid="!!usernameError" required />
      <span v-if="usernameError" class="text-danger small d-block mt-1">{{ usernameError }}</span>
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">MẬT KHẨU</label>
      <input v-model="form.password" type="password" class="form-control" placeholder="Nhập Mật khẩu" required />
    </div>
    <div class="mb-3">
      <label class="form-label fw-semibold">XÁC NHẬN MẬT KHẨU</label>
      <input v-model="retypePassword" type="password" class="form-control" placeholder="Nhập lại xác nhận mật khẩu"
        required />
      <span v-if="confirmError" class="text-danger small d-block mt-1">{{ confirmError }}</span>
    </div>
    <button type="submit" class="btn btn-dark w-100 fw-semibold">
      TẠO TÀI KHOẢN
    </button>
  </form>
</template>

<style scoped></style>
