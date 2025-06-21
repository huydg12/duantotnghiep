<script setup>
import { ref } from 'vue'

const user = ref({
  name: 'Đào Gia Huy',
  email: 'huydgpp03111@gmail.com',
  phone: '0123456789',
  birthday: ''
})

const activeTab = ref('info')

const tabs = [
  { id: 'info', label: 'Thông tin tài khoản' },
  { id: 'orders', label: 'Đơn hàng của bạn' },
  { id: 'password', label: 'Đổi mật khẩu' },
  { id: 'address', label: 'Địa chỉ nhận hàng' }
]

function showTab(tabId) {
  activeTab.value = tabId
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <!-- Sidebar -->
      <div class="col-lg-3 col-md-4">
        <div class="sidebar-nav">
          <h2 class="h5 mb-4">TRANG TÀI KHOẢN</h2>
          <p class="mb-4">
            Xin chào, <strong class="fw-semibold">{{ user.name }}</strong>!
          </p>
          <ul class="nav nav-pills flex-column">
            <li class="nav-item mb-1" v-for="tab in tabs" :key="tab.id">
              <a href="#" class="nav-link" :class="{ active: activeTab === tab.id }" @click.prevent="showTab(tab.id)">
                {{ tab.label }}
              </a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link text-danger">Đăng xuất</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Nội dung tab -->
      <div class="col-lg-7 col-md-7">
        <div class="p-3 border rounded bg-white">
          <!-- Tab: Thông tin tài khoản -->
          <div v-if="activeTab === 'info'">
            <h3 class="h6 mb-3">THÔNG TIN TÀI KHOẢN</h3>
            <form class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Họ tên</label>
                <input type="text" class="form-control" v-model="user.name">
              </div>
              <div class="col-md-6">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" v-model="user.email">
              </div>
              <div class="col-md-6">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="user.phone">
              </div>
              <div class="col-md-6">
                <label class="form-label">Ngày sinh</label>
                <input type="date" class="form-control" v-model="user.birthday">
              </div>
              <div class="col-12 mt-3">
                <button type="submit" class="btn btn-dark">Cập nhật</button>
              </div>
            </form>
          </div>

          <!-- Các tab khác -->
          <div v-else>
            <p>Bạn đã chọn tab: <strong>{{ tabs.find(t => t.id === activeTab)?.label }}</strong></p>
            <p>(Chưa có nội dung)</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css");

body {
  background-color: #f8f9fa;
}
.sidebar-nav .nav-link {
  color: #212529;
  padding: 0.6rem 1rem;
  border-radius: 0.375rem;
}
.sidebar-nav .nav-link:not(.active):hover {
  background-color: #e9ecef;
}
.sidebar-nav .nav-link.active {
  background-color: #e0e7ff;
  font-weight: 600;
  color: #212529;
}
.sidebar-nav .nav-link.text-danger {
  font-weight: 500;
}
.sidebar-nav .nav-link.text-danger:hover {
  background-color: #f8d7da;
}
</style>
