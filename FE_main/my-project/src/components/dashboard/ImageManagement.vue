<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'
import vSelect from "vue-select"
import "vue-select/dist/vue-select.css"

const images = ref([])
const selectedFiles = ref([])
const previewUrls = ref([])
const products = ref([])

const form = ref({
  id: null,
  isMain: false,
  productDetailId: null
})

const isEditing = ref(false)
const currentPage = ref(1)
const pageSize = 5
const mainImageIndex = ref(null)

const totalPages = computed(() =>
  Math.ceil(images.value.length / pageSize)
)

const paginatedImages = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return images.value.slice(start, start + pageSize)
})

const fetchImages = async () => {
  try {
    const res = await axios.get('http://localhost:8080/image/show')
    images.value = res.data
  } catch (error) {
    console.error('Lỗi tải ảnh:', error)
  }
}

const fetchProductDetails = async () => {
  try {
    const response = await axios.get('http://localhost:8080/productDetail/showProductReceipt')
    products.value = response.data.map(item => ({
      id: item.productDetailId,
      name: `Giày ${item.productName}`
    }))
  } catch (error) {
    console.log("Lỗi", error)
  }
}

onMounted(() => {
  fetchImages()
  fetchProductDetails()
})

function resetForm() {
  form.value = {
    id: null,
    isMain: false,
    productDetailId: null
  }
  selectedFiles.value = []
  previewUrls.value = []
  isEditing.value = false
  mainImageIndex.value = null
}

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function handleFileChange(event) {
  selectedFiles.value = Array.from(event.target.files)
  previewUrls.value = selectedFiles.value.map(file => URL.createObjectURL(file))
  mainImageIndex.value = null
}

function selectMainImage(index) {
  mainImageIndex.value = index
}

function getImageCountByProductDetailId(productDetailId) {
  return images.value.filter(img => img.productDetailId === productDetailId).length
}

async function saveImage() {
  try {
    if (!form.value.productDetailId || (!selectedFiles.value.length && !isEditing.value)) {
      alert('Vui lòng chọn ảnh và nhập đầy đủ thông tin.')
      return
    }

    const currentImageCount = getImageCountByProductDetailId(form.value.productDetailId)
    if (!isEditing.value && currentImageCount + selectedFiles.value.length > 6) {
      alert('Mỗi sản phẩm chỉ được tối đa 6 ảnh. Vui lòng xoá bớt để thêm ảnh mới.')
      return
    }

    const formData = new FormData()
    formData.append('productDetailId', form.value.productDetailId)

    if (isEditing.value) {
      formData.append('id', form.value.id)
      formData.append('isMain', form.value.isMain ? 'true' : 'false')
      formData.append('productDetailId', form.value.productDetailId)

      if (selectedFiles.value.length) {
        formData.append('file', selectedFiles.value[0])
      }

      await axios.put(`http://localhost:8080/image/update/${form.value.id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
    else {
      selectedFiles.value.forEach(file => {
        formData.append('files', file)
      })
      formData.append('mainImageIndex', mainImageIndex.value ?? -1)

      await axios.post('http://localhost:8080/image/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    }

    await fetchImages()
    resetForm()
  } catch (error) {
    console.error('Lỗi lưu ảnh:', error)
  }
}


function editImage(image) {
  form.value = {
    id: image.id,
    isMain: image.isMain,
    productDetailId: image.productDetailId
  }
  selectedFiles.value = []
  previewUrls.value = []
  isEditing.value = true
}

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

    <form @submit.prevent="saveImage" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">Chọn ảnh</label>
        <input type="file" class="form-control" accept="image/*" multiple @change="handleFileChange" />
      </div>

      <div v-if="previewUrls.length" class="mb-3 d-flex flex-wrap gap-3">
        <div v-for="(url, index) in previewUrls" :key="index" class="text-center position-relative"
          style="cursor: pointer;" @click="selectMainImage(index)">
          <img :src="url" :alt="'Preview ' + index" :style="{
            height: '80px',
            border: mainImageIndex === index ? '3px solid red' : '1px solid #ccc',
            borderRadius: '4px',
            boxShadow: mainImageIndex === index ? '0 0 8px red' : 'none'
          }" />
          <div v-if="mainImageIndex === index"
            class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            Chính
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">Sản phẩm</label>
        <v-select v-model="form.productDetailId" :options="products" label="name" :reduce="p => p.id"
          placeholder="Chọn sản phẩm..." />
      </div>

      <div v-if="isEditing" class="mb-3 form-check">
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

    <!-- Danh sách ảnh -->
    <div class="table-container table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-secondary text-center">
          <tr>
            <th>ID sản phẩm</th>
            <th>Ảnh</th>
            <th>URL</th>
            <th style="width: 160px">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="image in paginatedImages" :key="image.id">
            <td class="text-center">{{ image.productDetailId }}</td>
            <td class="text-center">
              <img :src="image.url" style="max-height: 60px" :alt="'Ảnh ' + image.id" />
            </td>
            <td>{{ image.url }}</td>
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
