<script setup>
import { ref, onMounted, computed } from "vue";
import { Modal } from "bootstrap";
import axios from "axios";

// Khuyến mãi
const promotions = ref([]);
const form = ref({
  id: null,
  promotionCode: "",
  name: "",
  type: 1,
  value: 0,
  startDate: "",
  endDate: "",
  note: "",
  status: 1,
  apply_all: false,
});

const fetchPromotions = async () => {
  try {
    const response = await axios.get("http://localhost:8080/promotion/show");
    promotions.value = response.data;
    console.log(promotions.value)
  } catch (error) {
    console.error("Lỗi khi load khuyến mãi:", error);
  }
};

const savePromotion = async () => {
  try {
    const payload = {
      ...form.value,
      startDate: form.value.startDate + "T00:00:00",
      endDate: form.value.endDate + "T00:00:00"
    };

    if (form.value.id) {
      await axios.put(`http://localhost:8080/promotion/update/${form.value.id}`, payload);
    } else {
      const response = await axios.post("http://localhost:8080/promotion/add", payload);
      form.value.id = response.data.id;
    }

    await fetchPromotions();
    resetForm();
  } catch (error) {
    console.error("Lỗi khi lưu khuyến mãi:", error); // sửa từ `err` -> `error`
  }
};


const editPromotion = (promo) => {
  form.value = {
    ...promo,
    startDate: promo.startDate ? promo.startDate.substring(0, 10) : "",
    endDate: promo.endDate ? promo.endDate.substring(0, 10) : ""
  };
};

const deletePromotion = async (id) => {
  try {
    if (confirm("Bạn có chắc chắn muốn xóa size này không?")) {
      await axios.delete(`http://localhost:8080/promotionDetail/delete/${id}`);
      await fetchPromotions();
    }
    if (form.value.id === id) resetForm();
  } catch (err) {
    console.error("Lỗi khi xoá:", err);
  }
};

const resetForm = () => {
  form.value = {
    id: null,
    promotionCode: "",
    name: "",
    type: 1,
    value: 0,
    startDate: "",
    endDate: "",
    note: "",
    status: 1,
    apply_all: false,
  };
};

// Khuyến mãi chi tiết
const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString();



const products = ref([]); // Toàn bộ sản phẩm
const selectedProducts = ref([]); // ID sản phẩm được chọn trong promotion
const promotionDetails = ref({}); // Lưu theo promotion ID

const searchKeyword = ref("");
const pageSize = 5;
const currentPage = ref(1);



onMounted(async () => {
  await fetchPromotions();     
});

</script>

<template>
  <div class="container mt-4">
    <h2>Quản lý khuyến mãi</h2>

    <!-- FORM THÊM/SỬA KHUYẾN MÃI -->
    <div class="card mb-4">
      <div class="card-header">
        {{ form.id ? "Cập nhật khuyến mãi" : "Thêm khuyến mãi mới" }}
      </div>
      <div class="card-body">
        <form @submit.prevent="savePromotion" class="row g-3">
          <div class="col-md-4">
            <label class="form-label">Mã KM</label>
            <input v-model="form.promotionCode" class="form-control" required />
          </div>
          <div class="col-md-4">
            <label class="form-label">Tên khuyến mãi</label>
            <input v-model="form.name" class="form-control" required />
          </div>
          <div class="col-md-4">
            <label class="form-label">Loại</label>
            <select v-model="form.type" class="form-select">
              <option :value="1">%</option>
              <option :value="2">Cố định</option>
            </select>
          </div>

          <div class="col-md-3">
            <label class="form-label">Giá trị</label>
            <input type="number" v-model="form.value" class="form-control" required />
          </div>
          <div class="col-md-3">
            <label class="form-label">Từ ngày</label>
            <input type="date" v-model="form.startDate" class="form-control" required />
          </div>
          <div class="col-md-3">
            <label class="form-label">Đến ngày</label>
            <input type="date" v-model="form.endDate" class="form-control" required />
          </div>
          <div class="col-md-3">
            <label class="form-label d-block">Trạng thái</label>
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="radio" id="statusActive" value="1" v-model="form.status" />
              <label class="form-check-label" for="statusActive">Đang áp dụng</label>
            </div>
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="radio" id="statusInactive" value="0" v-model="form.status" />
              <label class="form-check-label" for="statusInactive">Ngưng áp dụng</label>
            </div>
          </div>

          <div class="col-md-12">
            <label class="form-label">Ghi chú</label>
            <textarea v-model="form.note" class="form-control" rows="2"></textarea>
          </div>

          <div class="col-12 text-end">
            <button type="submit" class="btn btn-success me-2">Lưu</button>
            <button v-if="form.id" type="button" @click="resetForm" class="btn btn-secondary me-2">
              Huỷ
            </button>
            <button type="button" @click="resetForm" class="btn btn-outline-secondary">
              Làm mới
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- DANH SÁCH -->
    <table class="table table-bordered">
      <thead>
        <tr>
          <th>STT</th>
          <th>Mã KM</th>
          <th>Tên</th>
          <th>Loại</th>
          <th>Giá trị</th>
          <th>Thời gian</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(promo, index) in promotions" :key="promo.id">
          <td>{{ index + 1 }}</td>
          <td>{{ promo.promotionCode }}</td>
          <td>{{ promo.name }}</td>
          <td>{{ promo.type === 1 ? "%" : "Cố định" }}</td>
          <td>{{ promo.value }}</td>
          <td>
            {{ formatDate(promo.startDate) }} -
            {{ formatDate(promo.endDate) }}
          </td>
          <td>
            <span :class="promo.status === 1 ? 'text-success' : 'text-secondary'">
              {{ promo.status === 1 ? "Đang áp dụng" : "Ngưng áp dụng" }}
            </span>
          </td>
          <td>
            <button class="btn btn-sm btn-warning me-1" @click="editPromotion(promo)">
              Sửa
            </button>
            <button class="btn btn-sm btn-danger me-1" @click="deletePromotion(promo.id)">
              Xoá
            </button>
          </td>
        </tr>
      </tbody>
    </table>


  </div>
</template>

<style scoped>
.card-header {
  font-weight: bold;
  background-color: #f8f9fa;
}

.modal-body {
  max-height: 500px;
  overflow-y: auto;
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
