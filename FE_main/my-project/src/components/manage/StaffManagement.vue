<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

const employees = ref([
    {
        id: 1,
        accountId: 1001,
        fullName: 'Nguyễn Văn B',
        gender: 'Nam',
        email: 'nvb@example.com',
        numberPhone: '0912345678',
        birthOfDate: '1990-06-15',
        isActive: true,
        createdBy: 'admin',
        createdDate: '2025-07-10T01:23:13.613Z',
    }
]);

const form = ref({
    id: null,
    accountId: null,
    fullName: '',
    gender: '',
    email: '',
    numberPhone: '',
    birthOfDate: '',
    isActive: true,
    createdBy: '',
    createdDate: '',
});
const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() => Math.ceil(employees.value.length / pageSize));

const paginatedEmployees = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return employees.value.slice(start, start + pageSize);
});

function resetForm() {
    form.value = {
        id: null,
        accountId: null,
        fullName: '',
        gender: '',
        email: '',
        numberPhone: '',
        birthOfDate: '',
        isActive: true,
        createdBy: '',
        createdDate: '',
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
        <h2 class="text-center mb-4 fw-bold">Quản Lý Nhân Viên</h2>

        <!-- Form -->
        <form @submit.prevent="saveBrand" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Họ tên</label>
                <input v-model="form.fullName" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label d-block">Giới tính</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="gender-male" value="Nam" v-model="form.gender" />
                    <label class="form-check-label" for="gender-male">Nam</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="gender-female" value="Nữ" v-model="form.gender" />
                    <label class="form-check-label" for="gender-female">Nữ</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="gender-other" value="Khác" v-model="form.gender" />
                    <label class="form-check-label" for="gender-other">Khác</label>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" v-model="form.email" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input v-model="form.numberPhone" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày sinh</label>
                <input type="date" v-model="form.birthOfDate" required class="form-control" />
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
                <label class="form-label">Người tạo</label>
                <input v-model="form.createdBy" class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày tạo</label>
                <input type="date" v-model="form.createdDate" class="form-control" />
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
                        <th style="width: 180px">Account ID</th>
                        <th style="width: 180px">Họ tên</th>
                        <th style="width: 180px">Giới tính</th>
                        <th style="width: 180px">Email</th>
                        <th style="width: 180px">SĐT</th>
                        <th style="width: 180px">Ngày sinh</th>
                        <th style="width: 180px">Trạng thái</th>
                        <th style="width: 180px">Người tạo</th>
                        <th style="width: 180px">Ngày tạo</th>
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="employees in paginatedEmployees" :key="employees.id">
                        <td class="text-center">{{ employees.id }}</td>
                        <td class="text-center">{{ employees.accountId }}</td>
                        <td class="text-center">{{ employees.fullName }}</td>
                        <td class="text-center">{{ employees.gender }}</td>
                        <td class="text-center">{{ employees.email }}</td>
                        <td class="text-center">{{ employees.numberPhone }}</td>
                        <td class="text-center">{{ employees.birthOfDate }}</td>
                        <td class="text-center">
                            <span v-if="employees.isActive === true" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">{{ employees.createdBy }}</td>
                        <td class="text-center">{{ formatDateTime(employees.createdDate) }}</td>
                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editBrand(employees)">
                                Sửa
                            </button>
                            <button class="btn btn-danger btn-sm" @click="deleteBrand(employees.id)">
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
