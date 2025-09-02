<script setup>
import { ref, computed, onMounted, reactive } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";
const inventoryList = ref([]);
const searchQuery = ref("");
const showModal = ref(false);
const isEdit = ref(false);
const isViewOnly = ref(false);
const products = ref([]);
import axios from "axios";

const employeeId = ref(null);
// Lọc theo tên sản phẩm
const filteredInventory = computed(() => {
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return inventoryList.value;
  return inventoryList.value.filter((item) =>
    String(item?.productName ?? "")
      .toLowerCase()
      .includes(q)
  );
});


function closeModal() {
  showModal.value = false;
}


function formatCurrency(val) {
  return Number(val).toLocaleString() + " đ";
}

function removeDetail(index) {
  const detail = form.details[index];

  // CHẾ ĐỘ CHỈ THÊM: không cho xoá dòng đã lưu trong DB
  if (detail.id) {
    alert("Chế độ chỉ thêm: không được xoá dòng đã lưu.");
    return;
  }
  // Nếu chưa lưu DB thì chỉ cần xoá trên form
  form.details.splice(index, 1);
}

const toNum = (v) => (v === "" || v === null || v === undefined ? NaN : Number(v));

// Chặn nhập ký tự không phải số cho input type="number"
const preventInvalidNumber = (e) => {
  const invalidKeys = ["e", "E", "+", "-"];
  if (invalidKeys.includes(e.key)) e.preventDefault();
};

// Sửa về giá trị hợp lệ khi rời ô (blur)
const onQuantityBlur = (item) => {
  const q = Math.floor(toNum(item.quantity));
  item.quantity = Number.isFinite(q) && q > 0 ? q : 1; // min = 1
};
const onUnitPriceBlur = (item) => {
  const up = toNum(item.unitPrice);
  item.unitPrice = Number.isFinite(up) && up >= 0 ? up : 0; // min = 0
};

// Validate 1 dòng; trả về chuỗi lỗi (null nếu OK)
const validateRow = (d) => {
  if (isBlank(d.productDetailId)) return "Chưa chọn sản phẩm.";
  const q = toNum(d.quantity);
  if (!Number.isFinite(q) || q <= 0) return "Số lượng phải > 0.";
  const up = toNum(d.unitPrice);
  if (!Number.isFinite(up) || up < 0) return "Đơn giá không hợp lệ.";
  return null;
};



// Helpers
const isBlank = (v) =>
  v === null || v === undefined || (typeof v === "string" && v.trim() === "");

// ✅ Ghi chú (note) được phép để trống
async function saveReceipt() {
  if (!Array.isArray(form.details) || form.details.length === 0) {
    alert("Vui lòng thêm ít nhất 1 dòng chi tiết.");
    return;
  }

  // Validate & tính tổng (tổng này chỉ gửi khi TẠO MỚI header)
  let computedTotal = 0;
  for (let i = 0; i < form.details.length; i++) {
    const d = form.details[i];
    if (isBlank(d.productDetailId)) {
      alert(`Dòng #${i + 1}: Chưa chọn sản phẩm.`);
      return;
    }
    const q = toNum(d.quantity);
    const up = toNum(d.unitPrice); // nếu bỏ đơn giá, up sẽ là 0
    if (!Number.isFinite(q) || q <= 0) {
      alert(`Dòng #${i + 1}: Số lượng phải > 0.`);
      return;
    }
    computedTotal += q * up;
  }

  try {
    // 1) Tạo header nếu chưa có (CHỈ POST)
    if (!form.id) {
      const res = await axios.post(`http://localhost:8080/importReceipt/create`, {
        employeeId: form.employeeId,
        importReceiptCode: String(form.importReceiptCode).trim(),
        importDate: form.importDate,
        note: form.note ?? "",
        totalAmount: computedTotal, // tổng hiện tại; không PUT cập nhật sau này
      });
      form.id = res?.data?.id;
      if (!form.id) throw new Error("Không nhận được ID phiếu sau khi tạo.");
    }
    // Nếu đã có form.id (phiếu cũ) → KHÔNG cập nhật header (không PUT)

    // 2) CHỈ POST các chi tiết CHƯA có id
    for (const detail of form.details) {
      if (!detail.id) {
        const q = Number(detail.quantity);
        const up = Number(detail.unitPrice ?? 0);
        const created = await axios.post(
          `http://localhost:8080/importReceiptDetail/create`,
          {
            importReceiptId: form.id,
            productDetailId: detail.productDetailId,
            quantity: q,
            unitPrice: up,
            totalPrice: q * up,
          }
        );
        detail.id = created?.data?.id ?? null; // lưu lại id để biết đã được thêm
      }
      // Nếu detail.id đã tồn tại → BỎ QUA (không PUT cập nhật)
    }

    alert("Lưu thành công (chỉ thêm mới).");
    closeModal();
  } catch (error) {
    console.error("Lỗi khi lưu phiếu:", error);
    alert("Đã xảy ra lỗi khi lưu phiếu!");
  }
}
const fetchProductDetail = async () => {
  try {
    const response = await axios.get('http://localhost:8080/productDetail/showProductReceipt')
    products.value = response.data.map(item => {
      return {
        id: item.productDetailId,
        name: `Giày ${item.productName} - Size ${item.size} - ${item.color}`
      }
    })
  } catch (error) {
    console.log("Lỗi", error);
  }
};
function addDetail() {
  if (!Array.isArray(form.details)) form.details = [];

  // Kiểm tra dòng cuối (nếu có) trước khi thêm dòng mới
  const n = form.details.length;
  if (n > 0) {
    const last = form.details[n - 1];
    const err = validateRow(last);
    if (err) {
      alert(`Dòng ${n}: ${err}`);
      return;
    }
  }

  // Chỉ push vào form; không POST chi tiết ở đây
  form.details.push({
    id: null,
    importReceiptId: form.id || null,
    productDetailId: null,
    quantity: 1,
    unitPrice: 0,      // nếu không dùng đơn giá thì cứ để 0
  });
}
// Lấy dữ liệu tồn kho
async function fetchInventory() {
  try {
    const res = await fetch("http://localhost:8080/inventory/show");
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    inventoryList.value = Array.isArray(data) ? data : [];
  } catch (error) {
    console.error("Lỗi khi lấy dữ liệu tồn kho:", error);
  }
}

