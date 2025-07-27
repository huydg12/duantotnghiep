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
    PRODUCT_DETAIL_ID: item.PRODUCT_DETAIL_ID || item.productDetailId,
    PRODUCT_NAME: item.PRODUCT_NAME || item.productName,
    SIZE: item.SIZE || item.size,
    QUANTITY: item.QUANTITY || item.quantity,
    PRICE: item.PRICE || item.price,
    PRODUCT_IMAGE: item.PRODUCT_IMAGE || item.productImage,
    COLOR: item.COLOR || item.color,
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
const updateBillStatus = async (bill) => {
  try {
    // Gọi API cập nhật trạng thái
    await axios.put(`http://localhost:8080/bill/updateStatus/${bill.ID}`, {
      status: bill.STATUS
    });

    // ✅ Nếu trạng thái mới là "Đã hủy"
    if (bill.STATUS === 5) {
      // Gọi API lấy danh sách bill detail theo bill ID
      const res = await axios.get(`http://localhost:8080/billDetail/show/${bill.ID}`);
      const billDetails = res.data;

      // ✅ Lặp qua từng chi tiết đơn hàng để cộng lại vào kho
      for (const detail of billDetails) {
        console.log("Bill details:", billDetails);
        await axios.put(`http://localhost:8080/inventory/updateQuantity/${detail.productDetailId}`, {
          quantity: detail.quantity
        });
      }

      alert("Đơn hàng đã hủy và số lượng tồn kho đã được khôi phục.");
    } else {
      alert("Cập nhật trạng thái thành công");
    }

  } catch (error) {
    console.error("❌ Lỗi khi cập nhật trạng thái:", error);
    alert("Cập nhật trạng thái thất bại");
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
    // 1. Cập nhật chi tiết hóa đơn
    for (let detail of billDetails.value) {
      await updateBillDetail(detail);
    }

    // 2. Cập nhật tổng hóa đơn
    selectedBill.value.SUB_TOTAL = subTotal.value;
    selectedBill.value.GRAND_TOTAL = grandTotal.value;
    await axios.put(`http://localhost:8080/bill/update/${selectedBill.value.ID}`, selectedBill.value);


    // 4. Load lại dữ liệu
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
  const detail = billDetails.value[index];

  if (confirm("Bạn có chắc muốn xoá sản phẩm này khỏi hoá đơn?")) {
    try {
      // 1. Xoá khỏi DB
      await axios.delete(`http://localhost:8080/billDetail/delete/${detail.ID}`);

      // 2. Nếu đã thanh toán, cộng lại số lượng vào kho
      if (isPaid) {
        await axios.put(`http://localhost:8080/inventory/updateQuantity/${detail.PRODUCT_DETAIL_ID}`, {
          quantity: detail.QUANTITY
        });
      }

      // 3. Xoá khỏi danh sách hiển thị
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

const isPaid = computed(() => selectedBill.value?.STATUS >= 3);

const openEditDetail = (index) => {
  editingDetail.value = { ...billDetails.value[index] };
  editQuantity.value = editingDetail.value.QUANTITY;
  editIndex.value = index;
  const editModal = new Modal(document.getElementById('editDetailModal'));
  editModal.show();
};

function blockMinus(e) {
  if (e.key === '-' || e.key === 'e') {
    e.preventDefault()
  }
}
const cacheOldQuantity = (detail) => {
  detail.oldQuantity = detail.QUANTITY;
};
const handleQuantityChange = async (detail) => {
  try {
    // ✅ Lấy tồn kho hiện tại
    const inventoryRes = await axios.get(`http://localhost:8080/inventory/getQuantity/${detail.PRODUCT_DETAIL_ID}`);
    const quantityInventory = inventoryRes.data.quantityInventory;
    detail.quantityInventory = quantityInventory;

    // ✅ Lấy oldQuantity đúng thời điểm, trước khi thay đổi
    const oldQuantity = detail.oldQuantity !== undefined ? detail.oldQuantity : parseInt(detail.QUANTITY) || 1;

    // ✅ Parse lại QUANTITY người dùng nhập
    detail.QUANTITY = parseInt(detail.QUANTITY);
    if (!detail.QUANTITY || detail.QUANTITY < 1) {
      detail.QUANTITY = 1;
    } else if (detail.QUANTITY > quantityInventory + oldQuantity) {
      detail.QUANTITY = quantityInventory + oldQuantity;
    }

    // ✅ Kiểm tra ID
    if (!detail.ID || !detail.PRODUCT_DETAIL_ID) {
      console.error("❌ Lỗi: ID hoặc PRODUCT_DETAIL_ID bị thiếu:", detail);
      return;
    }

    // ✅ Cập nhật BILL_DETAIL
    await axios.put(`http://localhost:8080/billDetail/updateQuantity/${detail.ID}`, 
      { quantity: detail.QUANTITY }, 
      { headers: { "Content-Type": "application/json" } }
    );

    // ✅ Gửi chênh lệch để cập nhật kho
    await axios.put(`http://localhost:8080/inventory/updateQuantityByBill/${detail.PRODUCT_DETAIL_ID}`, 
      { 
        quantity: detail.QUANTITY,
        oldQuantity: oldQuantity
      },
      { headers: { "Content-Type": "application/json" } }
    );

    // ✅ Lưu lại oldQuantity mới nhất
    detail.oldQuantity = detail.QUANTITY;
            console.log("Old:", oldQuantity, "New:", detail.QUANTITY, "Chênh lệch:", detail.QUANTITY - oldQuantity);
  } catch (error) {
    console.error("❌ Lỗi khi cập nhật số lượng:", error);
  }
};


function statusClass(s) {
  return [
    "", // 0 - Không dùng
    "bg-light text-dark border border-warning",  // 1 - Chờ xác nhận (nhẹ nhàng, chờ xử lý)
    "bg-primary-subtle text-primary fw-bold",    // 2 - Đã xác nhận (tông xanh đậm rõ ràng)
    "bg-info-subtle text-info fw-bold",          // 3 - Đang giao (xanh nước biển nhạt)
    "bg-success-subtle text-success fw-bold",    // 4 - Hoàn tất (xanh lá nhẹ, dễ chịu)
    "bg-danger-subtle text-danger fw-bold",      // 5 - Đã hủy (đỏ nhạt nhưng cảnh báo)
  ][s];
}

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
        <tr v-for="bill in bills.slice().sort((a, b) => new Date(b.CREATED_DATE) - new Date(a.CREATED_DATE))" :key="bill.ID">
          <td>{{ bill.CODE }}</td>
          <td>{{ bill.CREATED_DATE }}</td>
          <td>{{ bill.RECIPIENT_NAME }}</td>
          <td>
            <select v-model="bill.STATUS" @change="updateBillStatus(bill)" class="form-select form-select-sm" :class="statusClass(bill.STATUS)">
            <option value="1" :disabled="bill.STATUS > 1">Chờ xác nhận</option>
            <option value="2" :disabled="bill.STATUS > 2">Đã xác nhận</option>
            <option value="3" :disabled="bill.STATUS > 3">Đang giao</option>
            <option value="4" :disabled="bill.STATUS > 4">Hoàn tất</option>
            <option value="5" :disabled="bill.STATUS === 5">Đã hủy</option>
            </select>
          </td>
          <td>{{ bill.STATUS_PAYMENT }}</td>
          <td>{{ formatCurrency(bill.GRAND_TOTAL || 0) }}</td>
          <td>
            <button class="btn btn-sm btn-primary me-2" @click="openModal(bill)">Chi tiết</button>
            <button v-show="false" class="btn btn-sm btn-danger" @click="deleteBill(bill.ID)">Xóa</button>
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
                  <th>Màu</th>
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
                  <td>{{ detail.COLOR }}</td>
                  <td>{{ detail.SIZE }}</td>
                  <!-- ✅ Số lượng có nút tăng/giảm -->
                  <td>
                    <input
                      type="number"
                      v-model="detail.QUANTITY"
                      min="1"
                      class="form-control form-control-sm text-center"
                      style="width: 60px;"
                      @focus="cacheOldQuantity(detail)"
                      @input="handleQuantityChange(detail)"
                      @keydown="blockMinus"
                      :readonly="isPaid"
                    />
                  </td>
                  <td>{{ formatCurrency(detail.PRICE) }}</td>
                  <td>{{ formatCurrency(detail.PRICE * detail.QUANTITY) }}</td>
                  <td v-if="!isPaid">
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

<style scoped>
img {
  border-radius: 4px;
}

option:disabled {
  background-color: #f8f9fa;
  color: #999;
}
</style>
