<script setup>
import { ref, reactive } from "vue";

// Khai báo reactive cho tab đang active
const activeTab = ref("info");

const userInfo = reactive({
  fullName: "Đào Gia Huy",
  email: "kun12112002@gmail.com",
  phone: "0912681528",
  birthDate: "2002-11-12",
});

const passwordData = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const orders = ref([
  {
    id: "#12345",
    date: "15/06/2025",
    address: "867 Trường Chinh, Kiến An",
    total: "1.200.000đ",
    status: "Đã thanh toán",
  },
  {
    id: "#12346",
    date: "18/06/2025",
    address: "29 Hào Khê, Lê Chân",
    total: "850.000đ",
    status: "Đang giao",
  },
]);

const addresses = ref([
  {
    id: 1,
    name: "Đào Gia Huy",
    address: "867 Trường Chinh, Kiến An, Hải Phòng",
    phone: "0123456789",
    isDefault: true,
  },
  {
    id: 2,
    name: "Đào Gia Huy",
    address: "29 Hào Khê, Lê Chân, Hải Phòng",
    phone: "0987654321",
    isDefault: false,
  },
]);

const newAddress = reactive({
  name: "",
  phone: "",
  address: "",
});

// Hàm đổi tab
function showTab(tabId) {
  activeTab.value = tabId;
}

function updateUserInfo() {
  alert("Đã cập nhật thông tin!");
}

function changePassword() {
  if (passwordData.newPassword !== passwordData.confirmPassword) {
    alert("Mật khẩu mới và xác nhận mật không khớp!");
  }
  alert("Đổi mật khẩu thành công!");
  passwordData.oldPassword = "";
  passwordData.newPassword = "";
  passwordData.confirmPassword = "";
}

function addNewAddress() {
  if (!newAddress.name || !newAddress.address || !newAddress.phone) {
    alert("Vui lòng điền đầy đủ thông tin địa chỉ!");
    return;
  }
  alert("Thêm địa chỉ mới thành công thành công!");
  newAddress.name = "";
  newAddress.address = "";
  newAddress.phone = "";
}

