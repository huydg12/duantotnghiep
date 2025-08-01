<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const overview = ref(null)
const topSellingProducts = ref([])
const fromDate = ref('')
const toDate = ref('')
const revenueByBrand = ref([])

const stockStatistics = ref([])

const fromDateBrand = ref('')
const toDateBrand = ref('')

const fromDateStock = ref('')
const toDateStock = ref('')

const filterRevenueByBrand = async () => {
  if (!fromDateBrand.value || !toDateBrand.value) return
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/revenue-by-brand/filter', {
      params: {
        from: fromDateBrand.value,
        to: toDateBrand.value
      }
    })
    revenueByBrand.value = res.data
  } catch (error) {
    console.error('Lỗi khi lọc doanh thu theo thương hiệu:', error)
  }
}

const filterStockStatistics = async () => {
  if (!fromDateStock.value || !toDateStock.value) return
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/stock/filter', {
      params: {
        from: fromDateStock.value,
        to: toDateStock.value
      }
    })
    stockStatistics.value = res.data
  } catch (error) {
    console.error('Lỗi khi lọc tồn kho đã bán:', error)
  }
}

const fetchStockStatistics = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/stock')
    stockStatistics.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải tồn kho đã bán:', error)
  }
}

const fetchRevenueByBrand = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/revenue-by-brand')
    revenueByBrand.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải doanh thu theo thương hiệu:', error)
  }
}

const formatCurrency = (val) => {
  if (val == null) return '0 đ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(val)
}

const fetchOverview = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/overview')
    overview.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu tổng quan:', error)
  }
}

const fetchTopSellingProducts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/top-selling-products')
    topSellingProducts.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm bán chạy:', error)
  }
}

const filterTopSellingProducts = async () => {
  if (!fromDate.value || !toDate.value) return
  try {
    const res = await axios.get('http://localhost:8080/api/statistics/top-selling-products/filter', {
      params: {
        from: fromDate.value,
        to: toDate.value,
      },
    })
    topSellingProducts.value = res.data
  } catch (error) {
    console.error('Lỗi khi lọc theo ngày:', error)
  }
}

onMounted(() => {
  fetchOverview()
  fetchTopSellingProducts()
  fetchRevenueByBrand()
  fetchStockStatistics()
})
</script>

<template>
  <div class="container py-4">
    <h2 class="mb-4 text-primary fw-bold">📊 Thống kê tổng quan</h2>

    <div class="row g-4">
      <div class="col-md-4" v-if="overview">
        <div class="card border-0 shadow rounded-4">
          <div class="card-body text-center">
            <h6 class="text-muted">Tổng doanh thu</h6>
            <p class="fs-5 fw-bold text-success">{{ formatCurrency(overview.totalRevenue) }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-4" v-if="overview">
        <div class="card border-0 shadow rounded-4">
          <div class="card-body text-center">
            <h6 class="text-muted">Hóa đơn đã thanh toán</h6>
            <p class="fs-5 fw-bold">{{ overview.paidBillCount }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-4" v-if="overview">
        <div class="card border-0 shadow rounded-4">
          <div class="card-body text-center">
            <h6 class="text-muted">Sản phẩm đã bán</h6>
            <p class="fs-5 fw-bold">{{ overview.totalProductSold }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-4" v-if="overview">
        <div class="card border-0 shadow rounded-4">
          <div class="card-body text-center">
            <h6 class="text-muted">Đơn có khuyến mãi</h6>
            <p class="fs-5 fw-bold">{{ overview.promoBillCount }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-4" v-if="overview">
        <div class="card border-0 shadow rounded-4">
          <div class="card-body text-center">
            <h6 class="text-muted">Tổng giảm giá</h6>
            <p class="fs-5 fw-bold text-danger">{{ formatCurrency(overview.totalDiscountAmount) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Top sản phẩm bán chạy -->
    <div class="mt-5">
      <h3 class="mb-3 text-primary">🔥 Top sản phẩm bán chạy</h3>
      <!-- <div class="d-flex gap-2 align-items-center mb-3 flex-wrap">
        <input type="date" v-model="fromDate" class="form-control" style="max-width: 200px" />
        <span>→</span>
        <input type="date" v-model="toDate" class="form-control" style="max-width: 200px" />
        <button class="btn btn-outline-primary" @click="filterTopSellingProducts">Lọc</button>
      </div> -->
      <table class="table table-bordered text-center align-middle table-hover">
        <thead class="table-dark">
          <tr>
            <th>#</th>
            <th>Tên sản phẩm</th>
            <th>Số lượng bán</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in topSellingProducts" :key="index">
            <td>{{ index + 1 }}</td>
            <td>{{ item.productName }}</td>
            <td class="fw-semibold">{{ item.totalSold }}</td>
          </tr>
          <tr v-if="topSellingProducts.length === 0">
            <td colspan="3" class="text-center">Không có dữ liệu</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Doanh thu theo thương hiệu -->
    <div class="mt-5">
      <h3 class="mb-3 text-primary">🏷️ Doanh thu theo thương hiệu</h3>
      <!-- <div class="d-flex gap-2 align-items-center mb-3 flex-wrap">
        <input type="date" v-model="fromDateBrand" class="form-control" style="max-width: 200px" />
        <span>→</span>
        <input type="date" v-model="toDateBrand" class="form-control" style="max-width: 200px" />
        <button class="btn btn-outline-success" @click="filterRevenueByBrand">Lọc</button>
      </div> -->

      <table class="table table-bordered text-center align-middle table-hover">
        <thead class="table-success">
          <tr>
            <th>#</th>
            <th>Thương hiệu</th>
            <th>Doanh thu</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in revenueByBrand" :key="index">
            <td>{{ index + 1 }}</td>
            <td>{{ item.brandName }}</td>
            <td class="fw-semibold">{{ formatCurrency(item.totalRevenue) }}</td>
          </tr>
          <tr v-if="revenueByBrand.length === 0">
            <td colspan="3" class="text-center">Không có dữ liệu</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Tồn kho đã bán -->
    <div class="mt-5">
      <h3 class="mb-3 text-primary">📦 Tồn kho đã bán</h3>
      <!-- <div class="d-flex gap-2 align-items-center mb-3 flex-wrap">
        <input type="date" v-model="fromDateStock" class="form-control" style="max-width: 200px" />
        <span>→</span>
        <input type="date" v-model="toDateStock" class="form-control" style="max-width: 200px" />
        <button class="btn btn-outline-warning" @click="filterStockStatistics">Lọc</button>
      </div> -->

      <table class="table table-bordered text-center align-middle table-hover">
        <thead class="table-warning">
          <tr>
            <th>#</th>
            <th>Tên sản phẩm</th>
            <th>Màu sắc</th>
            <th>Kích thước</th>
            <th>Số lượng đã bán</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in stockStatistics" :key="index">
            <td>{{ index + 1 }}</td>
            <td>{{ item.productName }}</td>
            <td>{{ item.color }}</td>
            <td>{{ item.size }}</td>
            <td class="fw-semibold">{{ item.totalSold }}</td>
          </tr>
          <tr v-if="stockStatistics.length === 0">
            <td colspan="5" class="text-center">Không có dữ liệu</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.card-text {
  font-size: 1.2rem;
  font-weight: bold;
}
</style>
