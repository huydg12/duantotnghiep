<script setup>
import { ref, onMounted, computed } from "vue";
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
  active: true,
  apply_all: false,
});

const fetchPromotion = async () => {
  try {
    const response = await axios.get("http://localhost:8080/promotion/show");
    promotions.value = response.data;
    console.log(promotions.value)
  } catch (error) {
    console.error("Lỗi khi load khuyến mãi:", error);
  }
};

const originalPromotion = ref(null);

// Gọi khi mở form EDIT (sau khi load dữ liệu vào form)
function snapshotOriginal() {
  originalPromotion.value = JSON.parse(JSON.stringify(form.value));
}

// Chuẩn hóa để so sánh (bỏ id, trim string, ép kiểu)
const normalize = (x) => ({
  promotionCode: (x.promotionCode ?? "").trim(),
  name: (x.name ?? "").trim(),
  type: Number(x.type ?? 0),
  value: Number(x.value ?? 0),
  startDate: (x.startDate ?? ""), // so sánh theo ngày, chưa cộng T00:00:00
  endDate: (x.endDate ?? ""),
  note: (x.note ?? "").trim(),
  active: !!x.active,
  apply_all: !!x.apply_all,
});

const savePromotion = async () => {
  try {
    // Chỉ check “không đổi” khi đang EDIT & có snapshot
    if (form.value.id && originalPromotion.value) {
      const curr = normalize(form.value);
      const prev = normalize(originalPromotion.value);
      if (JSON.stringify(curr) === JSON.stringify(prev)) {
        alert("Không có thay đổi nào để lưu.");
        return;
      }
    }

    const payload = {
      ...form.value,
      startDate: form.value.startDate + "T00:00:00",
      endDate: form.value.endDate + "T00:00:00",
    };

    if (form.value.id) {
      await axios.put(`http://localhost:8080/promotion/update/${form.value.id}`, payload);
    } else {
      const res = await axios.post("http://localhost:8080/promotion/add", payload);
      form.value.id = res.data.id;
    }

    // Cập nhật lại snapshot sau khi lưu thành công
    snapshotOriginal();

    await fetchPromotion();
    resetForm();
  } catch (error) {
    console.error("Lỗi khi lưu khuyến mãi:", error);
  }
};

const editPromotion = (promo) => {
  form.value = {
    ...promo,
    // chuẩn hoá để UI và snapshot nhất quán
    type: Number(promo.type),
    value: Number(promo.value),
    startDate: promo.startDate ? promo.startDate.substring(0, 10) : "",
    endDate:   promo.endDate   ? promo.endDate.substring(0, 10)   : "",
    active: !!promo.active,
    apply_all: !!promo.apply_all,
  };

  snapshotOriginal(); // 👈 Quan trọng: gọi sau khi gán form
};

async function changeStatus(id) {
  if (!confirm('Bạn có chắc muốn chuyển trạng thái khuyến mãi này?')) return;

  const updatePromotion = {
    id: id,
  };

  try {
    await axios.put(`http://localhost:8080/promotion/updateStatus/${id}`, updatePromotion)
    alert('Đã chuyển trạng thái khuyến mãi');
    await fetchPromotion();
  } catch (error) {
    console.error('Lỗi chuyển trạng thái khuyến mãi:', error.response ? error.response.data : error.message);
    alert('Không thể chuyển trạng thái khuyến mãi');
  }
}

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
    active: true,
    apply_all: false,
  };
};

const formatDate = (str) => {
  const d = new Date(str);
  return `${d.getDate().toString().padStart(2, '0')}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getFullYear()}`;
};

onMounted(async () => {
  await fetchPromotion();
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
            <span v-if="promo.active" class="badge bg-success">Đang áp dụng</span>
            <span v-else class="badge bg-danger">Ngưng áp dụng</span>
          </td>
          <td>
            <button class="btn btn-sm btn-warning me-1" @click="editPromotion(promo)">
              Sửa
            </button>
            <button class="btn btn-sm btn-danger me-1" @click="changeStatus(promo.id)">
              Chuyển trạng thái
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
