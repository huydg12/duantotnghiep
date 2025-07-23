<script setup>
import { ref, reactive } from 'vue';
import  axios  from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

const form = reactive ({
    fullname: '',
    phone: '',
    email: '',
    username: '',
    password: '',
})

const handleRegister = async () => {
    try {
        const response = await axios.post("http://localhost:8080/auth/register", form)
        const customer = response.data;
        const customerId = customer.id;
        console.log("customerId: " + customerId)
        // Tạo giỏ hàng cho customer mới
        await axios.post("http://localhost:8080/cart/add", {
        customerId: customerId
        });
        alert("Đăng ký thành công") 
        router.push("/auth/login")
    } catch (error) {
        alert("Đăng ký không thành công")
    }
}

</script>

<template>
    <form @submit.prevent="handleRegister">
        <div class="mb-3">
            <label class="form-label fw-semibold">HỌ VÀ TÊN</label>
            <input v-model="form.fullname" type="text" class="form-control" placeholder="Nhập họ và tên" />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">SỐ ĐIỆN THOẠI</label>
            <input v-model="form.phone" type="tel" class="form-control" placeholder="Nhập số điện thoại" />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">EMAIL</label>
            <input v-model="form.email" type="email" class="form-control" placeholder="Nhập địa chỉ email" />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">TÀI KHOẢN</label>
            <input v-model="form.username" type="Text" class="form-control" placeholder="Nhập tài khoản" />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">MẬT KHẨU</label>
            <input v-model="form.password" type="password" class="form-control" placeholder="Nhập Mật khẩu" />
        </div>
        <div class="mb-3">
            <label class="form-label fw-semibold">XÁC NHẬN MẬT KHẨU</label>
            <input type="password" class="form-control" placeholder="Nhập lại xác nhận mật khẩu" />
        </div>
        <button type="submit" class="btn btn-dark w-100 fw-semibold">
            TẠO TÀI KHOẢN
        </button>
    </form>
</template>

<style scoped></style>
