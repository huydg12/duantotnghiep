<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

const accounts = ref([]);

const fetchAccount = async () => {
    try {
        const response = await axios.get('http://localhost:8080/account/show')
        accounts.value = response.data;
        console.log(accounts.value)
    } catch (error) {
        console.log("Lỗi hiển thị", error)
    }
}

onMounted(() => {
    fetchAccount();
})

async function saveAccount() {
    try {
        if (isEditing.value) {
            await axios.put(`http://localhost:8080/account/update/${form.value.id}`, form.value)
        } else {

            await axios.post('http://localhost:8080/account/add', form.value);
        }
        await fetchAccount();
        resetForm();
    } catch (error) {
        console.log('Lỗi save account', error);
    }
}

function editAccount(account) {
    form.value = { ...account };
    isEditing.value = true;
}

async function deleteAccount(id) {
    try {
        if (confirm('Bạn có chắc chắn muốn xóa tài khoản này không ?')) {
            await axios.delete(`http://localhost:8080/account/delete/${id}`)
            await fetchAccount()
        }
    } catch (error) {
        console.log('Lỗi delete', error)
    }
}

const form = ref({
    id: null,
    username: '',
    password: '',
    email: '',
    numberPhone: '',
    createdDate: getVietnamDateTimeLocalFormat(),
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
        isActive: true,
        createdDate: getVietnamDateTimeLocalFormat(),
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

function getVietnamDateTimeLocalFormat() {
    const now = new Date();
    const vietnamOffset = 7 * 60;
    const localOffset = now.getTimezoneOffset();
    const totalOffset = vietnamOffset + localOffset;
    const vietnamTime = new Date(now.getTime() + totalOffset * 60000);

    const year = vietnamTime.getFullYear();
    const month = String(vietnamTime.getMonth() + 1).padStart(2, '0');
    const day = String(vietnamTime.getDate()).padStart(2, '0');
    const hours = String(vietnamTime.getHours()).padStart(2, '0');
    const minutes = String(vietnamTime.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}
</script>
<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Tài Khoản</h2>

        <!-- Form -->
        <form @submit.prevent="saveAccount" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Tên đăng nhập</label>
                <input v-model="form.username" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Mật khẩu</label>
                <input v-model="form.password" type="password" required class="form-control" />
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
                <label class="form-label d-block">Vai trò</label>

                <!-- Khi thêm mới: chỉ hiện Admin và Nhân viên -->
                <template v-if="!isEditing">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="admin" value="1" v-model="form.roleId">
                        <label class="form-check-label" for="admin">Admin</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="employee" value="3" v-model="form.roleId">
                        <label class="form-check-label" for="employee">Nhân viên</label>
                    </div>
                </template>

                <!-- Khi chỉnh sửa: hiện đủ 3 vai trò nhưng disabled -->
                <template v-else>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="admin" value="1" v-model="form.roleId"
                            disabled>
                        <label class="form-check-label" for="admin">Admin</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="employee" value="3" v-model="form.roleId"
                            disabled>
                        <label class="form-check-label" for="employee">Nhân viên</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="customer" value="2" v-model="form.roleId"
                            disabled>
                        <label class="form-check-label" for="customer">Khách hàng</label>
                    </div>
                </template>
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
                            <span v-if="account.roleId === 1">Admin</span>
                            <span v-else-if="account.roleId === 2">Khách hàng</span>
                            <span v-else>Nhân viên</span>
                        </td>
                        <td class="text-center">
                            <span v-if="account.isActive" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>

                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editAccount(account)">
                                Sửa
                            </button>
                            <button class="btn btn-danger btn-sm" @click="deleteAccount(account.id)">
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
