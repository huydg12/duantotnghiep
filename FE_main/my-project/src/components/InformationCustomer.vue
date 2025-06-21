<script setup>
import { ref } from "vue";

// Khai báo reactive cho tab đang active
const activeTab = ref("info");

// Hàm đổi tab
function showTab(tabId) {
  activeTab.value = tabId;
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
            Xin chào, <strong class="fw-semibold">Đào Gia Huy</strong>!
          </p>
          <ul class="nav nav-pills flex-column">
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'info' }" @click.prevent="showTab('info')">Thông tin tài khoản</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'orders' }" @click.prevent="showTab('orders')">Đơn hàng của bạn</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'password' }" @click.prevent="showTab('password')">Đổi mật khẩu</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'address' }" @click.prevent="showTab('address')">Địa chỉ nhận hàng</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link text-danger">Đăng xuất</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Nội dung chính -->
      <div class="col-lg-7 col-md-7">
        <!-- Thông tin tài khoản -->
        <div v-show="activeTab === 'info'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">THÔNG TIN TÀI KHOẢN</h3>
          <form>
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Họ tên</label>
                <input type="text" class="form-control" value="Gia Huy Đào" />
              </div>
              <div class="col-md-6">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" value="huydgpp03111@gmail.com" />
              </div>
              <div class="col-md-6">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" value="0123456789" />
              </div>
              <div class="col-md-6">
                <label class="form-label">Ngày sinh</label>
                <input type="date" class="form-control" />
              </div>
            </div>
            <button type="submit" class="btn btn-dark mt-4">Cập nhật</button>
          </form>
        </div>

        <!-- Đơn hàng -->
        <div v-show="activeTab === 'orders'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐƠN HÀNG CỦA BẠN</h3>
          <div class="table-responsive">
            <table class="table table-bordered">
              <thead class="table-light">
                <tr>
                  <th>Đơn hàng</th>
                  <th>Ngày</th>
                  <th>Địa chỉ</th>
                  <th>Giá trị</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>#12345</td>
                  <td>15/06/2025</td>
                  <td>867 Trường Chinh, Kiến An</td>
                  <td>1.200.000đ</td>
                  <td>Đã thanh toán</td>
                  <td>
                    <a href="#" class="btn btn-link p-0">Chi tiết</a> <br>
                    <a href="#" class="btn btn-link p-0 text-warning" data-bs-toggle="modal" data-bs-target="#trackingModal">Theo dõi</a> <br>
                    <a href="#" class="btn btn-link p-0 text-success">Mua lại</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Đổi mật khẩu -->
        <div v-show="activeTab === 'password'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỔI MẬT KHẨU</h3>
          <form>
            <div class="mb-3">
              <input type="password" placeholder="Mật khẩu cũ" class="form-control" />
            </div>
            <div class="mb-3">
              <input type="password" placeholder="Mật khẩu mới" class="form-control" />
            </div>
            <div class="mb-3">
              <input type="password" placeholder="Xác nhận mật khẩu mới" class="form-control" />
            </div>
            <button class="btn btn-dark">Đặt lại mật khẩu</button>
          </form>
        </div>

        <!-- Địa chỉ nhận hàng -->
        <div v-show="activeTab === 'address'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỊA CHỈ NHẬN HÀNG</h3>
          <div class="mb-4">
            <div class="border rounded p-3 mb-3 d-flex justify-content-between bg-light">
              <div>
                <p class="fw-semibold mb-1">Gia Huy Đào</p>
                <p class="mb-1">867 Trường Chinh, Kiến An, Hải Phòng</p>
                <p class="mb-1">SĐT: 0123456789</p>
                <span class="badge bg-success">Mặc định</span>
              </div>
              <div>
                <a href="#" class="btn btn-sm btn-link">Sửa</a>
                <a href="#" class="btn btn-sm btn-link text-danger">Xoá</a>
              </div>
            </div>

            <div class="border rounded p-3 d-flex justify-content-between">
              <div>
                <p class="fw-semibold mb-1">Gia Huy Đào</p>
                <p class="mb-1">29 Hào Khê, Lê Chân, Hải Phòng</p>
                <p class="mb-1">SĐT: 0987654321</p>
              </div>
              <div>
                <a href="#" class="btn btn-sm btn-link">Sửa</a>
                <a href="#" class="btn btn-sm btn-link text-danger">Xoá</a>
                <a href="#" class="btn btn-sm btn-link text-success">Đặt mặc định</a>
              </div>
            </div>
          </div>

          <!-- Thêm địa chỉ mới -->
          <div class="border-top pt-4">
            <h4 class="h6 mb-3">Thêm địa chỉ mới</h4>
            <form class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Họ tên người nhận</label>
                <input type="text" class="form-control" />
              </div>
              <div class="col-md-6">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" />
              </div>
              <div class="col-12">
                <label class="form-label">Địa chỉ chi tiết</label>
                <input type="text" class="form-control" />
              </div>
              <div class="col-12">
                <button type="button" class="btn btn-dark">Thêm địa chỉ</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal Theo dõi đơn hàng -->
  <div class="modal fade" id="trackingModal" tabindex="-1" aria-labelledby="trackingModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Theo dõi đơn hàng #DH123456</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p><strong>Ngày đặt:</strong> 12/06/2025</p>
          <p><strong>Trạng thái:</strong> <span class="text-success">Đang giao</span></p>
          <p><strong>Người nhận:</strong> Đào Huy</p>
          <p><strong>Địa chỉ:</strong> 867 Trường Chinh, Kiến An, Hải Phòng</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
body {
  background-color: #f8f9fa;
}

/* Kiểu chung cho các link trong sidebar */
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

.actions-cell .btn {
  display: block;
  margin-bottom: 5px;
  text-align: left;
}

.tracking-progress-bar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.tracking-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  width: 80px;
}

.tracking-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 2;
  background-color: #6c757d;
}

.tracking-icon.completed {
  background-color: #198754;
}

.tracking-label {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #6c757d;
  text-align: center;
}

.tracking-label.completed {
  color: #198754;
}

.tracking-connector {
  height: 4px;
  width: 100%;
  background-color: #dee2e6;
  position: absolute;
  top: 18px;
  left: 50%;
  z-index: 1;
}

.tracking-connector.completed {
  background-color: #198754;
}
</style>
