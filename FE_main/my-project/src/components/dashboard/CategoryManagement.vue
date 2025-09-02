<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'

const categories = ref([])

const fetchCategories = async () => {
    try {
        const response = await axios.get('http://localhost:8080/category/show')
        categories.value = response.data
        console.log(categories)
    } catch (error) {
        console.log('lỗi hiển thị ', error)
    }
}

async function saveCategories() {
    try {
        if (isEditing.value) {
            await axios.put(`http://localhost:8080/category/update/${form.value.id}`, form.value)
        } else {
            await axios.post('http://localhost:8080/category/add', form.value)
        }
        fetchCategories()
        resetForm()
    } catch (error) {
        console.log('lỗi save', error)
    }
}

function editCategories(category) {
    form.value = { ...category }
    isEditing.value = true
}

async function changeStatus(id) {
    if (!confirm('Bạn có chắc muốn chuyển trạng thái thể loại này?')) return;

    const updateCategory = {
        id: id,
    };

    try {
        await axios.put(`http://localhost:8080/category/updateStatus/${id}`, updateCategory)
        alert('Đã chuyển trạng thái thể loại');
        await fetchCategories();
    } catch (error) {
        console.error('Lỗi chuyển trạng thái thể loại:', error.response ? error.response.data : error.message);
        alert('Không thể chuyển trạng thái thể loại');
    }
}

const form = ref({ id: null, name: '', description: '' })
const isEditing = ref(false)
const currentPage = ref(1)
const pageSize = 5

const totalPages = computed(() =>
    Math.ceil(categories.value.length / pageSize)
)

const paginatedCategories = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return categories.value.slice(start, start + pageSize)
})

function resetForm() {
    form.value = { id: null, name: '', description: '' }
    isEditing.value = false
}

function goToPage(page) {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
    }
}

onMounted(() => {
    fetchCategories()
})

</script>
<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Loại</h2>

        <!-- Form -->
        <form @submit.prevent="saveCategories" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Tên loại</label>
                <input v-model="form.name" required class="form-control" />
            </div>
            <div class="mb-3">
                <label class="form-label">Mô tả</label>
                <input v-model="form.description" class="form-control" />
            </div>
            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary"> {{ isEditing ? 'Cập nhật' : 'Thêm' }}</button>
                <button type="button" class="btn btn-secondary" @click="resetForm"> Làm mới </button>
            </div>
        </form>

        <!-- Table -->
        <div class="table-container table-responsive">
            <table class="table table-bordered table-hover align-middle">
                <thead class="table-secondary text-center">
                    <tr>
                        <th style="width: 60px">ID</th>
                        <th style="width: 180px">Tên thương hiệu</th>
                        <th style="width: 300px">Mô tả</th>
                        <th style="width: 160px">Trạng thái</th>
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="category in paginatedCategories" :key="category.id">
                        <td class="text-center">{{ category.id }}</td>
                        <td>{{ category.name }}</td>
                        <td class="text-wrap">{{ category.description }}</td>
                        <td class="text-center">
                            <span v-if="category.active" class="badge bg-success">Hoạt động</span>
                            <span v-else class="badge bg-danger">Không hoạt động</span>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editCategories(category)"> Sửa </button>
                            <button class="btn btn-danger btn-sm" @click="changeStatus(category.id)"> Chuyển trạng thái
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
                    <button class="page-link" @click="goToPage(currentPage - 1)"> « </button>
                </li>

                <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === currentPage }">
                    <button class="page-link" @click="goToPage(page)"> {{ page }} </button>
                </li>

                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="goToPage(currentPage + 1)"> » </button>
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
