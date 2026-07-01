<script setup>
import {onMounted, reactive, ref} from "vue";
import "vue-select/dist/vue-select.css";
import axios from "axios";

const showModal = ref(false);
const isEdit = ref(false);
const isViewOnly = ref(false);
const products = ref([]);

const receipts = ref([]);

const employeeId = ref(null);

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

const fetchReceipts = async () => {
  try {
    const response = await axios.get("http://localhost:8080/importReceipt/show");
    console.log("receipts:", response.data);
    receipts.value = response.data;
  } catch (error) {
    console.log("Lỗi", error);
  }
};

const form = reactive({
  id: null,
  employeeId: null,
  importReceiptCode: "",
  importDate: "",
  note: "",
  status: 0,
  details: [],
});

function generateImportReceiptCode() {
  const randomNumber = Math.floor(Math.random() * 100000000); // 8 số
  return "PN" + String(randomNumber).padStart(8, "0");
}

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

async function openEdit(receipt) {
  isEdit.value = receipt.status !== 2;
  isViewOnly.value = receipt.status === 2;
  Object.assign(form, JSON.parse(JSON.stringify(receipt)));

  try {
    const res = await axios.get(`http://localhost:8080/importReceiptDetail/showById/${receipt.id}`);
    // Chuẩn hoá field để v-select & tính tiền hoạt động ổn
    form.details = (res.data || []).map(x => ({
      id: x.id,
      importReceiptId: x.importReceiptId ?? x.import_receipt_id ?? receipt.id,
      productDetailId: x.productDetailId ?? x.product_detail_id ?? x.productDetail?.id ?? null,
      quantity: Number(x.quantity) || 0,
      unitPrice: Number(x.unitPrice) || 0,
    }));
  } catch (error) {
    console.error("Lỗi lấy chi tiết phiếu nhập:", error);
    form.details = [];
  }

  showModal.value = true;
}

// THAY THẾ HOÀN TOÀN hàm addDetail hiện tại
function addDetail() {
  if (!Array.isArray(form.details)) form.details = [];

  const n = form.details.length;
  if (n > 0) {
    const err = validateRow(form.details[n - 1]);
    if (err) {
      alert(`Dòng ${n}: ${err}`);
      return;
    }
  }

  form.details.push({
    id: null,
    importReceiptId: form.id ?? null,
    productDetailId: null,
    quantity: 1,
  });
}

function removeDetail(index) {
  const detail = form.details[index];

  // Nếu chi tiết đã có ID (đã lưu trong DB) thì gọi API xoá
  if (detail.id) {
    axios.delete(`http://localhost:8080/importReceiptDetail/delete/${detail.id}`)
        .then(() => {
          console.log(`Đã xoá chi tiết ID ${detail.id}`);
          form.details.splice(index, 1); // xoá khỏi form sau khi xoá DB
        })
        .catch(err => {
          console.error("Lỗi khi xoá chi tiết:", err);
        });
  } else {
    // Nếu chưa lưu DB thì chỉ cần xoá trên form
    form.details.splice(index, 1);
  }
}

// --- Helpers ---

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
  return null;
};


// Helpers
const isBlank = (v) =>
    v === null || v === undefined || (typeof v === "string" && v.trim() === "");

