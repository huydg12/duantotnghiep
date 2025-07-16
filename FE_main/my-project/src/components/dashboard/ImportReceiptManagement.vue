<template>
  <div class="container mt-4">
    <h3 class="mb-3">Quản lý Phiếu Nhập</h3>

    <!-- Danh sách phiếu nhập -->
    <table class="table table-bordered">
      <thead>
        <tr>
          <th>Mã phiếu</th>
          <th>Nhân viên</th>
          <th>Ngày nhập</th>
          <th>Tổng tiền</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="receipt in receipts" :key="receipt.ID">
          <td>{{ receipt.IMPORT_RECEIPT_CODE }}</td>
          <td>{{ receipt.EMPLOYEE_ID }}</td>
          <td>{{ receipt.IMPORT_DATE }}</td>
          <td>{{ formatCurrency(receipt.TOTAL_AMOUNT) }}</td>
          <td>{{ getStatusText(receipt.STATUS) }}</td>
          <td>
            <button class="btn btn-primary btn-sm me-2" @click="openReceipt(receipt)">Chi tiết</button>
            <button class="btn btn-danger btn-sm" @click="deleteReceipt(receipt.ID)">Xoá</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Modal phiếu nhập -->
    <div class="modal fade" id="receiptModal" tabindex="-1" ref="receiptModal">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết Phiếu Nhập</h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <form @submit.prevent="saveReceipt">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">Mã phiếu</label>
                  <input v-model="currentReceipt.IMPORT_RECEIPT_CODE" class="form-control" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Nhân viên ID</label>
                  <input v-model="currentReceipt.EMPLOYEE_ID" class="form-control" type="number" />
                </div>
              </div>

              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">Ngày nhập</label>
                  <input v-model="currentReceipt.IMPORT_DATE" class="form-control" type="datetime-local" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Trạng thái</label>
                  <select v-model="currentReceipt.STATUS" class="form-select">
                    <option value="0">Đang nhập</option>
                    <option value="1">Đã nhập</option>
                    <option value="2">Hủy</option>
                  </select>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Ghi chú</label>
                <textarea v-model="currentReceipt.NOTE" class="form-control"></textarea>
              </div>

              <h6>Chi tiết sản phẩm</h6>
              <table class="table table-sm table-bordered">
                <thead>
                  <tr>
                    <th>Mã SP</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Tổng</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(detail, idx) in receiptDetails" :key="idx">
                    <td>
                      <input v-model="detail.PRODUCT_DETAIL_ID" class="form-control form-control-sm" />
                    </td>
                    <td>
                      <input v-model.number="detail.QUANTITY" class="form-control form-control-sm" type="number" min="1" />
                    </td>
                    <td>
                      <input v-model.number="detail.UNIT_PRICE" class="form-control form-control-sm" type="number" min="0" />
                    </td>
                    <td>{{ formatCurrency(detail.QUANTITY * detail.UNIT_PRICE) }}</td>
                    <td>
                      <button class="btn btn-danger btn-sm" @click.prevent="removeDetail(idx)">Xóa</button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <button class="btn btn-outline-primary btn-sm mb-3" @click.prevent="addDetail">+ Thêm dòng</button>

              <div class="d-flex justify-content-between">
                <strong>Tổng tiền:</strong>
                <span>{{ formatCurrency(totalAmount) }}</span>
              </div>
            </form>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button class="btn btn-success" @click="saveReceipt">Lưu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Modal } from 'bootstrap';

export default {
  data() {
    return {
      receipts: [],
      receiptDetails: [],
      currentReceipt: {},
      modalInstance: null,
    };
  },
  computed: {
    totalAmount() {
      return this.receiptDetails.reduce((sum, d) => sum + d.QUANTITY * d.UNIT_PRICE, 0);
    },
  },
  methods: {
    async fetchReceipts() {
      // Giả lập dữ liệu
      this.receipts = [
        {
          ID: 1,
          IMPORT_RECEIPT_CODE: 'PN001',
          EMPLOYEE_ID: 101,
          IMPORT_DATE: '2025-07-01T09:00',
          TOTAL_AMOUNT: 500000,
          NOTE: 'Nhập lô hàng mới',
          STATUS: 0,
        },
      ];
    },
    openReceipt(receipt) {
      this.currentReceipt = { ...receipt };
      this.receiptDetails = [
        {
          PRODUCT_DETAIL_ID: 1,
          QUANTITY: 10,
          UNIT_PRICE: 20000,
        },
      ];
      this.$nextTick(() => {
        this.modalInstance = new Modal(this.$refs.receiptModal);
        this.modalInstance.show();
      });
    },
    addDetail() {
      this.receiptDetails.push({
        PRODUCT_DETAIL_ID: '',
        QUANTITY: 1,
        UNIT_PRICE: 0,
      });
    },
    removeDetail(index) {
      this.receiptDetails.splice(index, 1);
    },
    saveReceipt() {
      this.currentReceipt.TOTAL_AMOUNT = this.totalAmount;
      alert('Đã lưu phiếu nhập');
      this.modalInstance.hide();
    },
    deleteReceipt(id) {
      if (confirm('Xác nhận xóa phiếu nhập?')) {
        this.receipts = this.receipts.filter(r => r.ID !== id);
      }
    },
    getStatusText(code) {
      return ['Đang nhập', 'Đã nhập', 'Hủy'][code] || 'Không rõ';
    },
    formatCurrency(v) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(v);
    },
  },
  mounted() {
    this.fetchReceipts();
  },
};
</script>

<style scoped>
input,
select,
textarea {
  font-size: 0.9rem;
}
</style>
