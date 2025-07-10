<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

const customers = ref([
    {
        id: 1,
        accountId: null,
        fullName: "Nguyễn Văn A",
        gender: "Nam",
        email: "a@gmail.com",
        numberPhone: "0123456789",
        birthOfDate: "1998-01-01",
        status: 1,
        createdDate: new Date().toISOString(),
    },
]);

const form = ref({
    id: null,
    fullName: '',
    gender: '',
    email: '',
    numberPhone: '',
    birthOfDate: '',
    status: 1,
    createdDate: new Date().toISOString()
});

const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() => Math.ceil(customers.value.length / pageSize));

const paginatedCustomers = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return customers.value.slice(start, start + pageSize);
});

function resetForm() {
    form.value = {
        id: null,
        fullName: '',
        gender: '',
        email: '',
        numberPhone: '',
        birthOfDate: '',
        status: 1,
        createdDate: new Date().toISOString()
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
        <h2 class="text-center mb-4 fw-bold">Quản Lý Khách Hàng</h2>

        <!-- Form -->
        <form @submit.prevent="saveBrand" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Họ tên</label>
                <input v-model="form.name" required class="form-control" />
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
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input v-model="form.name" required class="form-control" />
            </div>
            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input v-model="form.name" required class="form-control" />
            </div>
            <div class="mb-3">
                <label class="form-label">Ngày sinh</label>
                <input type="date" v-model="form.name" required class="form-control" />
            </div>
            <div class="mb-3">
                <label class="form-label d-block">Trạng thái</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="status-active" value="Hoạt động"
                        v-model="form.gender" />
                    <label class="form-check-label" for="status-active">Hoạt động</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="status-inactive" value="Không hoạt động"
                        v-model="form.gender" />
                    <label class="form-check-label" for="status-inactive">Không hoạt động</label>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Ngày tạo</label>
                <input type="date" v-model="form.name" required class="form-control" />
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
                        <th style="width: 180px">Ngày tạo</th>
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="customers in paginatedCustomers" :key="customers.id">
                        <td class="text-center">{{ customers.id }}</td>
                        <td class="text-center">{{ customers.accountId }}</td>
                        <td class="text-center">{{ customers.fullName }}</td>
                        <td class="text-center">{{ customers.gender }}</td>
                        <td class="text-center">{{ customers.email }}</td>
                        <td class="text-center">{{ customers.numberPhone }}</td>
                        <td class="text-center">{{ customers.birthOfDate }}</td>
                        <td class="text-center">
                            <span v-if="customers.status === 1" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">{{ formatDateTime(customers.createdDate) }}</td>
                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editBrand(customers)">
                                Sửa
                            </button>
                            <button class="btn btn-danger btn-sm" @click="deleteBrand(customers.id)">
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