function logout() {
  if (confirm("Bạn có chắc muốn đăng xuất?")) {
    alert("Đã đăng xuất");
  }
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
            Xin chào,
            <strong class="fw-semibold">{{ userInfo.fullName }}</strong>!
          </p>
          <ul class="nav nav-pills flex-column">
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'info' }"
                @click.prevent="showTab('info')">Thông tin tài khoản</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'orders' }"
                @click.prevent="showTab('orders')">Đơn hàng của bạn</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'password' }"
                @click.prevent="showTab('password')">Đổi mật khẩu</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link" :class="{ active: activeTab === 'address' }"
                @click.prevent="showTab('address')">Địa chỉ nhận hàng</a>
            </li>
            <li class="nav-item mb-1">
              <a href="#" class="nav-link text-danger" @click.prevent="logout()">Đăng xuất</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Nội dung chính -->
      <div class="col-lg-7 col-md-7">
        <!-- Thông tin tài khoản -->
        <div v-show="activeTab === 'info'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">THÔNG TIN TÀI KHOẢN</h3>
          <form @submit.prevent="updateUserInfo">
            <div class="row g-3">
              <div class="col-md-6">
                <label for="fullName" class="form-label">Họ tên</label>
                <input type="text" id="fullName" class="form-control" v-model="userInfo.fullName" />
              </div>
              <div class="col-md-6">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" class="form-control" v-model="userInfo.email" />
              </div>
              <div class="col-md-6">
                <label for="phone" class="form-label">Số điện thoại</label>
                <input type="text" id="phone" class="form-control" v-model="userInfo.phone" />
              </div>
              <div class="col-md-6">
                <label for="birthDate" class="form-label">Ngày sinh</label>
                <input type="date" id="birthDate" class="form-control" v-model="userInfo.birthDate" />
              </div>
            </div>
            <button type="submit" class="btn btn-dark mt-4">Cập nhật</button>
          </form>
        </div>

        <!-- Đơn hàng -->
        <div v-show="activeTab === 'orders'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐƠN HÀNG CỦA BẠN</h3>
          <div class="table-responsive">
            <table class="table table-bordered align-middle">
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
                <tr v-if="orders.length === 0">
                  <td colspan="6" class="text-center p-4">
                    Bạn chưa có đơn hàng nào
                  </td>
                </tr>
                <tr v-for="order in orders" :key="order.id">
                  <td>{{ order.id }}</td>
                  <td>{{ order.date }}</td>
                  <td>{{ order.address }}</td>
                  <td>{{ order.total }}</td>
                  <td>{{ order.status }}</td>
                  <td>
                    <a href="#" class="btn btn-link p-0 d-block text-start">Chi tiết</a>
                    <a href="#" class="btn btn-link p-0 d-block text-start text-warning" data-bs-toggle="modal"
                      data-bs-target="#trackingModal">Theo dõi</a>
                    <a href="#" class="btn btn-link p-0 d-block text-start text-success">Mua lại</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Đổi mật khẩu -->
        <div v-show="activeTab === 'password'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỔI MẬT KHẨU</h3>
          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <input type="password" id="oldPassword" placeholder="Mật khẩu cũ" class="form-control"
                v-model="passwordData.oldPassword" required />
            </div>
            <div class="mb-3">
              <input type="password" id="newPassword" placeholder="Mật khẩu mới" class="form-control"
                v-model="passwordData.newPassword" required />
            </div>
            <div class="mb-3">
              <input type="password" id="confirmPassword" placeholder="Xác nhận mật khẩu mới" class="form-control"
                v-model="passwordData.confirmPassword" required />
            </div>
            <button type="submit" class="btn btn-dark">Đặt lại mật khẩu</button>
          </form>
        </div>

        <!-- Địa chỉ nhận hàng -->
        <div v-show="activeTab === 'address'" class="card p-4 shadow-sm">
          <h3 class="h5 mb-4">ĐỊA CHỈ NHẬN HÀNG</h3>
          <div class="mb-4">
            <div v-if="addresses.length === 0" class="text-center p-4 border rounded">
              Bạn chưa có địa chỉ nào.
            </div>
            <div v-for="item in addresses" :key="item.id"
              class="border rounded p-3 mb-3 d-flex justify-content-between align-items-start"
              :class="{ 'bg-light': item.isDefault }">
              <div>
                <p class="fw-semibold mb-1">{{ item.name }}</p>
                <p class="mb-1 text-muted">{{ item.address }}</p>
                <p class="mb-1 text-muted">SĐT: {{ item.phone }}</p>
                <span v-if="item.isDefault" class="badge bg-success">Mặc định</span>
              </div>

              <div class="d-flex flex-column align-items-end">
                <a href="#" class="btn btn-sm btn-link">Sửa</a>
                <a href="#" class="btn btn-sm btn-link text-danger">Xoá</a>
                <a href="#" v-if="!item.isDefault" class="btn btn-sm btn-link text-primary">Đặt mặc định</a>
              </div>
            </div>
          </div>

          <!-- Thêm địa chỉ mới -->
          <div class="border-top pt-4">
            <h4 class="h6 mb-3">Thêm địa chỉ mới</h4>
            <form @submit.prevent="addNewAddress" class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Họ tên người nhận</label>
                <input type="text" class="form-control" v-model="newAddress.name" />
              </div>
              <div class="col-md-6">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="newAddress.phone" />
              </div>
              <div class="col-12">
                <label class="form-label">Địa chỉ chi tiết</label>
                <input type="text" class="form-control" v-model="newAddress.address" />
              </div>
              <div class="col-12">
                <button type="submit" class="btn btn-dark">Thêm địa chỉ</button>
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
        <div class="modal-header border-0">
          <h5 class="modal-title" id="trackingModalLabel">
            Theo dõi đơn hàng #DH123456
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="tracking-progress-bar mb-5">
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-check"></i>
              </div>
              <div class="tracking-label completed">Đã đặt</div>
              <div class="tracking-connector completed"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-box"></i>
              </div>
              <div class="tracking-label completed">Chuẩn bị</div>
              <div class="tracking-connector completed"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon completed">
                <i class="fas fa-shipping-fast"></i>
              </div>
              <div class="tracking-label completed">Đang giao</div>
              <div class="tracking-connector"></div>
            </div>
            <div class="tracking-step">
              <div class="tracking-icon">
                <i class="fas fa-check-double"></i>
              </div>
              <div class="tracking-label">Hoàn tất</div>
            </div>
          </div>
          <p><strong>Ngày đặt:</strong> 12/06/2025</p>
          <p>
            <strong>Trạng thái:</strong><span class="text-success fw-bold"> Đang giao</span>
          </p>
          <p><strong>Người nhận:</strong> Đào Gia Huy</p>
          <p><strong>Địa chỉ:</strong> 867 Trường Chinh, Kiến An, Hải Phòng</p>
        </div>
      </div>
    </div>
  </div>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
    integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6A=="
    crossorigin="anonymous" referrerpolicy="no-referrer" />
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
  transition: background-color 0.2s ease-in-out;
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
  color: #58151c !important;
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
