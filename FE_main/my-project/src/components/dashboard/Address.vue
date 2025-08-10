<script setup>
import axios from "axios";
import { ref, computed, onMounted } from "vue";

const addresses = ref([]);
const isEditing = ref(false);
const currentPage = ref(1);
const pageSize = 5;

const form = ref({
  id: null,
  fullName: '',
  numberPhone: '',
  fullAddress: '',
  customerId: '',
  isDefault: false,
  addressType: '',
  streetName: '',
  wardName: '',
  cityName: '',
});

const fetchAddresses = async () => {
  try {
    const response = await axios.get("http://localhost:8080/address/show");
    addresses.value = response.data;
  } catch (error) {
    console.error("Lỗi fetch address:", error);
  }
};

const saveAddress = async () => {
  try {
    if (isEditing.value && form.value.id) {
      await axios.put(`http://localhost:8080/address/update/${form.value.id}`, form.value);
    } else {
      await axios.post("http://localhost:8080/address/add", form.value);
    }
    await fetchAddresses();
    resetForm();
  } catch (error) {
    console.error("Lỗi save address:", error);
  }
};

const editAddress = (addr) => {
  form.value = { ...addr };
  isEditing.value = true;
};

const deleteAddress = async (id) => {
  try {
    if (confirm("Bạn có chắc chắn muốn xoá địa chỉ này không?")) {
      await axios.delete(`http://localhost:8080/address/delete/${id}`);
      await fetchAddresses();
    }
  } catch (error) {
    console.error("Lỗi xoá:", error);
  }
};

const resetForm = () => {
  form.value = {
    id: null,
    fullName: '',
    numberPhone: '',
    fullAddress: '',
    customerId: '',
    isDefault: false,
    addressType: '',
    streetName: '',
    wardName: '',
    cityName: '',
  };
  isEditing.value = false;
};

const totalPages = computed(() =>
  Math.ceil(addresses.value.length / pageSize)
);

const paginatedAddresses = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return addresses.value.slice(start, start + pageSize);
});

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

onMounted(() => {
  fetchAddresses();
});
</script>

<template>
  <div class="container py-4">
    <h2 class="text-center mb-4 fw-bold">Quản Lý Địa Chỉ</h2>

    <!-- Form -->
    <form @submit.prevent="saveAddress" class="border p-4 rounded bg-light mb-4">
      <div class="mb-3">
        <label class="form-label">Họ tên</label>
        <input v-model="form.fullName" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">Số điện thoại</label>
        <input v-model="form.numberPhone" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">Địa chỉ đầy đủ</label>
        <input v-model="form.fullAddress" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">Mã Khách hàng</label>
        <input v-model="form.customerId" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Loại địa chỉ</label>
        <input v-model="form.addressType" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Đường</label>
        <input v-model="form.streetName" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Phường/Xã</label>
        <input v-model="form.wardName" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">Thành phố</label>
        <input v-model="form.cityName" class="form-control" />
      </div>
      <div class="mb-3 form-check">
        <input type="checkbox" class="form-check-input" id="isDefault" v-model="form.isDefault" />
        <label class="form-check-label" for="isDefault">Đặt làm mặc định</label>
      </div>
      <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">{{ isEditing ? "Cập nhật" : "Thêm" }}</button>
        <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
      </div>
    </form>

    <!-- Table -->
    <div class="table-responsive">
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-secondary text-center">
          <tr>
            <th>ID</th>
            <th>Họ tên</th>
            <th>SĐT</th>
            <th>Địa chỉ</th>
            <th>Loại</th>
            <th>Mặc định</th>
            <th>Thành phố</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="addr in paginatedAddresses" :key="addr.id">
            <td class="text-center">{{ addr.id }}</td>
            <td class="text-center">{{ addr.fullName }}</td>
            <td class="text-center">{{ addr.numberPhone }}</td>
            <td class="text-wrap">{{ addr.fullAddress }}</td>
            <td class="text-center">{{ addr.addressType }}</td>
            <td class="text-center">
              <span class="badge" :class="addr.isDefault ? 'bg-success' : 'bg-secondary'">
                {{ addr.isDefault ? '✔️' : '' }}
              </span>
            </td>
            <td class="text-center">{{ addr.cityName }}</td>
            <td class="text-center">
              <button class="btn btn-success btn-sm me-2" @click="editAddress(addr)">Sửa</button>
              <button class="btn btn-danger btn-sm" @click="deleteAddress(addr.id)">Xoá</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <nav>
      <ul class="pagination justify-content-center mt-4 custom-pagination">
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
  </div>
</template>

<style scoped>
.text-wrap {
  white-space: normal;
  word-break: break-word;
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
