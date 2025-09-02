<script setup>
import axios from "axios";
import { ref, computed, onMounted, watch } from "vue";

const employees = ref([]);
const originalEmployee = ref(null);

const filters = ref({
    q: "",
    accountId: "",
    status: "all",
});


const norm = (s) => String(s ?? "").toLowerCase().trim();


const toComparable = (x) => ({
    fullName: (x?.fullName ?? "").trim(),
    gender: String(x?.gender ?? ""),
    email: (x?.email ?? "").trim().toLowerCase(),
    numberPhone: String(x?.numberPhone ?? "").replace(/\s+/g, ""),
    birthOfDate: String(x?.birthOfDate ?? "").slice(0, 10),
});

const isSame = (a, b) => {
    if (!a || !b) return false;
    return Object.keys(a).every((k) => a[k] === b[k]);
};

const fetchEmployee = async () => {
    try {
        const { data } = await axios.get("http://localhost:8080/employee/show");
        employees.value = data;
        console.log(employees.value);
    } catch (error) {
        console.log("Lỗi hiển thị", error);
    }
};


const filteredEmployees = computed(() => {
    const q = norm(filters.value.q);
    const acc = String(filters.value.accountId ?? "").trim();
    const st = filters.value.status;

    return employees.value.filter((e) => {
        const text = [e.fullName, e.email, e.numberPhone]
            .map(norm)
            .join(" ");
        const okText = q === "" || text.includes(q);

        const okAcc = acc === "" || String(e.accountId ?? "") === acc;

        const okStatus =
            st === "all" ||
            (st === "active" && e.active === true) ||
            (st === "inactive" && e.active === false);

        return okText && okAcc && okStatus;
    });
});

const currentPage = ref(1);
watch(filters, () => {
    currentPage.value = 1;
}, { deep: true });

async function saveEmployee() {
    try {
        if (isEditing.value) {
            const before = originalEmployee.value;
            const now = toComparable(form.value);
            if (isSame(before, now)) {
                alert("Không có thay đổi nào để lưu.");
                return;
            }
            await axios.put(
                `http://localhost:8080/employee/update/${form.value.id}`,
                form.value
            );
        } else {
            await axios.post("http://localhost:8080/employee/add", form.value);
        }
        await fetchEmployee();
        resetForm();
    } catch (error) {
        console.log("Lỗi thêm nhân viên", error);
    }
}

function editEmployee(employee) {
    form.value = { ...employee };
    isEditing.value = true;
    originalEmployee.value = toComparable(form.value);
}

async function changeStatus(id) {
    if (!confirm("Bạn có chắc muốn chuyển trạng thái nhân viên này?")) return;

    const updateEmployee = { id };
    try {
        await axios.put(
            `http://localhost:8080/employee/updateStatus/${id}`,
            updateEmployee
        );
        alert("Đã chuyển trạng thái nhân viên");
        await fetchEmployee();
    } catch (error) {
        console.error(
            "Lỗi chuyển trạng thái nhân viên:",
            error.response ? error.response.data : error.message
        );
        alert("Không thể chuyển trạng thái nhân viên");
    }
}

function getVietnamDateTimeLocalFormat() {
    const now = new Date();
    const vietnamOffset = 7 * 60;
    const localOffset = now.getTimezoneOffset();
    const totalOffset = vietnamOffset + localOffset;
    const vietnamTime = new Date(now.getTime() + totalOffset * 60000);

    const year = vietnamTime.getFullYear();
    const month = String(vietnamTime.getMonth() + 1).padStart(2, "0");
    const day = String(vietnamTime.getDate()).padStart(2, "0");
    const hours = String(vietnamTime.getHours()).padStart(2, "0");
    const minutes = String(vietnamTime.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function formatDateTime(datetimeStr) {
    const date = new Date(datetimeStr);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}

const form = ref({
    id: null,
    accountId: null,
    username: "",
    password: "",
    roleId: null,
    fullName: "",
    gender: true,
    email: "",
    numberPhone: "",
    birthOfDate: "",
    active: true,
    createdBy: "admin",
    createdDate: getVietnamDateTimeLocalFormat(),
});

const isEditing = ref(false);

const pageSize = 5;
const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredEmployees.value.length / pageSize))
);

