<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-center">Quản lý Tồn Kho</h2>

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
          <th>Kích cỡ</th>
          <th>Số lượng tồn</th>
          <th>Ngày cập nhật</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in filteredInventory" :key="item.ID">
          <td>{{ index + 1 }}</td>
          <td>{{ item.PRODUCT_NAME }}</td>
          <td>{{ item.SIZE }}</td>
          <td>{{ item.QUANTITY }}</td>
          <td>{{ formatDate(item.MODIFIED_DATE) }}</td>
        </tr>
        <tr v-if="filteredInventory.length === 0">
          <td colspan="5" class="text-center text-muted">Không có kết quả phù hợp</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      inventoryList: [],
      searchQuery: "", // Dữ liệu nhập từ ô tìm kiếm
    };
  },
  computed: {
    filteredInventory() {
      if (!this.searchQuery) return this.inventoryList;
      return this.inventoryList.filter((item) =>
        item.PRODUCT_NAME.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
  },
  methods: {
    async fetchInventory() {
      // Giả lập dữ liệu - bạn thay bằng API thực tế
      this.inventoryList = [
        {
          ID: 1,
          PRODUCT_NAME: "Áo thun nam cổ tròn",
          SIZE: "M",
          QUANTITY: 120,
          MODIFIED_DATE: "2025-07-10T14:00:00",
        },
        {
          ID: 2,
          PRODUCT_NAME: "Quần jean nữ co giãn",
          SIZE: "S",
          QUANTITY: 55,
          MODIFIED_DATE: "2025-07-12T09:30:00",
        },
      ];
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleString("vi-VN");
    },
  },
  mounted() {
    this.fetchInventory();
  },
};
</script>

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