// ✅ Ghi chú (note) được phép để trống
async function saveReceipt() {
  // --- VALIDATION CƠ BẢN ---
  if (!Array.isArray(form.details) || form.details.length === 0) {
    alert("Vui lòng thêm ít nhất 1 dòng chi tiết.");
    return;
  }

  // Validate từng dòng & tính tổng
  let total = 0;
  for (let i = 0; i < form.details.length; i++) {
    const d = form.details[i];

    // tái dùng validateRow nếu có
    const err = validateRow(d);
    if (err) {
      alert(`Dòng #${i + 1}: ${err}`);
      return;
    }

    const q = Number(d.quantity) || 0;
    const up = Number(d.unitPrice) || 0;
    total += q * up;
  }
  form.totalAmount = total;

  try {
    let receiptRes;

    if (isEdit.value) {
      // Cập nhật phiếu
      receiptRes = await axios.put(
          `http://localhost:8080/importReceipt/update/${form.id}`,
          {
            employeeId: form.employeeId,
            importReceiptCode: String(form.importReceiptCode).trim(),
            importDate: form.importDate,
            note: form.note ?? "",
          }
      );
    } else {
      // Tạo phiếu mới
      receiptRes = await axios.post(
          `http://localhost:8080/importReceipt/create`,
          {
            employeeId: form.employeeId,
            importReceiptCode: String(form.importReceiptCode).trim(),
            importDate: form.importDate,
            note: form.note ?? "",
            totalAmount: form.totalAmount,
          }
      );
      form.id = receiptRes?.data?.id;
    }

    // Upsert tất cả dòng chi tiết
    const upserts = form.details.map(async (d) => {
      const payload = {
        importReceiptId: form.id,
        productDetailId: d.productDetailId,
        quantity: Number(d.quantity) || 0,
        unitPrice: Number(d.unitPrice) || 0,
        totalPrice: (Number(d.quantity) || 0) * (Number(d.unitPrice) || 0),
      };
      if (d.id) {
        await axios.put(`http://localhost:8080/importReceiptDetail/update/${d.id}`, payload);
      } else {
        const res = await axios.post(`http://localhost:8080/importReceiptDetail/create`, payload);
        d.id = res?.data?.id; // lưu lại id mới
      }
    });
    await Promise.all(upserts);

    await fetchReceipts();
    closeModal();
  } catch (error) {
    console.error("Lỗi khi lưu phiếu:", error);
    alert("Đã xảy ra lỗi khi lưu phiếu!");
  }
}


function closeModal() {
  showModal.value = false;
}

async function confirm(receipt) {
  try {
    // Cập nhật trạng thái trong form hiện tại
    receipt.status = 1;

    // Gửi yêu cầu cập nhật lên server
    await axios.put(`http://localhost:8080/importReceipt/updateStatus/${receipt.id}`, {
      status: 1
    });

    console.log("✅ Đã xác nhận phiếu nhập:", receipt.id);

    // Làm mới lại danh sách (nếu cần)
    await fetchReceipts();
  } catch (error) {
    console.error("❌ Lỗi khi xác nhận phiếu:", error);
    alert("Đã xảy ra lỗi khi xác nhận phiếu!");
  }
}

async function complete(receipt) {
  try {
    // 1. Lấy chi tiết phiếu nhập
    const res = await axios.get(`http://localhost:8080/importReceiptDetail/showById/${receipt.id}`);
    const details = res.data;
    console.log("danh sách phiếu nhập: ", details);
    for (const detail of details) {
      // 2. Kiểm tra xem sản phẩm đã tồn tại trong kho chưa
      console.log("📦 Kiểm tra tồn kho cho ProductDetail ID:", detail.productDetailId);
      const checkRes = await axios.get(`http://localhost:8080/inventory/check/${detail.productDetailId}`);

      console.log("✅ Kết quả check:", checkRes.data);

      if (checkRes.data.exists) {
        // 3a. Nếu có → cập nhật số lượng
        await axios.put(`http://localhost:8080/inventory/updateQuantityCong/${detail.productDetailId}`, {
          quantity: detail.quantity
        });
      } else {
        // 3b. Nếu chưa có → tạo mới bản ghi kho
        await axios.post(`http://localhost:8080/inventory/create`, {
          productDetailId: detail.productDetailId,
          quantity: detail.quantity,
          modifiedDate: new Date().toISOString() // nếu muốn gửi
        });
      }
    }

    // 4. Cập nhật trạng thái phiếu nhập
    await axios.put(`http://localhost:8080/importReceipt/updateStatus/${receipt.id}`, {
      status: 2
    });

    receipt.status = 2;
    await fetchReceipts();
    alert("✅ Ghi nhận kho thành công!");
  } catch (err) {
    console.error("❌ Lỗi ghi nhận kho:", err);
    alert("❌ Ghi nhận kho thất bại!");
  }
}

