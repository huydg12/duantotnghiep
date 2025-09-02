<script setup>
import axios from "axios";
import { ref, computed, onMounted, watch } from "vue";

// ================== DATA ==================
const accounts = ref([]);

// ================== FETCH ==================
const fetchAccount = async () => {
    try {
        const { data } = await axios.get("http://localhost:8080/account/show");
        accounts.value = data;
        console.log("accounts", accounts.value);
    } catch (error) {
        console.log("Lỗi hiển thị", error);
    }
};

// ================== ACTIONS ==================
async function changeStatus(id) {
    if (!confirm("Bạn có chắc muốn chuyển trạng thái tài khoản này?")) return;
    try {
        await axios.put(`http://localhost:8080/account/updateStatus/${id}`, { id });
        alert("Đã chuyển trạng thái tài khoản");
        await fetchAccount();
    } catch (error) {
        console.error(
            "Lỗi chuyển trạng thái tài khoản:",
            error.response ? error.response.data : error.message
        );
        alert("Không thể chuyển trạng thái tài khoản");
    }
}

async function resetPassword(id) {
    if (!confirm("Bạn có chắc muốn cập nhật mật khẩu tài khoản này?")) return;
    try {
        await axios.put(`http://localhost:8080/account/updatePassword/${id}`, { id });
        alert("Cập nhật mật khẩu thành công");
        await fetchAccount();
    } catch (error) {
        console.error(
            "Lỗi cập nhật mật khẩu:",
            error.response ? error.response.data : error.message
        );
        alert("Không thể cập nhật mật khẩu");
    }
}

// ================== FORMATTERS ==================
function formatDateTime(datetimeStr) {
    if (!datetimeStr) return "";
    const d = new Date(datetimeStr);
    if (isNaN(d.getTime())) return "";
    const dd = String(d.getDate()).padStart(2, "0");
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const yyyy = d.getFullYear();
    return `${dd}-${mm}-${yyyy}`;
}

const strip = (s) =>
    String(s ?? "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase();

const roleLabel = (r) => (r === 1 ? "admin" : r === 2 ? "khach hang" : "nhan vien");

// ================== FILTER STATE ==================
const searchQuery = ref(""); // chỉ tìm theo username
const filters = ref({
    roleId: "all",   // all | 1 | 2 | 3
    status: "all",   // all | active | inactive
});

function clearAll() {
    searchQuery.value = "";
    filters.value = { roleId: "all", status: "all" };
}

// ================== FILTERED + PAGINATION ==================
const filteredAccounts = computed(() => {
    const q = strip(searchQuery.value);
    const role = String(filters.value.roleId);
    const st = filters.value.status;

    return accounts.value.filter((acc) => {
        // 🔎 Chỉ tìm theo username
        const okText = !q || strip(acc?.username).includes(q);

        // lọc theo vai trò
        const okRole = role === "all" || String(acc?.roleId ?? "") === role;

        // lọc theo trạng thái
        const okStatus =
            st === "all" ||
            (st === "active" && acc?.isActive === true) ||
            (st === "inactive" && acc?.isActive === false);

        return okText && okRole && okStatus;
    });
});

const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredAccounts.value.length / pageSize))
);

const paginatedAccounts = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return filteredAccounts.value.slice(start, start + pageSize);
});

// 👉 Hàm phân trang (bị thiếu nên trước đó không đổi trang được)
function goToPage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
}

// Reset về trang 1 khi thay đổi từ khóa / filter
watch([searchQuery, filters], () => {
    currentPage.value = 1;
}, { deep: true });

// ================== MOUNT ==================
onMounted(() => {
    fetchAccount();
});
</script>

<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Tài Khoản</h2>

        <!-- Tìm kiếm & Bộ lọc -->
        <div class="row g-2 align-items-end mb-3">
            <div class="col-md-5">
                <label class="form-label small">Tìm theo tài khoản</label>
                <input v-model="searchQuery" type="text" class="form-control" placeholder="Nhập username..." />
            </div>

            <div class="col-md-3">
                <label class="form-label small">Vai trò</label>
                <select v-model="filters.roleId" class="form-select">
                    <option value="all">Tất cả</option>
                    <option value="1">Admin</option>
                    <option value="2">Khách hàng</option>
                    <option value="3">Nhân viên</option>
                </select>
            </div>

            <div class="col-md-3">
                <label class="form-label small">Trạng thái</label>
                <select v-model="filters.status" class="form-select">
                    <option value="all">Tất cả</option>
                    <option value="active">Hoạt động</option>
                    <option value="inactive">Không hoạt động</option>
                </select>
            </div>

            <div class="col-md-1 d-grid mt-2">
                <button class="btn btn-outline-secondary" @click="clearAll">Xóa</button>
            </div>

            <div class="col-12 mt-1">
                <span class="text-muted small">
                    Đang hiển thị {{ paginatedAccounts.length }} / {{ filteredAccounts.length }} kết quả
                </span>
            </div>
        </div>

        <!-- Bảng -->
        <div class="table-container table-responsive">
            <table class="table table-bordered table-hover align-middle">
                <thead class="table-secondary text-center">
                    <tr>
                        <th style="width: 60px">ID</th>
                        <th style="width: 180px">Tài khoản</th>
                        <th style="width: 180px">Mật khẩu</th>
                        <th style="width: 180px">Ngày tạo</th>
                        <th style="width: 180px">Trạng thái</th>
                        <th style="width: 180px">Vai trò</th>
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="account in paginatedAccounts" :key="account.id">
                        <td class="text-center">{{ account.id }}</td>
                        <td class="text-center">{{ account.username }}</td>
                        <td class="text-center">{{ account.password }}</td>
                        <td class="text-center">{{ formatDateTime(account.createdDate) }}</td>
                        <td class="text-center">
                            <span v-if="account.isActive" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">
                            <span v-if="account.roleId === 1">Admin</span>
                            <span v-else-if="account.roleId === 2">Khách hàng</span>
                            <span v-else>Nhân viên</span>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-success btn-sm" @click="resetPassword(account.id)">
                                Làm mới mật khẩu
                            </button>
                            <button class="btn btn-danger btn-sm" @click="changeStatus(account.id)">
                                Chuyển trạng Thái
                            </button>
                        </td>
                    </tr>
                    <tr v-if="!paginatedAccounts.length">
                        <td colspan="7" class="text-center text-muted">Không có dữ liệu phù hợp</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Phân Trang -->
        <nav>
            <ul class="pagination justify-content-center mt-4 custom-pagination">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <button class="page-link" @click="goToPage(currentPage - 1)">«</button>
                </li>
                <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === currentPage }">
                    <button class="page-link" @click="goToPage(page)">{{ page }}</button>
                </li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="goToPage(currentPage + 1)">»</button>
                </li>
            </ul>
        </nav>
    </div>
</template>

<style scoped>
.text-wrap {
    white-space: normal;
    word-break: break-word;
}

.table-container {
    min-height: 300px;
}

.pagination .active .page-link {
    background-color: #198754;
    border-color: #198754;
    color: #fff;
}
</style>
