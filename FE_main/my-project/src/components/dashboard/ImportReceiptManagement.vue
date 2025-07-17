<script setup>
import { reactive, ref } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

const showModal = ref(false);
const isEdit = ref(false);

const products = ref([
  { id: 1, name: "Giày Nike Air Max - Size 42 - Đỏ" },
  { id: 2, name: "Giày Adidas UltraBoost - Size 40 - Trắng" },
  { id: 3, name: "Giày Converse Classic - Size 41 - Đen" },
  { id: 4, name: "Giày Vans Old Skool - Size 43 - Xanh" },
]);

const receipts = ref([
  {
    id: 1,
    importReceiptCode: "PN001",
    importDate: "2025-07-15T10:00",
    totalAmount: 1500000,
    note: "Nhập lô hàng A",
    status: 0,
    details: [
      { productDetailId: 101, quantity: 10, unitPrice: 50000 },
      { productDetailId: 102, quantity: 5, unitPrice: 100000 },
    ],
  },
]);

const form = reactive({
  id: null,
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
    importReceiptCode: "",
    importDate: new Date().toISOString().slice(0, 16),
    note: "",
    status: 0,
    details: [],
  });
  showModal.value = true;
}

function openEdit(receipt) {
  isEdit.value = true;
  Object.assign(form, JSON.parse(JSON.stringify(receipt)));
  showModal.value = true;
}

function addDetail() {
  form.details.push({ productDetailId: "", quantity: 1, unitPrice: 0 });
}

function removeDetail(index) {
  form.details.splice(index, 1);
}

function saveReceipt() {
  form.totalAmount = form.details.reduce(
    (sum, d) => sum + d.quantity * d.unitPrice,
    0
  );
  if (isEdit.value) {
    const index = receipts.value.findIndex((r) => r.id === form.id);
    if (index !== -1) receipts.value[index] = { ...form };
  } else {
    form.id = Date.now();
    receipts.value.push({ ...form });
  }
  closeModal();
}

function closeModal() {
  showModal.value = false;
}

function confirm(receipt) {
  receipt.status = 1;
}
function complete(receipt) {
  receipt.status = 2;
}
function cancel(receipt) {
  receipt.status = 3;
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
          <th>Tổng tiền</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="receipt in receipts" :key="receipt.id">
          <td>{{ receipt.importReceiptCode }}</td>
          <td>{{ formatDate(receipt.importDate) }}</td>
          <td>{{ formatCurrency(receipt.totalAmount) }}</td>
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
              <input v-model="form.importReceiptCode" class="form-control" />
            </div>
            <div class="mb-3">
              <label>Ngày nhập</label>
              <input v-model="form.importDate" type="datetime-local" class="form-control" />
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="form.note" class="form-control"></textarea>
            </div>

            <!-- Chi tiết sản phẩm -->
            <h5>Chi tiết sản phẩm</h5>
            <table class="table table-sm table-bordered">
              <thead>
                <tr>
                  <th>ID sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Thành tiền</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in form.details" :key="idx">
                  <td style="min-width: 250px;">
                    <div class="form-control p-0">
                      <v-select v-model="item.productDetailId" :options="products" label="name" :reduce="p => p.id"
                        class="w-100" placeholder="Chọn sản phẩm" />
                    </div>
                  </td>
                  <td>
                    <input type="number" v-model.number="item.quantity" class="form-control form-control-sm" />
                  </td>
                  <td>
                    <input type="number" v-model.number="item.unitPrice" class="form-control form-control-sm" />
                  </td>
                  <td>{{ formatCurrency(item.quantity * item.unitPrice) }}</td>
                  <td>
                    <button class="btn btn-sm btn-danger" @click="removeDetail(idx)">
                      Xoá
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <button class="btn btn-sm btn-outline-primary" @click="addDetail">
              + Thêm dòng
            </button>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Hủy</button>
            <button class="btn btn-primary" @click="saveReceipt">Lưu</button>
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