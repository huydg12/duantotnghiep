<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-center">Quản lý Tồn Kho</h2>

    <!-- Tìm kiếm -->
    <div class="mb-3">
      <input
        type="text"
        v-model="searchQuery"
        class="form-control"
        placeholder="Tìm kiếm theo tên sản phẩm..."
      />
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
        <tr v-for="(item, index) in filteredInventory" :key="item.id">
          <td>{{ index + 1 }}</td>
          <td>{{ item.productName }}</td>
          <td>{{ item.size }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ formatDate(item.modifiedDate) }}</td>
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
      searchQuery: "",
    };
  },
  computed: {
    filteredInventory() {
      if (!this.searchQuery) return this.inventoryList;
      return this.inventoryList.filter((item) =>
        item.productName.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
  },
  methods: {
    async fetchInventory() {
      try {
        const response = await fetch("http://localhost:8080/inventory/show");
        const data = await response.json();
        this.inventoryList = data;
      } catch (error) {
        console.error("Lỗi khi lấy dữ liệu tồn kho:", error);
      }
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
