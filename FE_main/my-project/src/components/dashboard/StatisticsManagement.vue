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
const searchStock = ref('')
const fromDateStock = ref('')
const toDateStock = ref('')
const searchTopSelling = ref('')
const searchBrand = ref('')

const filterRevenueByBrand = async () => {
  try {
    const params = {}
    if (fromDateBrand.value) params.from = fromDateBrand.value
    if (toDateBrand.value) params.to = toDateBrand.value
    if (searchBrand.value) params.keyword = searchBrand.value

    const res = await axios.get('http://localhost:8080/api/statistics/revenue-by-brand/filter', { params })
    revenueByBrand.value = res.data
  } catch (error) {
    console.error('Lỗi khi lọc doanh thu theo thương hiệu:', error)
  }
}

const filterStockStatistics = async () => {
  try {
    const params = {}
    if (fromDateStock.value) params.from = fromDateStock.value
    if (toDateStock.value) params.to = toDateStock.value
    if (searchStock.value) params.keyword = searchStock.value  // từ khóa tìm kiếm
    console.log(params)
    const res = await axios.get('http://localhost:8080/api/statistics/stock/filter', { params })
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

// Reset

const resetTopSellingProducts = async () => {
  fromDate.value = ''
  toDate.value = ''
  searchTopSelling.value = ''
  await fetchTopSellingProducts()
}

const resetRevenueByBrand = async () => {
  fromDateBrand.value = ''
  toDateBrand.value = ''
  searchBrand.value = ''
  await fetchRevenueByBrand()
}

const searchTopSellingProducts = async () => {
  try {
    const params = {}
    if (fromDate.value) params.from = fromDate.value
    if (toDate.value) params.to = toDate.value
    if (searchTopSelling.value) params.keyword = searchTopSelling.value

    const res = await axios.get('http://localhost:8080/api/statistics/top-selling-products/filter', { params })
    topSellingProducts.value = res.data
  } catch (error) {
    console.error('Lỗi khi tìm kiếm sản phẩm:', error)
  }
}

const resetStockStatistics = async () => {
  searchStock.value = ''
  fromDateStock.value = ''
  toDateStock.value = ''
  await fetchStockStatistics()
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
      <div class="d-flex gap-2 align-items-center mb-3 flex-wrap">
        <input type="date" v-model="fromDate" class="form-control" style="max-width: 200px" />
        <span>→</span>
        <input type="date" v-model="toDate" class="form-control" style="max-width: 200px" />

        <input type="text" v-model="searchTopSelling" class="form-control" placeholder="Tên sản phẩm..."
          style="max-width: 200px" />

        <button class="btn btn-outline-primary" @click="searchTopSellingProducts">Tìm kiếm</button>
        <button class="btn btn-outline-secondary" @click="resetTopSellingProducts">Bỏ lọc</button>
      </div>


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
            <td class="fw-semibold">{{ item.totalQuantity }}</td>
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
      <div class="d-flex gap-2 align-items-center mb-3 flex-wrap">
        <input type="date" v-model="fromDateBrand" class="form-control" style="max-width: 200px" />
        <span>→</span>
        <input type="date" v-model="toDateBrand" class="form-control" style="max-width: 200px" />

        <input type="text" v-model="searchBrand" class="form-control" placeholder="Tên thương hiệu..."
          style="max-width: 200px" />
        <button class="btn btn-outline-success" @click="filterRevenueByBrand">Tìm kiếm</button>
        <button class="btn btn-outline-secondary" @click="resetRevenueByBrand">Bỏ lọc</button>

      </div>


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
      <div class="mb-3 d-flex gap-2 align-items-center flex-wrap">
        <input type="text" v-model="searchStock" class="form-control" placeholder="Tìm theo tên sản phẩm..."
          style="max-width: 300px" />
        <button class="btn btn-outline-primary" @click="filterStockStatistics">Lọc</button>
        <button class="btn btn-outline-secondary" @click="resetStockStatistics">Bỏ lọc</button>
      </div>

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
