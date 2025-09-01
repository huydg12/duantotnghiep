<script setup>
import { computed, ref, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const originalInfo = ref(null);
let employeeId = null;

const userInfo = reactive({
    fullName: "",
    gender: "",
    email: "",
    phone: "",
    birthDate: ""
});
// Lấy thông tin nhân viên
const userJson = localStorage.getItem("user");
if (userJson) {
    try {
        const user = JSON.parse(userJson);
        employeeId = user.employeeId;
        console.log("✅ Employee ID:", employeeId);

    } catch (error) {
        console.error("❌ Lỗi khi parse userJson:", error);
    }
}
// Hàm fetch dữ liệu từ API
const fetchUserInfo = async () => {
    try {
        const { data } = await axios.get(`http://localhost:8080/employee/showInformationEmployee/${employeeId}`);

        const normalized = {
            fullName: data.fullName ?? "",
            gender: data.gender ?? "",
            email: data.email ?? "",
            phone: data.numberPhone ?? "",
            birthDate: data.birthOfDate ? data.birthOfDate.slice(0, 10) : ""
        };

        Object.assign(userInfo, normalized);      // đổ vào form
        originalInfo.value = { ...normalized };   // lưu bản gốc để so sánh
    } catch (error) {
        console.error("Lỗi khi fetch thông tin nhân viên:", error);
    }
};

// 👉 Hàm UPDATE (có check trống + check không đổi)
const updateUserInfo = async () => {
    try {
        if (!employeeId) {
            alert("Không tìm thấy Employee ID.");
            return;
        }

        const nothingChanged = () => {
            if (!originalInfo.value) return false; // lần đầu chưa có bản gốc
            const a = userInfo;
            const b = originalInfo.value;
            return (
                a.fullName === b.fullName &&
                a.gender === b.gender &&
                a.email === b.email &&
                a.phone === b.phone &&
                a.birthDate === b.birthDate
            );
        };

        // 2) Không có thay đổi thì báo
        if (nothingChanged()) {
            alert("Không có thay đổi nào để cập nhật.");
            return;
        }

        // 3) Map sang DTO backend
        const payload = {
            fullName: userInfo.fullName.trim(),
            gender: toBool(userInfo.gender),
            email: userInfo.email.trim(),
            numberPhone: userInfo.phone.trim(),
            birthOfDate: userInfo.birthDate, // "YYYY-MM-DD"
        };

        await axios.put(`http://localhost:8080/employee/updateInformation/${employeeId}`, payload);
        alert("Cập nhật thành công!");

        // Cập nhật lại bản gốc để lần sau so sánh
        originalInfo.value = { ...normalizeForCompare(userInfo) };

        await fetchUserInfo(); // (tuỳ chọn) load lại từ server
    } catch (error) {
        console.error("Lỗi cập nhật thông tin:", error?.response?.data || error);
        alert("Cập nhật thất bại.");
    }
};

onMounted(() => {
    fetchUserInfo();
});
</script>

<template>
    <div class="card p-4 shadow-sm">
        <h3 class="h5 mb-4">THÔNG TIN TÀI KHOẢN</h3>
        <form @submit.prevent="updateUserInfo">
            <div class="row g-3">
                <div class="col-md-6">
                    <label for="fullName" class="form-label">Họ tên</label>
                    <input type="text" id="fullName" class="form-control" v-model="userInfo.fullName" required />
                </div>
                <div class="col-md-6">
                    <label class="form-label d-block">Giới tính</label>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" value="true" v-model="userInfo.gender" required />
                        <label class="form-check-label">Nam</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" value="false" v-model="userInfo.gender" required />
                        <label class="form-check-label">Nữ</label>
                    </div>
                </div>
                <div class="col-md-6">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" id="email" class="form-control" v-model="userInfo.email" required />
                </div>
                <div class="col-md-6">
                    <label for="phone" class="form-label">Số điện thoại</label>
                    <input type="text" id="phone" class="form-control" v-model="userInfo.phone" required
                        pattern="^(0[0-9]{9})$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0" />
                </div>
                <div class="col-md-6">
                    <label for="birthDate" class="form-label">Ngày sinh</label>
                    <input type="date" id="birthDate" class="form-control" v-model="userInfo.birthDate" required />
                </div>
            </div>
            <button type="submit" class="btn btn-dark mt-4">Cập nhật</button>
        </form>
    </div>
</template>



<style scoped></style>