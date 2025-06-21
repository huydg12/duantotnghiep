<script>
</script>

<template>
    <div class="bg-light">
  <div class="container">
  <div class="bg-white mx-auto mt-5 p-4 rounded shadow" style="max-width: 480px;">
    <!-- Tabs -->
    <ul class="nav nav-tabs justify-content-center mb-4" id="formTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active" id="login-tab" data-bs-toggle="tab" data-bs-target="#form-login" type="button" role="tab">Đăng nhập</button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="register-tab" data-bs-toggle="tab" data-bs-target="#form-register" type="button" role="tab">Đăng ký</button>
      </li>
    </ul>

    <!-- Tab Content -->
    <div class="tab-content" id="formTabsContent">
      <!-- Đăng nhập -->
      <div class="tab-pane fade show active" id="form-login" role="tabpanel">
        <form>
          <div class="mb-3">
            <label class="form-label fw-semibold">EMAIL*</label>
            <input type="email" class="form-control" placeholder="Nhập Địa chỉ Email">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">MẬT KHẨU*</label>
            <input type="password" class="form-control" placeholder="Nhập Mật khẩu">
          </div>
          <div class="mb-3 text-end">
            <router-link to="/resetPass" class="text-decoration-none text-primary">Quên mật khẩu?</router-link>
          </div>
          <button type="submit" class="btn btn-dark w-100 fw-semibold">ĐĂNG NHẬP</button>
        </form>
      </div>

      <!-- Đăng ký -->
      <div class="tab-pane fade" id="form-register" role="tabpanel">
        <form>
          <div class="mb-3">
            <label class="form-label fw-semibold">HỌ*</label>
            <input type="text" class="form-control" placeholder="Nhập Họ">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">TÊN*</label>
            <input type="text" class="form-control" placeholder="Nhập Tên">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">SỐ ĐIỆN THOẠI*</label>
            <input type="tel" class="form-control" placeholder="Nhập Số điện thoại">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">EMAIL*</label>
            <input type="email" class="form-control" placeholder="Nhập Địa chỉ Email">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">MẬT KHẨU*</label>
            <input type="password" class="form-control" placeholder="Nhập Mật khẩu">
          </div>
          <button type="submit" class="btn btn-dark w-100 fw-semibold">TẠO TÀI KHOẢN</button>
        </form>
      </div>
    </div>
  </div>
</div>

    </div>
  

</template>

<style scoped>


</style>
