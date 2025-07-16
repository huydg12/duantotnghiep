<template>
  <div class="container mt-5">
    <h2 class="mb-4">Quản lý Hóa Đơn</h2>

    <table class="table table-bordered table-hover">
      <thead class="thead-dark">
        <tr>
          <th>Mã HĐ</th>
          <th>Ngày tạo</th>
          <th>Người nhận</th>
          <th>Trạng thái</th>
          <th>Thanh toán</th>
          <th>Tổng tiền</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="bill in bills" :key="bill.ID">
          <td>{{ bill.CODE }}</td>
          <td>{{ bill.CREATED_DATE }}</td>
          <td>{{ bill.RECIPIENT_NAME }}</td>
          <td>{{ bill.STATUS }}</td>
          <td>{{ bill.STATUS_PAYMENT }}</td>
          <td>{{ formatCurrency(bill.GRAND_TOTAL) }}</td>
          <td>
            <button class="btn btn-sm btn-primary me-2" @click="openModal(bill)">Chi tiết</button>
            <button class="btn btn-sm btn-danger" @click="deleteBill(bill.ID)">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Modal -->
    <div
      class="modal fade"
      id="billModal"
      tabindex="-1"
      aria-labelledby="billModalLabel"
      aria-hidden="true"
      ref="modal"
    >
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="billModalLabel">Chi tiết Hóa Đơn</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body" v-if="selectedBill">
            <form @submit.prevent="saveChanges">
              <div class="row">
                <div class="col-md-6">
                  <label class="form-label">Mã hóa đơn</label>
                  <input v-model="selectedBill.CODE" class="form-control" :readonly="isPaid" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Người nhận</label>
                  <input v-model="selectedBill.RECIPIENT_NAME" class="form-control" :readonly="isPaid" />
                </div>
              </div>
              <div class="row mt-2">
                <div class="col-md-6">
                  <label class="form-label">SĐT</label>
                  <input v-model="selectedBill.RECIPIENT_PHONE_NUMBER" class="form-control" :readonly="isPaid" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Địa chỉ</label>
                  <input v-model="selectedBill.RECEIVER_ADDRESS" class="form-control" :readonly="isPaid" />
                </div>
              </div>
              <div class="mt-3">
                <label>Ghi chú</label>
                <textarea v-model="selectedBill.NOTE" class="form-control" :readonly="isPaid"></textarea>
              </div>

              <h6 class="mt-4">Chi tiết sản phẩm</h6>
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>Ảnh</th>
                    <th>Tên SP</th>
                    <th>Kích cỡ</th>
                    <th>SL</th>
                    <th>Giá</th>
                    <th>Tổng</th>
                    <th v-if="!isPaid">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(detail, index) in billDetails" :key="detail.ID">
                    <td><img :src="detail.PRODUCT_IMAGE" width="50" /></td>
                    <td>{{ detail.PRODUCT_NAME }}</td>
                    <td>{{ detail.SIZE }}</td>
                    <td>{{ detail.QUANTITY }}</td>
                    <td>{{ formatCurrency(detail.PRICE) }}</td>
                    <td>{{ formatCurrency(detail.PRICE * detail.QUANTITY) }}</td>
                    <td v-if="!isPaid">
                      <button class="btn btn-sm btn-danger" @click.prevent="removeDetail(index)">Xóa</button>
                    </td>
                  </tr>
                </tbody>
              </table>

              <div class="mt-3">
                <h6>Tổng kết</h6>
                <ul class="list-group">
                  <li class="list-group-item d-flex justify-content-between">
                    <span>Tiền hàng:</span>
                    <strong>{{ formatCurrency(subTotal) }}</strong>
                  </li>
                  <li class="list-group-item d-flex justify-content-between">
                    <span>Giảm giá:</span>
                    <strong>{{ formatCurrency(selectedBill.DISCOUNT_AMOUNT || 0) }}</strong>
                  </li>
                  <li class="list-group-item d-flex justify-content-between">
                    <span>Phí vận chuyển:</span>
                    <strong>{{ formatCurrency(selectedBill.SHIPPING_FEE || 0) }}</strong>
                  </li>
                  <li class="list-group-item d-flex justify-content-between bg-light">
                    <span><strong>Tổng cộng:</strong></span>
                    <strong>{{ formatCurrency(grandTotal) }}</strong>
                  </li>
                </ul>
              </div>
            </form>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button class="btn btn-success" @click="saveChanges" :disabled="isPaid">Lưu</button>
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
      bills: [],
      selectedBill: null,
      billDetails: [],
      modalInstance: null,
    };
  },
  computed: {
    subTotal() {
      return this.billDetails.reduce((total, item) => total + item.QUANTITY * item.PRICE, 0);
    },
    grandTotal() {
      return (
        this.subTotal -
        (parseFloat(this.selectedBill.DISCOUNT_AMOUNT) || 0) +
        (parseFloat(this.selectedBill.SHIPPING_FEE) || 0)
      );
    },
    isPaid() {
      return this.selectedBill?.STATUS_PAYMENT === "Đã thanh toán";
    },
  },
  methods: {
    async fetchBills() {
      this.bills = [
        {
          ID: 1,
          CODE: "HD001",
          CREATED_DATE: "2025-07-01",
          RECIPIENT_NAME: "Nguyễn Văn A",
          RECIPIENT_PHONE_NUMBER: "0909123456",
          RECEIVER_ADDRESS: "123 Nguyễn Trãi",
          STATUS: "Đã giao hàng",
          STATUS_PAYMENT: "Đã thanh toán",
          GRAND_TOTAL: 1050000,
          SUB_TOTAL: 1200000,
          DISCOUNT_AMOUNT: 200000,
          SHIPPING_FEE: 50000,
          NOTE: "Giao giờ hành chính",
        },
        {
          ID: 2,
          CODE: "HD002",
          CREATED_DATE: "2025-07-10",
          RECIPIENT_NAME: "Trần Thị B",
          RECIPIENT_PHONE_NUMBER: "0912345678",
          RECEIVER_ADDRESS: "456 Lê Lợi",
          STATUS: "Chờ giao hàng",
          STATUS_PAYMENT: "Chưa thanh toán",
          GRAND_TOTAL: 700000,
          SUB_TOTAL: 700000,
          DISCOUNT_AMOUNT: 0,
          SHIPPING_FEE: 0,
          NOTE: "",
        },
      ];
    },
    async fetchBillDetails(billId) {
      if (billId === 1) {
        this.billDetails = [
          {
            ID: 1,
            PRODUCT_NAME: "Áo thun Polo",
            SIZE: "M",
            QUANTITY: 2,
            PRICE: 200000,
            PRODUCT_IMAGE: "https://via.placeholder.com/50",
          },
          {
            ID: 2,
            PRODUCT_NAME: "Quần Jeans",
            SIZE: "L",
            QUANTITY: 1,
            PRICE: 800000,
            PRODUCT_IMAGE: "https://via.placeholder.com/50",
          },
        ];
      } else {
        this.billDetails = [
          {
            ID: 3,
            PRODUCT_NAME: "Áo sơ mi trắng",
            SIZE: "M",
            QUANTITY: 1,
            PRICE: 400000,
            PRODUCT_IMAGE: "https://via.placeholder.com/50",
          },
          {
            ID: 4,
            PRODUCT_NAME: "Chân váy",
            SIZE: "S",
            QUANTITY: 1,
            PRICE: 300000,
            PRODUCT_IMAGE: "https://via.placeholder.com/50",
          },
        ];
      }
    },
    openModal(bill) {
      this.selectedBill = { ...bill };
      this.fetchBillDetails(bill.ID);
      this.$nextTick(() => {
        this.modalInstance = new Modal(this.$refs.modal);
        this.modalInstance.show();
      });
    },
    saveChanges() {
      if (this.isPaid) {
        alert("Hóa đơn đã thanh toán không thể chỉnh sửa.");
        return;
      }
      this.selectedBill.SUB_TOTAL = this.subTotal;
      this.selectedBill.GRAND_TOTAL = this.grandTotal;
      alert("Đã lưu thay đổi cho hóa đơn: " + this.selectedBill.CODE);
      this.modalInstance.hide();
    },
    deleteBill(id) {
      if (confirm("Bạn có chắc muốn xóa hóa đơn này không?")) {
        this.bills = this.bills.filter(b => b.ID !== id);
        alert("Đã xóa hóa đơn.");
        if (this.modalInstance) this.modalInstance.hide();
      }
    },
    removeDetail(index) {
      this.billDetails.splice(index, 1);
    },
    formatCurrency(value) {
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(value);
    },
  },
  mounted() {
    this.fetchBills();
  },
};
</script>

<style scoped>
img {
  border-radius: 4px;
}
</style>