// Format ngày
function formatDate(dateString) {
  const date = new Date(dateString);
  return isNaN(date) ? "" : date.toLocaleString("vi-VN");
}
function generateImportReceiptCode() {
  const randomNumber = Math.floor(Math.random() * 100000000); // 8 số
  return "PN" + String(randomNumber).padStart(8, "0");
}
const form = reactive({
  id: null,
  employeeId: null,
  importReceiptCode: "",
  importDate: "",
  note: "",
  status: 0,
  details: [],
});
function openCreate() {
  isEdit.value = false;
  Object.assign(form, {
    id: null,
    employeeId: employeeId.value,
    importReceiptCode: generateImportReceiptCode(),
    importDate: new Date().toISOString().slice(0, 16),
    note: "",
    status: 0,
    details: [],
  });
  showModal.value = true;
}
onMounted(() => {
  const userJson = localStorage.getItem("user");
  if (userJson) {
    try {
      const user = JSON.parse(userJson);
      employeeId.value = user.employeeId;
      form.employeeId = employeeId.value; // ✅ gán đúng vào form
      console.log("✅ Employee ID:", employeeId.value);
      console.log("🎯 form.employeeId khi khởi tạo:", form.employeeId);
    } catch (error) {
      console.error("❌ Lỗi khi parse userJson:", error);
    }
  } else {
    console.warn("⚠️ Chưa đăng nhập hoặc thiếu thông tin user");
  }
  fetchInventory();
  fetchProductDetail();
});
</script>
<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-center">Quản lý Tồn Kho</h2>
    <button class="btn btn-primary mb-3" @click="openCreate">
      + Thêm phiếu nhập kho
    </button>
    <!-- Tìm kiếm -->
    <div class="mb-3">
      <input type="text" v-model="searchQuery" class="form-control" placeholder="Tìm kiếm theo tên sản phẩm..." />
    </div>

    <!-- Bảng tồn kho -->
    <table class="table table-bordered table-hover">
      <thead class="thead-dark">
        <tr>
          <th>STT</th>
          <th>Tên sản phẩm</th>
          <th>Màu sắc</th>
          <th>Kích cỡ</th>
          <th>Số lượng tồn</th>
          <th>Giá nhập</th>
          <th>Ngày cập nhật</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in filteredInventory" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ item.productName }}</td>
          <td>{{ item.color }}</td>
          <td>{{ item.size }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ item.unitPrice }}</td>
          <td>{{ formatDate(item.modifiedDate) }}</td>
        </tr>
        <tr v-if="filteredInventory.length === 0">
          <td colspan="5" class="text-center text-muted">Không có kết quả phù hợp</td>
        </tr>
      </tbody>
    </table>


    <!-- Modal -->
    <div class="modal fade show d-block" tabindex="-1" v-if="showModal" style="background-color: rgba(0, 0, 0, 0.5)">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title w-100 text-center">
              {{ isEdit ? "Sửa phiếu" : "Thêm phiếu" }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label>Mã phiếu</label>
              <input v-model="form.importReceiptCode" class="form-control bg-light" readonly />
            </div>
            <div class="mb-3">
              <label>Ngày nhập</label>
              <input v-model="form.importDate" type="datetime-local" class="form-control" :readonly="isViewOnly"
                :disabled="isViewOnly" />
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="form.note" class="form-control" :readonly="isViewOnly"
                :disabled="isViewOnly"></textarea>
            </div>

            <!-- Chi tiết sản phẩm -->
            <h5>Chi tiết sản phẩm</h5>
            <table class="table table-sm table-bordered">
              <thead>
                <tr>
                  <th>ID sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in form.details" :key="idx">
                  <td style="min-width: 250px;">
                    <div class="form-control p-0">
                      <v-select v-model="item.productDetailId" :options="products" label="name" :reduce="p => p.id"
                        class="w-100" placeholder="Chọn sản phẩm" :disabled="isViewOnly" required />
                    </div>
                  </td>
                  <td>
                    <input type="number" v-model.number="item.quantity" class="form-control form-control-sm"
                      :readonly="isViewOnly" min="1" step="1" @keydown="preventInvalidNumber"
                      @blur="onQuantityBlur(item)" />
                  </td>
                  <td>
                    <button class="btn btn-sm btn-danger" @click="removeDetail(idx)" v-if="!isViewOnly">
                      Xoá
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <button class="btn btn-sm btn-outline-primary" @click="addDetail" v-if="!isViewOnly" required>
              + Thêm dòng
            </button>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button class="btn btn-primary" @click="saveReceipt" v-if="!isViewOnly">Lưu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.table th {
  background-color: #343a40;
  color: #fff;
  text-align: center;
}

.table td {
  vertical-align: middle;
}
</style>