async function cancel(receipt) {
  try {
    // Cập nhật trạng thái trong form hiện tại
    receipt.status = 3;

    // Gửi yêu cầu cập nhật lên server
    await axios.put(`http://localhost:8080/importReceipt/updateStatus/${receipt.id}`, {
      status: 3
    });

    console.log("✅ Đã xác nhận phiếu nhập:", receipt.id);

    // Làm mới lại danh sách (nếu cần)
    await fetchReceipts();
  } catch (error) {
    console.error("❌ Lỗi khi xác nhận phiếu:", error);
    alert("Đã xảy ra lỗi khi xác nhận phiếu!");
  }
}

function formatDate(d) {
  return new Date(d).toLocaleString();
}

function formatCurrency(val) {
  return Number(val).toLocaleString() + " đ";
}

function statusText(s) {
  return ["Đang tạo", "Đã xác nhận", "Đã nhập kho", "Đã huỷ"][s];
}

function statusClass(s) {
  return ["bg-secondary", "bg-warning text-dark", "bg-success", "bg-danger"][s];
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
  fetchReceipts();
  fetchProductDetail();
});
</script>
<template>
  <div class="container mt-4 ">
    <h3 class="text-center">Quản lý phiếu nhập</h3>
    <button class="btn btn-primary mb-3" @click="openCreate">
      + Thêm phiếu
    </button>

    <!-- Bảng phiếu nhập -->
    <table class="table table-bordered table-striped">
      <thead>
      <tr>
        <th>Mã phiếu</th>
        <th>Ngày nhập</th>
        <th>Trạng thái</th>
        <th>Hành động</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="receipt in receipts" :key="receipt.id">
        <td>{{ receipt.importReceiptCode }}</td>
        <td>{{ formatDate(receipt.importDate) }}</td>
        <td>
            <span class="badge" :class="statusClass(receipt.status)">{{
                statusText(receipt.status)
              }}</span>
        </td>
        <td>
          <button v-if="receipt.status === 0" class="btn btn-sm btn-secondary me-1" @click="openEdit(receipt)">
            Sửa
          </button>
          <button v-if="receipt.status === 0" class="btn btn-sm btn-warning me-1" @click="confirm(receipt)">
            Xác nhận
          </button>
          <button v-if="receipt.status === 1" class="btn btn-sm btn-success me-1" @click="complete(receipt)">
            Ghi nhận kho
          </button>
          <button v-if="receipt.status === 2" class="btn btn-sm btn-success me-1" @click="openEdit(receipt)">
            Chi tiết
          </button>
          <button v-if="[0, 1].includes(receipt.status)" class="btn btn-sm btn-danger" @click="cancel(receipt)">
            Huỷ
          </button>
        </td>
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
              <input v-model="form.importReceiptCode" class="form-control bg-light" readonly/>
            </div>
            <div class="mb-3">
              <label>Ngày nhập</label>
              <input v-model="form.importDate" type="datetime-local" class="form-control" :readonly="isViewOnly"
                     :disabled="isViewOnly"/>
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
                              class="w-100" placeholder="Chọn sản phẩm" :disabled="isViewOnly" required/>
                  </div>
                </td>
                <td>
                  <input type="number" v-model.number="item.quantity" class="form-control form-control-sm"
                         :readonly="isViewOnly" min="1" step="1" @keydown="preventInvalidNumber"
                         @blur="onQuantityBlur(item)"/>
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
.modal-backdrop {
  z-index: 1040;
}

.modal {
  z-index: 1050;
}

.modal.show {
  display: block;
}

.table td,
.table th {
  vertical-align: middle;
}

.v-select {
  --vs-border: none;
  font-size: 0.875rem;
}

.v-select .vs__dropdown-toggle {
  height: 38px;
  padding: 0 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 0.375rem;
  background-color: #fff;
  box-shadow: none;
}

.v-select .vs__selected {
  line-height: 38px;
}

.v-select .vs__actions {
  align-items: center;
}

.v-select .vs__dropdown-menu {
  z-index: 9999;
}
</style>