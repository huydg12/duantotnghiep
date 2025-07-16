<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

// Danh sách khuyến mãi
const promotions = ref([]);
const selectedPromotion = ref(null); // Dùng để hiển thị chi tiết

// Form dữ liệu khuyến mãi
const form = ref({
  id: null,
  promotionCode: "",
  name: "",
  type: 1,
  value: 0,
  startDate: "",
  endDate: "",
  status: 1,
  note: "",
  createdDate: new Date().toISOString(),
  modifiedDate: new Date().toISOString()
});

const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() =>
  Math.ceil(promotions.value.length / pageSize)
);

const paginatedPromotions = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return promotions.value.slice(start, start + pageSize);
});

const fetchPromotion = async () => {
  try {
    const response = await axios.get("http://localhost:8080/promotion/show");
    promotions.value = response.data;
  } catch (error) {
    console.log("Lỗi hiển thị:", error);
  }
};

function toDateTimeLocal(dateStr) {
  return dateStr ? `${dateStr}T00:00:00` : null;
}

async function save() {
  try {
    const now = new Date().toISOString();
    form.value.value = parseFloat(form.value.value);
    form.value.startDate = toDateTimeLocal(form.value.startDate);
    form.value.endDate = toDateTimeLocal(form.value.endDate);

    if (isEditing.value) {
      form.value.modifiedDate = now;
      await axios.put(
        `http://localhost:8080/promotion/update/${form.value.id}`,
        form.value
      );
    } else {
      form.value.createdDate = now;
      form.value.modifiedDate = now;
      await axios.post("http://localhost:8080/promotion/add", form.value);
    }

    await fetchPromotion();
    resetForm();
  } catch (error) {
    console.log("Lỗi save:", error);
    if (error.response) {
      console.log("Chi tiết từ server:", error.response.data);
    }
  }
}

function editPromotion(promotion) {
  form.value = {
    ...promotion,
    startDate: promotion.startDate?.split("T")[0] || "",
    endDate: promotion.endDate?.split("T")[0] || ""
  };
  isEditing.value = true;
}

function deletePromotion(id) {
  if (confirm("Bạn chắc chắn muốn xoá khuyến mãi này?")) {
    axios
      .delete(`http://localhost:8080/promotion/delete/${id}`)
      .then(() => fetchPromotion())
      .catch((error) => console.log("Lỗi xoá:", error));
  }
}

function showDetail(promotion) {
  selectedPromotion.value = promotion;
}

function resetForm() {
  form.value = {
    id: null,
    promotionCode: "",
    name: "",
    type: 1,
    value: 0,
    startDate: "",
    endDate: "",
    status: 1,
    note: "",
    createdDate: new Date().toISOString(),
    modifiedDate: new Date().toISOString()
  };
  isEditing.value = false;
  selectedPromotion.value = null;
}

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
}

function formatDateTime(datetimeStr) {
  if (!datetimeStr) return "";
  const date = new Date(datetimeStr);
  return `${date.getDate().toString().padStart(2, "0")}-${(date.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${date.getFullYear()}`;
}

