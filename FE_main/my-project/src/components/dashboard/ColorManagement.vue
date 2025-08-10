<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'

// Danh sách màu
const Colors = ref([])

// Lấy dữ liệu màu từ backend
const fetchColor = async () => {
  try {
    const response = await axios.get('http://localhost:8080/color/show')
    Colors.value = response.data
    console.log(Colors.value)
  } catch (error) {
    console.log('Lỗi hiển thị màu:', error)
  }
}

// Form màu
const form = ref({ id: null, name: '', description: '' })
const isEditing = ref(false)

// Phân trang
const currentPage = ref(1)
const pageSize = 5

const totalPages = computed(() =>
  Math.ceil(Colors.value.length / pageSize)
)

const paginatedColors = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return Colors.value.slice(start, start + pageSize)
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

async function saveColor() {
  try {
    if (isEditing.value) {
      await axios.put(`http://localhost:8080/color/update/${form.value.id}`, form.value)
    } else {
      await axios.post('http://localhost:8080/color/add', form.value)
    }
    await fetchColor()
    resetForm()
  } catch (error) {
    console.log('Lỗi lưu màu:', error)
  }
}

function editColor(color) {
  form.value = { ...color }
  isEditing.value = true
}

async function deleteColor(id) {
  try {
    if (confirm('Bạn có chắc chắn muốn xóa màu này không?')) {
      await axios.delete(`http://localhost:8080/color/delete/${id}`)
      await fetchColor()
    }
  } catch (error) {
    console.error('Lỗi khi xóa màu:', error)
  }
}

onMounted(() => {
  fetchColor()
})
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center mb-4 fw-bold">Quản Lý Màu Sắc</h2>

    <!-- Form thêm/sửa màu -->
    <form @submit.prevent="saveColor" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">Tên màu</label>
        <input v-model="form.name" required class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Mô tả</label>
        <input v-model="form.description" class="form-control" />
      </div>
      <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">
          {{ isEditing ? 'Cập nhật' : 'Thêm' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
      </div>
    </form>

    <!-- Bảng danh sách màu -->
    <div class="table-container table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-secondary text-center">
          <tr>
            <th style="width: 60px">ID</th>
            <th style="width: 180px">Tên màu</th>
            <th style="width: 300px">Mô tả</th>
            <th style="width: 160px">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="color in paginatedColors" :key="color.id">
            <td class="text-center">{{ color.id }}</td>
            <td>{{ color.name }}</td>
            <td class="text-wrap">{{ color.description }}</td>
            <td class="text-center">
              <button class="btn btn-success btn-sm me-2" @click="editColor(color)">Sửa</button>
              <button class="btn btn-danger btn-sm" @click="deleteColor(color.id)">Xoá</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
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
