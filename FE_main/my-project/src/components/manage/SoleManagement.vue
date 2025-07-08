<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
const soles = ref([])

const fetchSole = async() => {
    try{
        const response = await axios.get('http://localhost:8080/sole/show')
        soles.value = response.data
        console.log(soles.value)
    }catch(error){
        console.log('lỗi hiển thị ', error)
    }
}

onMounted(() => {
    fetchSole()
})

async function saveSole() {
    try{
        if(isEditing.value){
            await axios.put(`http://localhost:8080/sole/update/${form.value.id}`, form.value)
        }else{
            await axios.post('http://localhost:8080/sole/add', form.value)
        }
        await fetchSole()
        resetForm()
    }catch(error){
        console.log('lỗi save ', error)
    }
}

function editSole(sole){
    form.value = {...sole}
    isEditing.value = true
} 

async function deleteSole(id) {
    try{
        if(confirm ('Bạn có chắc là muốn xóa đế giày không ')){
        await axios.delete(`http://localhost:8080/sole/delete/${id}`)
        await fetchSole()
        }
    }catch(error){
        console.log('lỗi xóa ', error)
    }
}

const form = ref({ id: null, name: '', description: '' })
const isEditing = ref(false)
const currentPage = ref(1)
const pageSize = 5

const totalPages = computed(() =>
    Math.ceil(soles.value.length / pageSize)
)

const paginatedSoles = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return soles.value.slice(start, start + pageSize)
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

</script>
<template>
    <div class="container py-4">
        <h2 class="text-center mb-4 fw-bold">Quản Lý Đế</h2>

        <!-- Form -->
        <form @submit.prevent="saveSole" class="border p-4 rounded bg-light mb-4">
            <div class="mb-3">
                <label class="form-label">Tên đế</label>
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
                        <th style="width: 160px">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="soles in paginatedSoles" :key="soles.id">
                        <td class="text-center">{{ soles.id }}</td>
                        <td>{{ soles.name }}</td>
                        <td class="text-wrap">{{ soles.description }}</td>
                        <td class="text-center">
                            <button class="btn btn-success btn-sm me-2" @click="editSole(soles)"> Sửa </button>
                            <button class="btn btn-danger btn-sm" @click="deleteSole(soles.id)"> Xoá </button>
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
