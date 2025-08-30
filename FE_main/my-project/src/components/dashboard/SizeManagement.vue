<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'

// Danh sách size
const Sizes = ref([])

// Lấy dữ liệu size từ backend
const fetchSize = async () => {
  try {
    const response = await axios.get('http://localhost:8080/size/show')
    Sizes.value = response.data
    console.log(Sizes.value)
  } catch (error) {
    console.log('Lỗi hiển thị size:', error)
  }
}

// Form size
const form = ref({ id: null, eu: '', description: '' })
const isEditing = ref(false)

// Phân trang
const currentPage = ref(1)
const pageSize = 5

const totalPages = computed(() =>
  Math.ceil(Sizes.value.length / pageSize)
)

const paginatedSizes = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return Sizes.value.slice(start, start + pageSize)
})

function resetForm() {
  form.value = { id: null, eu: '', description: '' }
  isEditing.value = false
}

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

async function saveSize() {
  try {
    if (isEditing.value) {
      await axios.put(`http://localhost:8080/size/update/${form.value.id}`, form.value)
    } else {
      await axios.post('http://localhost:8080/size/add', form.value)
    }
    await fetchSize()
    resetForm()
  } catch (error) {
    console.log('Lỗi lưu size:', error)
  }
}

function editSize(size) {
  form.value = { ...size }
  isEditing.value = true
}

async function changeStatus(id) {
    if (!confirm('Bạn có chắc muốn chuyển trạng thái kích cỡ này?')) return;

    const updateSize = {
        id: id,
    };

    try {
        await axios.put(`http://localhost:8080/size/updateStatus/${id}`, updateSize)
        alert('Đã chuyển trạng thái kích cỡ');
        await fetchSize();
    } catch (error) {
        console.error('Lỗi chuyển trạng thái kích cỡ:', error.response ? error.response.data : error.message);
        alert('Không thể chuyển trạng thái kích cỡ');
    }
}

onMounted(() => {
  fetchSize()
})
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center mb-4 fw-bold">Quản Lý Size</h2>

    <!-- Form thêm/sửa size -->
    <form @submit.prevent="saveSize" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">Số Size (EU)</label>
        <input v-model="form.eu" required class="form-control" />
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

    <!-- Bảng danh sách size -->
    <div class="table-container table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-secondary text-center">
          <tr>
            <th style="width: 60px">ID</th>
            <th style="width: 180px">Size (EU)</th>
            <th style="width: 300px">Mô tả</th>
            <th style="width: 160px">Trạng thái</th>
            <th style="width: 160px">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="size in paginatedSizes" :key="size.id">
            <td class="text-center">{{ size.id }}</td>
            <td>{{ size.eu }}</td>
            <td class="text-wrap">{{ size.description }}</td>
            <td class="text-center">
              <span v-if="size.active" class="badge bg-success">Hoạt động</span>
              <span v-else class="badge bg-danger">Không hoạt động</span>
            </td>
            <td class="text-center">
              <button class="btn btn-success btn-sm me-2" @click="editSize(size)">Sửa</button>
              <button class="btn btn-danger btn-sm" @click="changeStatus(size.id)">Chuyển trạng thái</button>
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