const paginatedEmployees = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return filteredEmployees.value.slice(start, start + pageSize);
});

function resetForm() {
    form.value = {
        id: null,
        accountId: null,
        username: "",
        password: "",
        roleId: null,
        fullName: "",
        gender: true,
        email: "",
        numberPhone: "",
        birthOfDate: "",
        active: true,
        createdBy: "admin",
        createdDate: "",
    };
    isEditing.value = false;
}

function clearFilters() {
    filters.value = { q: "", accountId: "", status: "all" };
}

function goToPage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
}

onMounted(() => {
    fetchEmployee();
});
</script>

<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Nhân Viên</h2>

        <!-- Form -->
        <form @submit.prevent="saveEmployee" class="border p-4 rounded bg-light mb-4">
            <template v-if="!isEditing">
                <div class="mb-3">
                    <label class="form-label">Tài khoản</label>
                    <input v-model="form.username" required class="form-control" />
                </div>

                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <input type="password" v-model="form.password" required class="form-control" />
                </div>

                <div class="mb-3">
                    <label class="form-label d-block">Vai trò</label>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="admin" name="role" :value="1"
                            v-model.number="form.roleId" required />
                        <label class="form-check-label" for="admin">Admin</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" id="employee" name="role" :value="2"
                            v-model.number="form.roleId" required />
                        <label class="form-check-label" for="employee">Nhân viên</label>
                    </div>
                    <small class="text-muted">roleId: {{ form.roleId }}</small>
                </div>
            </template>

            <div class="mb-3">
                <label class="form-label">Họ tên</label>
                <input v-model="form.fullName" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label d-block">Giới tính</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="gender-male" name="gender" :value="true"
                        v-model="form.gender" />
                    <label class="form-check-label" for="gender-male">Nam</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" id="gender-female" name="gender" :value="false"
                        v-model="form.gender" />
                    <label class="form-check-label" for="gender-female">Nữ</label>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" v-model="form.email" required class="form-control" />
            </div>

            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input v-model="form.numberPhone" required class="form-control" pattern="^(0[0-9]{9})$"
                    title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0" />
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày sinh</label>
                <input type="date" v-model="form.birthOfDate" required class="form-control" />
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

        <!-- Filters -->
        <div class="row g-2 align-items-end mb-3">
            <div class="col-md-4">
                <label class="form-label small">Tìm nhanh</label>
                <input v-model="filters.q" class="form-control" placeholder="Tìm theo tên, email, SĐT..." />
            </div>

            <div class="col-md-3">
                <label class="form-label small">Account ID</label>
                <input v-model.trim="filters.accountId" class="form-control" placeholder="VD: 101" />
            </div>

            <div class="col-md-3">
                <label class="form-label small">Trạng thái</label>
                <select v-model="filters.status" class="form-select">
                    <option value="all">Tất cả</option>
                    <option value="active">Hoạt động</option>
                    <option value="inactive">Không hoạt động</option>
                </select>
            </div>

            <div class="col-md-2">
                <button type="button" class="btn btn-outline-secondary w-100" @click="clearFilters">
                    Xoá lọc
                </button>
            </div>
        </div>

        <p class="text-muted small">Tổng: {{ filteredEmployees.length }} nhân viên</p>

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
                    <tr v-for="employee in paginatedEmployees" :key="employee.id">
                        <td class="text-center">{{ employee.id }}</td>
                        <td class="text-center">{{ employee.accountId }}</td>
                        <td class="text-center">{{ employee.fullName }}</td>
                        <td class="text-center">{{ employee.gender === true ? "Nam" : "Nữ" }}</td>
                        <td class="text-center">{{ employee.email }}</td>
                        <td class="text-center">{{ employee.numberPhone }}</td>
                        <td class="text-center">{{ formatDateTime(employee.birthOfDate) }}</td>
                        <td class="text-center">
                            <span v-if="employee.active" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">{{ employee.createdBy }}</td>
                        <td class="text-center">{{ formatDateTime(employee.createdDate) }}</td>
                        <td>
                            <button class="btn btn-success btn-sm" @click="editEmployee(employee)">Sửa</button>
                            <button class="btn btn-danger btn-sm" @click="changeStatus(employee.id)">Chuyển trạng
                                thái</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
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
