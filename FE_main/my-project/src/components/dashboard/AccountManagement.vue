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

async function changeStatus(id) {
    if (!confirm('Bạn có chắc muốn chuyển trạng thái khách hàng này?')) return;

    const updateAccount = {
        id: id,
    };

    try {
        await axios.put(`http://localhost:8080/account/updateStatus/${id}`, updateAccount)
        alert('Đã chuyển trạng thái tài khoản');
        await fetchAccount();
    } catch (error) {
        console.error('Lỗi chuyển trạng thái tài khoản:', error.response ? error.response.data : error.message);
        alert('Không thể chuyển trạng thái tài khoản');
    }
}

const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() => Math.ceil(accounts.value.length / pageSize));

const paginatedAccounts = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return accounts.value.slice(start, start + pageSize);
});


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

onMounted(() => {
    fetchAccount();
})

</script>
<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Tài Khoản</h2>

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
                            <span v-if="account.isActive" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">
                            <span v-if="account.roleId === 1">Admin</span>
                            <span v-else-if="account.roleId === 2">Khách hàng</span>
                            <span v-else>Nhân viên</span>
                        </td>

                        <td class="text-center">
                            <button class="btn btn-danger btn-sm" @click="changeStatus(account.id)">
                               Chuyển trạng Thái
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