onMounted(() => {
  fetchPromotion();
});
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center fw-bold mb-4">Quản Lý Khuyến Mãi</h2>

    <!-- Form -->
    <form @submit.prevent="save" class="border p-4 rounded bg-light mb-4">
      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">Mã Khuyến Mãi</label>
          <input v-model="form.promotionCode" required class="form-control" />
        </div>
        <div class="col-md-6">
          <label class="form-label">Tên Khuyến Mãi</label>
          <input v-model="form.name" required class="form-control" />
        </div>
        <div class="col-md-6">
          <label class="form-label">Loại</label>
          <select v-model="form.type" class="form-select">
            <option :value="1">Phần trăm (%)</option>
            <option :value="2">Số tiền cố định</option>
          </select>
        </div>
        <div class="col-md-6">
          <label class="form-label">Giá Trị</label>
          <input v-model="form.value" type="number" min="0" required class="form-control" />
        </div>
        <div class="col-md-6">
          <label class="form-label">Ngày Bắt Đầu</label>
          <input type="date" v-model="form.startDate" required class="form-control" />
        </div>
        <div class="col-md-6">
          <label class="form-label">Ngày Kết Thúc</label>
          <input type="date" v-model="form.endDate" required class="form-control" />
        </div>
        <div class="col-md-12">
          <label class="form-label">Ghi chú</label>
          <textarea v-model="form.note" class="form-control" rows="2" placeholder="Nhập ghi chú"></textarea>
        </div>
        <div class="col-md-12">
          <label class="form-label d-block">Trạng thái</label>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" :value="1" id="active" v-model="form.status" />
            <label class="form-check-label" for="active">Hoạt động</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" :value="0" id="inactive" v-model="form.status" />
            <label class="form-check-label" for="inactive">Không hoạt động</label>
          </div>
        </div>
        <div class="d-flex gap-2 mt-3">
          <button type="submit" class="btn btn-primary">{{ isEditing ? "Cập nhật" : "Thêm" }}</button>
          <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
        </div>
      </div>
    </form>

    <!-- Bảng chính -->
    <div class="table-responsive table-container mb-4">
      <table class="table table-bordered align-middle table-hover">
        <thead class="table-secondary text-center">
          <tr>
            <th>ID</th>
            <th>Mã</th>
            <th>Tên</th>
            <th>Loại</th>
            <th>Giá trị</th>
            <th>Bắt đầu</th>
            <th>Kết thúc</th>
            <th>Trạng thái</th>
            <th>Ghi chú</th>
            <th>Ngày tạo</th>
            <th>Ngày sửa</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="promotion in paginatedPromotions" :key="promotion.id" @click="showDetail(promotion)">
            <td class="text-center">{{ promotion.id }}</td>
            <td class="text-center">{{ promotion.promotionCode }}</td>
            <td class="text-center fw-bold text-primary" style="cursor: pointer">{{ promotion.name }}</td>
            <td class="text-center">{{ promotion.type === 1 ? "Phần trăm" : "Số tiền cố định" }}</td>
            <td class="text-center">{{ promotion.value }}</td>
            <td class="text-center">{{ formatDateTime(promotion.startDate) }}</td>
            <td class="text-center">{{ formatDateTime(promotion.endDate) }}</td>
            <td class="text-center">
              <span :class="promotion.status === 1 ? 'badge bg-success' : 'badge bg-danger'">
                {{ promotion.status === 1 ? "Hoạt động" : "Không hoạt động" }}
              </span>
            </td>
            <td>{{ promotion.note }}</td>
            <td class="text-center">{{ formatDateTime(promotion.createdDate) }}</td>
            <td class="text-center">{{ formatDateTime(promotion.modifiedDate) }}</td>
            <td class="text-center">
              <button class="btn btn-success btn-sm me-1" @click.stop="editPromotion(promotion)">Sửa</button>
              <button class="btn btn-danger btn-sm" @click.stop="deletePromotion(promotion.id)">Xoá</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <nav>
      <ul class="pagination justify-content-center custom-pagination">
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

    <!-- Chi tiết khuyến mãi -->
    <div v-if="selectedPromotion" class="alert alert-info mt-4">
      <h5>Chi tiết khuyến mãi:</h5>
      <p><strong>Mã:</strong> {{ selectedPromotion.promotionCode }}</p>
      <p><strong>Tên:</strong> {{ selectedPromotion.name }}</p>
      <p><strong>Loại:</strong> {{ selectedPromotion.type === 1 ? 'Phần trăm' : 'Số tiền cố định' }}</p>
      <p><strong>Giá trị:</strong> {{ selectedPromotion.value }}</p>
      <p><strong>Thời gian:</strong> {{ formatDateTime(selectedPromotion.startDate) }} - {{ formatDateTime(selectedPromotion.endDate) }}</p>
      <p><strong>Trạng thái:</strong> {{ selectedPromotion.status === 1 ? 'Hoạt động' : 'Không hoạt động' }}</p>
      <p><strong>Ghi chú:</strong> {{ selectedPromotion.note || 'Không có' }}</p>
    </div>
  </div>
</template>

<style scoped>
.table-container {
  min-height: 300px;
}
.custom-pagination .page-link {
  cursor: pointer;
  color: #007bff;
  border-radius: 6px;
  margin: 0 5px;
}
.custom-pagination .page-link:hover {
  background-color: #e2e6ea;
  color: #0056b3;
}
.custom-pagination .page-item.active .page-link {
  background-color: #007bff;
  color: white;
  font-weight: bold;
}
</style>
