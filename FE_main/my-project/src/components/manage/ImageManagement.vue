<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'

// Danh sách ảnh
const images = ref([])

// Form dữ liệu ảnh
const form = ref({
  id: null,
  url: '',
  isMain: false, // sửa từ "main" thành "isMain"
  productDetailId: null
})

const isEditing = ref(false)
const currentPage = ref(1)
const pageSize = 5

// Tính tổng số trang
const totalPages = computed(() =>
  Math.ceil(images.value.length / pageSize)
)

// Danh sách ảnh hiển thị theo trang
const paginatedImages = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return images.value.slice(start, start + pageSize)
})

// Lấy danh sách ảnh từ backend
const fetchImages = async () => {
  try {
    const res = await axios.get('http://localhost:8080/image/show')
    images.value = res.data
  } catch (error) {
    console.error('Lỗi tải ảnh:', error)
  }
}

// Khi component mount
onMounted(() => {
  fetchImages()
})

// Reset form
function resetForm() {
  form.value = {
    id: null,
    url: '',
    isMain: false,
    productDetailId: null
  }
  isEditing.value = false
}

// Chuyển trang
function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// Lưu ảnh (thêm hoặc cập nhật)
async function saveImage() {
  try {
    if (!form.value.productDetailId || !form.value.url) {
      alert('Vui lòng nhập đầy đủ thông tin.')
      return
    }

    if (isEditing.value) {
      await axios.put(`http://localhost:8080/image/update/${form.value.id}`, form.value)
    } else {
      await axios.post('http://localhost:8080/image/add', form.value)
    }

    await fetchImages()
    resetForm()
  } catch (error) {
    console.error('Lỗi lưu ảnh:', error)
  }
}

// Sửa ảnh
function editImage(image) {
  form.value = { ...image }
  isEditing.value = true
}

// Xoá ảnh
async function deleteImage(id) {
  if (confirm('Bạn có chắc chắn muốn xoá ảnh này không?')) {
    try {
      await axios.delete(`http://localhost:8080/image/delete/${id}`)
      await fetchImages()
    } catch (error) {
      console.error('Lỗi xoá ảnh:', error)
    }
  }
}
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center fw-bold mb-4">Quản Lý Ảnh Sản Phẩm</h2>

    <!-- Form nhập -->
    <form @submit.prevent="saveImage" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">URL ảnh</label>
        <input v-model="form.url" class="form-control" placeholder="./images/shoe1.webp" required />
      </div>

      <div class="mb-3">
        <label class="form-label">ID chi tiết sản phẩm</label>
        <input v-model="form.productDetailId" type="number" class="form-control" required />
      </div>

      <div class="mb-3 form-check">
        <input v-model="form.isMain" type="checkbox" class="form-check-input" id="mainCheck" />
        <label class="form-check-label" for="mainCheck">Là ảnh chính</label>
      </div>

      <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">
          {{ isEditing ? 'Cập nhật' : 'Thêm' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
      </div>
    </form>

    <!-- Bảng danh sách ảnh -->
    <div class="table-container table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-secondary text-center">
          <tr>
            <th style="width: 60px">ID</th>
            <th>Ảnh</th>
            <th>URL</th>
            <th>Ảnh chính</th>
            <th style="width: 160px">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="image in paginatedImages" :key="image.id">
            <td class="text-center">{{ image.id }}</td>
            <td class="text-center">
              <img :src="image.url" style="max-height: 60px" :alt="'Ảnh ' + image.id" />
            </td>
            <td>{{ image.url }}</td>
            <td class="text-center">
              <span v-if="image.isMain" class="badge bg-success">Chính</span>
              <span v-else class="text-muted">--</span>
            </td>
            <td class="text-center">
              <button class="btn btn-success btn-sm me-2" @click="editImage(image)">Sửa</button>
              <button class="btn btn-danger btn-sm" @click="deleteImage(image.id)">Xoá</button>
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

        <li
          v-for="page in totalPages"
          :key="page"
          class="page-item"
          :class="{ active: page === currentPage }"
        >
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
