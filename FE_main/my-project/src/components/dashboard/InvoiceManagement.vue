<script setup>
import axios from "axios";
import { ref, computed, onMounted, nextTick } from "vue";
import { Modal } from "bootstrap";

const bills = ref([]);
const selectedBill = ref(null);
const billDetails = ref([]);
const modalInstance = ref(null);
const modal = ref(null);
const editingDetail = ref(null);
const editIndex = ref(null);
const editQuantity = ref(1);

const mapBillData = (data) => {
  return data.map((item) => ({
    ID: item.id,
    CODE: item.code,
    CREATED_DATE: item.createdDate,
    RECIPIENT_NAME: item.recipientName,
    RECIPIENT_PHONE_NUMBER: item.recipientPhoneNumber,
    RECEIVER_ADDRESS: item.receiverAddress,
    STATUS: item.status,
    STATUS_PAYMENT: item.statusPayment,
    GRAND_TOTAL: item.grandTotal,
    SUB_TOTAL: item.subTotal,
    DISCOUNT_AMOUNT: item.discountAmount,
    SHIPPING_FEE: item.shippingFee,
    NOTE: item.note,
  }));
};

const fetchBills = async () => {
  try {
    const response = await axios.get("http://localhost:8080/bill/show");
    console.log("Bills:", response.data);
    bills.value = mapBillData(response.data);
  } catch (error) {
    console.log("Lỗi lấy hoá đơn", error);
  }
};

const mapBillDetailData = (data) => {
  return data.map((item) => ({
    ID: item.id,
    PRODUCT_NAME: item.PRODUCT_NAME || item.productName,
    SIZE: item.SIZE || item.size,
    QUANTITY: item.QUANTITY || item.quantity,
    PRICE: item.PRICE || item.price,
    PRODUCT_IMAGE: item.PRODUCT_IMAGE || item.productImage,
  }));
};

const fetchBillDetails = async (billId) => {
  try {
    const response = await axios.get(
      `http://localhost:8080/billDetail/show/${billId}`
    );
    billDetails.value = mapBillDetailData(response.data);
    console.log("Bill Details:", billDetails.value);
  } catch (error) {
    console.log("Lỗi lấy chi tiết hoá đơn", error);
  }
};

const openModal = async (bill) => {
  selectedBill.value = { ...bill };
  await fetchBillDetails(bill.ID);
  await nextTick();
  if (!modalInstance.value) {
    modalInstance.value = new Modal(modal.value);
  }
  modalInstance.value.show();
};

const saveChanges = async () => {
  if (isPaid.value) {
    alert("Hoá đơn đã thanh toán không thể chỉnh sửa.");
    return;
  }

  try {
    for (let detail of billDetails.value) {
      await updateBillDetail(detail);
    }

    selectedBill.value.SUB_TOTAL = subTotal.value;
    selectedBill.value.GRAND_TOTAL = grandTotal.value;

    await axios.put(`http://localhost:8080/bill/update/${selectedBill.value.ID}`, selectedBill.value);

    await fetchBills();
    await fetchBillDetails(selectedBill.value.ID);
    console.log("Bill details sau khi lưu: ", billDetails.value);

    alert("Cập nhật hoá đơn thành công!");
    modalInstance.value.hide();

  } catch (error) {
    console.log("Lỗi cập nhật hóa đơn", error);
  }
};


const deleteBill = async (id) => {
  if (confirm("Bạn có chắc muốn xoá hay không?")) {
    try {
      await axios.delete(`http://localhost:8080/bill/delete/${id}`);
      await fetchBills();
      if (modalInstance.value) modalInstance.value.hide();
    } catch (error) {
      console.log("Lỗi xoá hoá đơn", error);
    }
  }
};

const removeDetail = async (index) => {
  const detailId = billDetails.value[index].ID;
  if (confirm("Bạn có chắc muốn xoá sản phẩm này khỏi hoá đơn?")) {
    try {
      await axios.delete(`http://localhost:8080/billDetail/delete/${detailId}`);
      billDetails.value.splice(index, 1);
    } catch (error) {
      console.log("Lỗi xoá chi tiết sản phẩm:", error);
    }
  }
};


