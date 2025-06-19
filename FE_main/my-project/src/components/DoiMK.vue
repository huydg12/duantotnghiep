<script setup>
    function changePassword() {
      const newPass = document.getElementById("newPassword").value;
      const confirmPass = document.getElementById("confirmPassword").value;

      if (!newPass || !confirmPass) {
        alert("Vui lòng nhập đầy đủ mật khẩu.");
      } else if (newPass !== confirmPass) {
        alert("Mật khẩu xác nhận không khớp.");
      } else {
        alert("Đổi mật khẩu thành công!");
        window.location.href = "dangnhap.html";
      }
      return false;
    }
</script>
<template>
  <div class="container">
    <div class="bg-white shadow rounded p-4 mt-5 mx-auto" style="max-width: 480px;">
      <h2 class="text-center fs-4 fw-bold mb-4">Đổi Mật Khẩu</h2>

      <form onsubmit="return changePassword()">
        <div class="mb-3">
          <label for="newPassword" class="form-label fw-semibold">Mật khẩu mới:</label>
          <input
            type="password"
            id="newPassword"
            required
            class="form-control"
            placeholder="Nhập mật khẩu mới"
          />
        </div>
        <div class="mb-3">
          <label for="confirmPassword" class="form-label fw-semibold">Xác nhận mật khẩu:</label>
          <input
            type="password"
            id="confirmPassword"
            required
            class="form-control"
            placeholder="Xác nhận mật khẩu mới"
          />
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


</style> 