<script setup>
import { ref } from 'vue'

const content = ref(`
  <h1 class="display-6 fw-bold text-dark mb-3">Chào mừng đến với hệ thống quản lý bán giày</h1>
  <p class="text-secondary fs-5">Chọn chức năng bên trái để bắt đầu làm việc.</p>
`)

function loadContent(target) {
  const htmlMap = {
    banhang: `<h2 class="h3 fw-bold mb-3 text-dark">Bán hàng tại quầy</h2><p class="fs-5 text-secondary">Giao diện bán hàng tại cửa hàng...</p>`,
    sanpham: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý sản phẩm</h2><p class="fs-5 text-secondary">Danh sách sản phẩm...</p>`,
    hang: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý hãng</h2><p class="fs-5 text-secondary">Thêm mới hãng...</p>`,
    size: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý size</h2><p class="fs-5 text-secondary">Thiết lập danh sách size...</p>`,
    mau: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý màu sắc</h2><p class="fs-5 text-secondary">Màu sắc giày có sẵn...</p>`,
    loaigiay: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý loại giày</h2><p class="fs-5 text-secondary">Sneaker, boot, sandal,...</p>`,
    hoadon: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý hóa đơn</h2><p class="fs-5 text-secondary">Danh sách hóa đơn...</p>`,
    nhanvien: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý nhân viên</h2><p class="fs-5 text-secondary">Thêm mới nhân viên...</p>`,
    khachhang: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý khách hàng</h2><p class="fs-5 text-secondary">Khách hàng thân thiết...</p>`,
    kho: `<h2 class="h3 fw-bold mb-3 text-dark">Quản lý kho</h2><p class="fs-5 text-secondary">Tồn kho từng sản phẩm...</p>`,
    thongke: `<h2 class="h3 fw-bold mb-3 text-dark">Thống kê - Doanh thu</h2><p class="fs-5 text-secondary">Biểu đồ doanh thu...</p>`,
    canhan: `<h2 class="h3 fw-bold mb-3 text-dark">Thông tin cá nhân</h2><p class="fs-5 text-secondary">Xem và cập nhật thông tin tài khoản quản trị.</p>`,
    dangxuat: `<h2 class="h3 fw-bold mb-3 text-danger">Đăng xuất</h2><p class="fs-5 text-secondary">Bạn đã đăng xuất khỏi hệ thống. Hẹn gặp lại!</p>`,
  }

  content.value = htmlMap[target] || '<h2 class="h3">Trang chưa có nội dung</h2>'
}

const menuItems = [
  { id: 'banhang', label: 'Bán hàng tại quầy', target: 'banhang', icon: 'fa-solid fa-cash-register' },
  { id: 'sanpham', label: 'Sản phẩm', target: 'sanpham', icon: 'fa-solid fa-box-open' },
  {
    id: 'submenu',
    label: 'Thuộc tính',
    icon: 'fa-solid fa-sliders',
    sub: [
      { label: 'Hãng', target: 'hang', icon: 'fa-solid fa-tags' },
      { label: 'Size', target: 'size', icon: 'fa-solid fa-ruler-horizontal' },
      { label: 'Màu', target: 'mau', icon: 'fa-solid fa-palette' },
      { label: 'Loại giày', target: 'loaigiay', icon: 'fa-solid fa-shoe-prints' },
    ],
  },
  { id: 'hoadon', label: 'Hóa đơn', target: 'hoadon', icon: 'fa-solid fa-receipt' },
  { id: 'nhanvien', label: 'Nhân viên', target: 'nhanvien', icon: 'fa-solid fa-user-tie' },
  { id: 'khachhang', label: 'Khách hàng', target: 'khachhang', icon: 'fa-solid fa-users' },
  { id: 'kho', label: 'Kho', target: 'kho', icon: 'fa-solid fa-warehouse' },
  { id: 'thongke', label: 'Thống kê', target: 'thongke', icon: 'fa-solid fa-chart-line' },
  { id: 'canhan', label: 'Thông tin cá nhân', target: 'canhan', icon: 'fa-solid fa-id-card' },
  { id: 'dangxuat', label: 'Đăng xuất', target: 'dangxuat', icon: 'fa-solid fa-sign-out-alt' },
]
</script>

<template>
  <div class="d-flex">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="flex-grow-1 sidebar-scroll">
        <div class="text-center py-4 border-bottom border-secondary user-profile">
          <div
            class="bg-white rounded-circle mx-auto d-flex align-items-center justify-content-center shadow-sm user-icon">
            <i class="fa-solid fa-user text-dark fa-2x"></i>
          </div>
          <h2 class="mt-3 fs-5 fw-semibold">
            Xin chào, <span class="fw-bold">Admin</span>
          </h2>
        </div>

        <ul class="nav flex-column p-3">
          <li v-for="item in menuItems" :key="item.id" class="nav-item">
            <a v-if="!item.sub" href="#" class="nav-link menu-btn" @click.prevent="loadContent(item.target)">
              <i :class="item.icon"></i> {{ item.label }}
            </a>

            <template v-else>
              <a class="nav-link" data-bs-toggle="collapse" :href="'#' + item.id" role="button" aria-expanded="false"
                :aria-controls="item.id">
                <i :class="item.icon"></i> {{ item.label }}
                <i class="fa-solid fa-chevron-down ms-auto"></i>
              </a>
              <div class="collapse submenu-collapse" :id="item.id">
                <ul class="nav flex-column">
                  <li v-for="sub in item.sub" :key="sub.target">
                    <a href="#" class="nav-link menu-btn" @click.prevent="loadContent(sub.target)">
                      <i :class="sub.icon"></i> {{ sub.label }}
                    </a>
                  </li>
                </ul>
              </div>
            </template>
          </li>
        </ul>
      </div>
    </div>

    <!-- Main content -->
    <div class="flex-grow-1 p-4 main-content">
      <div id="content" class="transition-all" v-html="content" />
    </div>
  </div>
</template>

<style scoped>
@import 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css';
@import 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css';

.sidebar {
  width: 288px;
  background-color: #212529;
  color: white;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.sidebar .user-profile .user-icon {
  width: 96px;
  height: 96px;
}

.sidebar .nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 0.75rem 1rem;
  display: flex;
  align-items: center;
  border-radius: 0.375rem;
}

.sidebar .nav-link:hover {
  background-color: #495057;
  color: white;
}

.sidebar .nav-link i {
  margin-right: 0.75rem;
}

.submenu-collapse {
  background-color: #212529;
}

.submenu-collapse .nav-link {
  padding: 0.5rem 1rem 0.5rem 2.5rem;
  font-size: 0.9rem;
}

.main-content {
  background-color: white;
  border-top-left-radius: 2rem;
  border-bottom-left-radius: 2rem;
  box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.sidebar-scroll {
  overflow-y: auto;
}

.sidebar-scroll::-webkit-scrollbar {
  width: 8px;
}

.sidebar-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
}

.sidebar:hover .sidebar-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.5);
}
</style>