const formatCurrency = (value) => {
  const num = parseFloat(value) || 0;
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const subTotal = computed(() =>
  billDetails.value.reduce(
    (total, item) => total + item.QUANTITY * item.PRICE,
    0
  )
);

const grandTotal = computed(
  () =>
    subTotal.value -
    (parseFloat(selectedBill.value?.DISCOUNT_AMOUNT) || 0) +
    (parseFloat(selectedBill.value?.SHIPPING_FEE) || 0)
);

const isPaid = computed(
  () => selectedBill.value?.STATUS_PAYMENT === "Đã thanh toán"
);

const openEditDetail = (index) => {
  editingDetail.value = { ...billDetails.value[index] };
  editQuantity.value = editingDetail.value.QUANTITY;
  editIndex.value = index;
  const editModal = new Modal(document.getElementById('editDetailModal'));
  editModal.show();
};

const saveEditDetail = () => {
  if (editQuantity.value <= 0) {
    alert("Số lượng phải lớn hơn 0");
    return;
  }
  billDetails.value[editIndex.value].QUANTITY = editQuantity.value;
  const editModal = Modal.getInstance(document.getElementById('editDetailModal'));
  editModal.hide();
};

const updateBillDetail = async (detail) => {
  try {
    console.log("Updating detail:", detail);
    if (!detail.PRODUCT_DETAIL_ID) {
      console.error("Thiếu PRODUCT_DETAIL_ID khi update:", detail);
      return;
    }
    await axios.put(`http://localhost:8080/billDetail/update/${detail.ID}`, detail, {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("Lỗi cập nhật chi tiết hoá đơn:", error);
  }
};


onMounted(() => {
  fetchBills();
});
</script>

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
          <td>{{ formatCurrency(bill.GRAND_TOTAL || 0) }}</td>
          <td>
            <button class="btn btn-sm btn-primary me-2" @click="openModal(bill)">Chi tiết</button>
            <button class="btn btn-sm btn-danger" @click="deleteBill(bill.ID)">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Modal -->
    <div class="modal fade" id="billModal" tabindex="-1" aria-labelledby="billModalLabel" aria-hidden="true" ref="modal">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="billModalLabel">Chi tiết Hóa Đơn</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body" v-if="selectedBill">
            <!-- Thông tin hóa đơn -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label>Mã hóa đơn</label>
                <input v-model="selectedBill.CODE" class="form-control" :readonly="isPaid" />
              </div>
              <div class="col-md-6">
                <label>Người nhận</label>
                <input v-model="selectedBill.RECIPIENT_NAME" class="form-control" :readonly="isPaid" />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label>SĐT</label>
                <input v-model="selectedBill.RECIPIENT_PHONE_NUMBER" class="form-control" :readonly="isPaid" />
              </div>
              <div class="col-md-6">
                <label>Địa chỉ</label>
                <input v-model="selectedBill.RECEIVER_ADDRESS" class="form-control" :readonly="isPaid" />
              </div>
            </div>
            <div class="mb-3">
              <label>Ghi chú</label>
              <textarea v-model="selectedBill.NOTE" class="form-control" :readonly="isPaid"></textarea>
            </div>

            <!-- Chi tiết sản phẩm -->
            <h6>Chi tiết sản phẩm</h6>
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
                    <button class="btn btn-sm btn-warning me-1" @click.prevent="openEditDetail(index)">Sửa</button>
                    <button class="btn btn-sm btn-danger" @click.prevent="removeDetail(index)">Xóa</button>
                  </td>
                </tr>
                <tr v-if="billDetails.length === 0">
                  <td colspan="7" class="text-center text-muted">Không có sản phẩm nào</td>
                </tr>
              </tbody>
            </table>

            <!-- Modal sửa chi tiết sản phẩm -->
            <div class="modal fade" id="editDetailModal" tabindex="-1" aria-hidden="true" ref="editDetailModal">
              <div class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <h5 class="modal-title">Chỉnh sửa Sản phẩm</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                  </div>
                  <div class="modal-body" v-if="editingDetail">
                    <p><strong>{{ editingDetail.PRODUCT_NAME }}</strong></p>
                    <div class="mb-3">
                      <label class="form-label">Số lượng</label>
                      <input v-model.number="editQuantity" type="number" class="form-control" min="1" />
                    </div>
                  </div>
                  <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="button" class="btn btn-success" @click="saveEditDetail">Lưu</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tổng kết -->
            <h6 class="mt-4">Tổng kết</h6>
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

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button class="btn btn-success" @click="saveChanges" :disabled="isPaid">Lưu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- <script>
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
</script> -->

<style scoped>
img {
  border-radius: 4px;
}
</style>
