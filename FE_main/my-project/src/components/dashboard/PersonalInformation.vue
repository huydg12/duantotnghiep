<script setup>
import { computed, ref, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useUserStore } from "@/stores/userStore";
const userStore = useUserStore();
const router = useRouter();
const originalInfo = ref(null);
let employeeId = null;
let accountId = null;
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
        accountId = user.id;
        console.log("✅ Employee ID:", employeeId);
        console.log("✅ Account ID:", accountId);

    } catch (error) {
        console.error("❌ Lỗi khi parse userJson:", error);
    }
}
const handleLogout = () => {
  // Xóa localStorage và store
  userStore.logout();
  userInfo.value = null;

  // Điều hướng bằng replace để không quay lại được
  router.replace("/auth/login").then(() => {
    // Reload để clear cache nội dung đã xem
    window.location.reload();
  });
};
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

        Object.assign(userInfo, normalized);
        originalInfo.value = { ...normalized };
    } catch (error) {
        console.error("Lỗi khi fetch thông tin nhân viên:", error);
    }
};

function toBool(v) {
    if (typeof v === "boolean") return v;
    if (typeof v === "number") return v !== 0;
    if (v == null) return false;

    const s = String(v).trim().toLowerCase();
    if (["true", "1", "yes", "y", "nam", "male", "m"].includes(s)) return true;
    if (["false", "0", "no", "n", "nu", "nữ", "female", "f"].includes(s)) return false;

    return s === "true";
}

function normalizeForCompare(src) {
    const str = (x) => (x == null ? "" : String(x).trim());
    return {
        fullName: str(src.fullName),
        gender: String(toBool(src.gender)),
        email: str(src.email),
        phone: str(src.phone),
        birthDate: str(src.birthDate).slice(0, 10),
    };
}

const updateUserInfo = async () => {
    try {
        if (!employeeId) {
            alert("Không tìm thấy Employee ID.");
            return;
        }

        const nothingChanged = () => {
            if (!originalInfo.value) return false;
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

        window.location.reload();
        await fetchUserInfo(); // (tuỳ chọn) load lại từ server
    } catch (error) {
        console.error("Lỗi cập nhật thông tin:", error?.response?.data || error);
        alert("Cập nhật thất bại.");
    }
};

// --- state hiển thị message bằng <span> ---
const formMsg = ref({ type: "", text: "" }); // type: success|error|warning
const setMsg = (type, text) => (formMsg.value = { type, text });

// --- validate ---
const passwordMismatch = computed(
    () =>
        passwordData.newPassword !== "" &&
        passwordData.confirmPassword !== "" &&
        passwordData.newPassword !== passwordData.confirmPassword
);
const tooShort = computed(
    () => passwordData.newPassword.length > 0 && passwordData.newPassword.length < 6
);
const canSubmit = computed(
    () =>
        !!passwordData.currentPassword &&
        !!passwordData.newPassword &&
        !!passwordData.confirmPassword &&
        !passwordMismatch.value &&
        !tooShort.value
);

const submitting = ref(false);

const passwordData = reactive({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
});


const changePassword = async () => {
    // chỉ HIỂN THỊ THÔNG BÁO bằng span, không bật alert/swal
    if (passwordMismatch.value) {
        setMsg("warning", "Xác nhận mật khẩu không khớp. Vui lòng nhập lại.");
        return;
    }
    if (tooShort.value) {
        setMsg("warning", "Mật khẩu mới tối thiểu 6 ký tự.");
        return;
    }
    if (!accountId) {
        setMsg("error", "Thiếu thông tin tài khoản. Vui lòng đăng nhập lại.");
        return;
    }

    try {
        submitting.value = true;
        const payload = {
            currentPassword: passwordData.currentPassword,
            newPassword: passwordData.newPassword,
            confirmPassword: passwordData.confirmPassword,
        };
        await axios.put(
            `http://localhost:8080/account/changePassword/${accountId}`,
            payload
        );
        setMsg("success", "Đổi mật khẩu thành công!");
        // reset form
        passwordData.currentPassword = "";
        passwordData.newPassword = "";
        passwordData.confirmPassword = "";
         handleLogout();
    } catch (error) {
        const msg =
            error?.response?.data?.message ||
            error?.response?.data ||
            "Cập nhật thất bại. Vui lòng thử lại.";
        setMsg("error", String(msg));
    } finally {
        submitting.value = false;
    }
};
onMounted(() => {
    fetchUserInfo();
});
</script>

<template>
    <section class="space-y-4">
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

    <div class="card p-4 shadow-sm">
        <h3 class="h5 mb-4">ĐỔI MẬT KHẨU</h3>
        <!-- dòng thông báo tổng -->

        <form @submit.prevent="changePassword">
            <div class="mb-3">
                <input type="password" id="oldPassword" placeholder="Mật khẩu hiện tại" class="form-control"
                    v-model="passwordData.currentPassword" required />
            </div>
            <div class="mb-3">
                <input type="password" id="newPassword" placeholder="Mật khẩu mới" class="form-control"
                    v-model="passwordData.newPassword" required />
                <span v-if="tooShort" class="text-danger small">Mật khẩu tối thiểu 6 ký tự.</span>
            </div>
            <div class="mb-3">
                <input type="password" id="confirmPassword" placeholder="Xác nhận mật khẩu mới" class="form-control"
                    v-model="passwordData.confirmPassword" required />
                <!-- span không khớp -->
                <span v-if="passwordMismatch" class="text-danger small">Xác nhận mật khẩu không khớp.</span>
            </div>
            <button type="submit" class="btn btn-dark" :disabled="!canSubmit || submitting">
                {{ submitting ? "Đang xử lý..." : "Đặt lại mật khẩu" }}
            </button>
            <div class="mb-2" v-if="formMsg.text">
                <span :class="{
                    'text-success': formMsg.type === 'success',
                    'text-danger': formMsg.type === 'error',
                    'text-warning': formMsg.type === 'warning'
                }" class="fw-semibold">
                    {{ formMsg.text }}
                </span>
            </div>
        </form>
    </div>
    </section>
</template>

<style scoped></style>