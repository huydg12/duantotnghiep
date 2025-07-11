<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

const accounts = ref([
    {
        id: 1,
        username: 'customer01',
        password: '',
        email: 'kh01@example.com',
        numberPhone: '0912345678',
        createdDate: '2025-07-10T08:00:00Z',
        isActive: true,
        roleId: 1
    },
]);

const form = ref({
    id: null,
    username: '',
    password: '',
    email: '',
    numberPhone: '',
    createdDate: new Date().toISOString().substring(0, 10),
    isActive: true,
    roleId: 1
});

const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() => Math.ceil(accounts.value.length / pageSize));

const paginatedAccounts = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return accounts.value.slice(start, start + pageSize);
});

function resetForm() {
    form.value = {
        id: null,
        username: '',
        password: '',
        email: '',
        numberPhone: '',
        createdDate: new Date().toISOString().substring(0, 10),
        isActive: true,
        roleId: 1
    };
    isEditing.value = false;
}

function goToPage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
}

function formatDateTime(datetimeStr) {
    const date = new Date(datetimeStr);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}
</script>
<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Tài Khoản</h2>

        <!-- Form -->
        <form @submit.prevent="" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Tên đăng nhập</label>
                <input v-model="form.username" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Mật khẩu</label>
                <input v-model="form.password" type="password" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input v-model="form.email" type="email" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input v-model="form.numberPhone" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label d-block">Trạng thái</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="active" :value="true" v-model="form.isActive" />
                    <label class="form-check-label" for="active">Hoạt động</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="inactive" :value="false" v-model="form.isActive" />
                    <label class="form-check-label" for="inactive">Không hoạt động</label>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày tạo</label>
                <input type="date" v-model="form.createdDate" class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Vai trò</label>
                <select v-model="form.roleId" class="form-select">
                    <option value="1">Khách hàng</option>
                    <option value="2">Nhân viên</option>
                    <option value="3">Admin</option>
                </select>
            </div>
            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    {{ isEditing ? "Cập nhật" : "Thêm" }}
                </button>
                <button type="button" class="btn btn-secondary" @click="resetForm">
                    Làm mới
                </button>
            </div>
        </form>

        <!-- Table -->
        <div class="table-container table-responsive">
            <table class="table table-bordered table-hover align-middle">
                <thead class="table-secondary text-center">
                    <tr>
                        <th style="width: 60px">ID</th>
                        <th style="width: 180px">Tài khoản</th>
                        <th style="width: 180px">Mật khẩu</th>
                        <th style="width: 180px">Email</th>
                        <th style="width: 180px">Số điện thoại</th>
                        <th style="width: 180px">Ngày tạo</th>
                        <th style="width: 180px">Trạng thái</th>
                        <th style="width: 180px">Vai trò</th>
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="accounts in paginatedAccounts" :key="accounts.id">
                        <td class="text-center">{{ accounts.id }}</td>
                        <td class="text-center">{{ accounts.username }}</td>
                        <td class="text-center">{{ accounts.password }}</td>
                        <td class="text-center">{{ accounts.email }}</td>
                        <td class="text-center">{{ accounts.numberPhone }}</td>
                        <td class="text-center">{{ formatDateTime(accounts.createdDate) }}</td>
                        <td class="text-center">
                            <span v-if="accounts.roleId === 1">Admin</span>
                            <span v-else-if="accounts.roleId === 2">Khách hàng</span>
                            <span v-else>Nhân viên</span>
                        </td>
                        <td class="text-center">
                            <span v-if="accounts.isActive === true" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>

                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editBrand(accounts)">
                                Sửa
                            </button>
                            <button class="btn btn-danger btn-sm" @click="deleteBrand(accounts.id)">
                                Xoá
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Phân Trang -->
        <nav>
            <ul class="pagination justify-content-center mt-4 custom-pagination">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <button class="page-link" @click="goToPage(currentPage - 1)">
                        «
                    </button>
                </li>

                <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === currentPage }">
                    <button class="page-link" @click="goToPage(page)">{{ page }}</button>
                </li>

                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="goToPage(currentPage + 1)">
                        »
                    </button>
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

.custom-pagination .page-link {
    transition: all 0.2s ease-in-out;
    cursor: pointer;
    color: #007bff;
    border-radius: 6px;
    margin: 0 10px;
}

.custom-pagination .page-link:hover {
    background-color: #e2e6ea;
    color: #0056b3;
}

.custom-pagination .page-item.active .page-link {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
    font-weight: bold;
}
</style>
