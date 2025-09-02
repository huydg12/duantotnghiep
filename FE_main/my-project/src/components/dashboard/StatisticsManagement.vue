<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const dailyRevenue = ref([])             // [{ createDate, billCount, totalStatistic }]
const loading = ref(false)
const errorMsg = ref('')

// Định dạng tiền tệ VND
const formatCurrency = (val) => {
  if (val == null) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(val) || 0)
}

// ====== NHÓM / GỘP THEO THÁNG & NĂM (từ dailyRevenue) ======
const monthRevenue = computed(() => {
  const m = new Map()
  for (const r of dailyRevenue.value) {
    const key = (r.createDate || '').slice(0, 7) // YYYY-MM
    if (!key) continue
    const cur = m.get(key) || { period: key, billCount: 0, totalStatistic: 0 }
    cur.billCount += Number(r.billCount) || 0
    cur.totalStatistic += Number(r.totalStatistic) || 0
    m.set(key, cur)
  }
  // mới nhất lên đầu
  return Array.from(m.values()).sort((a, b) => b.period.localeCompare(a.period))
})

const yearRevenue = computed(() => {
  const m = new Map()
  for (const r of dailyRevenue.value) {
    const key = (r.createDate || '').slice(0, 4) // YYYY
    if (!key) continue
    const cur = m.get(key) || { period: key, billCount: 0, totalStatistic: 0 }
    cur.billCount += Number(r.billCount) || 0
    cur.totalStatistic += Number(r.totalStatistic) || 0
    m.set(key, cur)
  }
  return Array.from(m.values()).sort((a, b) => b.period.localeCompare(a.period))
})

// ====== CHỌN CHẾ ĐỘ & INPUT ======
const viewMode = ref('day')        // 'day' | 'month' | 'year'
const selectedDay = ref('')
const selectedMonth = ref('')      // YYYY-MM
const selectedYear = ref('')       // YYYY
const startDate = ref('')
const endDate = ref('')
// Ô kết quả
const summary = ref({ period: '', billCount: 0, total: 0 })

// Tính kết quả theo chế độ & input
const calculate = () => {
  if (viewMode.value === 'day') {
    const row = dailyRevenue.value.find(x => x.createDate === selectedDay.value)
    summary.value = {
      period: selectedDay.value || '—',
      billCount: row ? Number(row.billCount) || 0 : 0,
      total: row ? Number(row.totalStatistic) || 0 : 0
    }
  } else if (viewMode.value === 'month') {
    const row = monthRevenue.value.find(x => x.period === selectedMonth.value)
    summary.value = {
      period: selectedMonth.value || '—',
      billCount: row ? Number(row.billCount) || 0 : 0,
      total: row ? Number(row.totalStatistic) || 0 : 0
    }
  } else if (viewMode.value === 'year') {
    const row = yearRevenue.value.find(x => x.period === String(selectedYear.value))
    summary.value = {
      period: selectedYear.value || '—',
      billCount: row ? Number(row.billCount) || 0 : 0,
      total: row ? Number(row.totalStatistic) || 0 : 0
    }
  } else if (viewMode.value === 'range') {
    const from = new Date(startDate.value)
    const to = new Date(endDate.value)
    const rows = dailyRevenue.value.filter(r => {
      const d = new Date(r.createDate)
      return d >= from && d <= to
    })
    summary.value = {
      period: (startDate.value || '—') + ' → ' + (endDate.value || '—'),
      billCount: rows.reduce((sum, r) => sum + (Number(r.billCount) || 0), 0),
      total: rows.reduce((sum, r) => sum + (Number(r.totalStatistic) || 0), 0)
    }
  }
}

// Chỉ để hiển thị bảng ngày (5 ngày gần nhất)
const dailyRevenueLast5 = computed(() => {
  if (!dailyRevenue.value.length) return []
  const today = new Date()
  const fiveDaysAgo = new Date()
  fiveDaysAgo.setDate(today.getDate() - 5)

  return dailyRevenue.value
    .filter(r => {
      const d = new Date(r.createDate)
      return d >= fiveDaysAgo && d <= today
    })
    .sort((a, b) => new Date(b.createDate) - new Date(a.createDate))
})

const fetchDailyRevenue = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await axios.get('http://localhost:8080/statistics/daily')
    dailyRevenue.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    errorMsg.value = err?.response?.data?.message || err?.message || 'Có lỗi khi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

// --- State ---
const top5Products = ref([]);
const loadingTop5 = ref(false);

// --- Hàm fetch dữ liệu Top 5 ---
const fetchTop5Products = async () => {
  try {
    loadingTop5.value = true;
    const res = await axios.get("http://localhost:8080/statistics/top5-best-selling");
    top5Products.value = res.data;
  } catch (err) {
    console.error("❌ Lỗi khi tải top 5 sản phẩm:", err);
    top5Products.value = [];
  } finally {
    loadingTop5.value = false;
  }
};


// Click vào 1 dòng ngày -> đổ qua ô kết quả
const pickDay = (row) => {
  viewMode.value = 'day'
  selectedDay.value = row.createDate
  calculate()
}

onMounted(() => {
  fetchDailyRevenue()
  fetchTop5Products()
})
</script>

<template>
  <div class="container py-4">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h3 class="text-primary m-0">📅 Doanh thu theo ngày</h3>
    </div>

    <div v-if="errorMsg" class="alert alert-danger" role="alert">
      {{ errorMsg }}
    </div>

    <!-- Bộ chọn chế độ & input + Ô kết quả -->
    <div class="row g-3 mb-3">
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4">
          <div class="card-body">
            <div class="d-flex gap-2 align-items-center flex-wrap">
              <select v-model="viewMode" class="form-select" style="max-width: 180px">
                <option value="day">Theo ngày</option>
                <option value="month">Theo tháng</option>
                <option value="year">Theo năm</option>
                <option value="range">Khoảng ngày</option>
              </select>

              <template v-if="viewMode === 'day'">
                <input type="date" v-model="selectedDay" class="form-control" style="max-width: 220px" />
              </template>

              <template v-else-if="viewMode === 'month'">
                <input type="month" v-model="selectedMonth" class="form-control" style="max-width: 220px" />
              </template>

              <template v-else-if="viewMode === 'year'">
                <input type="number" v-model="selectedYear" placeholder="YYYY" min="2000" max="2100"
                  class="form-control" style="max-width: 160px" />
              </template>
              <template v-else>
                <input type="date" v-model="startDate" class="form-control" style="max-width: 200px" />
                <span>→</span>
                <input type="date" v-model="endDate" class="form-control" style="max-width: 200px" />
              </template>

              <button class="btn btn-success" @click="calculate">Tính</button>
            </div>
            <small class="text-muted d-block mt-2">
              Gợi ý: bạn có thể <u>click vào một dòng</u> ở bảng dưới để đổ dữ liệu vào ô kết quả.
            </small>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4">
          <div class="card-body">
            <h6 class="text-muted mb-2">Kết quả</h6>
            <div class="d-flex justify-content-between">
              <span>Kỳ:</span>
              <strong>{{ summary.period || '—' }}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span>Số hóa đơn:</span>
              <strong>{{ summary.billCount }}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span>Tổng tiền:</span>
              <strong class="text-success">{{ formatCurrency(summary.total) }}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- BẢNG NGÀY -->
    <h3 class="text-primary m-0">Dữ liệu 5 ngày gần nhất</h3>
    <br>
    <div v-if="loading" class="text-muted mb-2">Đang tải dữ liệu…</div>

    <table class="table table-bordered text-center align-middle table-hover">
      <thead class="table-info">
        <tr>
          <th style="width: 60px">#</th>
          <th>Ngày</th>
          <th>Số hóa đơn</th>
          <th>Tổng tiền</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="(row, idx) in dailyRevenueLast5" :key="row.createDate + '-' + idx" class="row-click"
          @click="pickDay(row)">
          <td>{{ idx + 1 }}</td>
          <td>{{ row.createDate }}</td>
          <td class="fw-semibold">{{ row.billCount }}</td>
          <td class="fw-semibold">{{ formatCurrency(row.totalStatistic) }}</td>
        </tr>
        <tr v-if="!loading && dailyRevenueLast5.length === 0">
          <td colspan="4">Không có dữ liệu</td>
        </tr>
      </tbody>
    </table>

    <!-- BẢNG TOP 5 SẢN PHẨM BÁN CHẠY -->
    <h3 class="text-primary m-0 mt-5">🔥 Top 5 sản phẩm bán chạy nhất</h3>
    <br>
    <div v-if="loadingTop5" class="text-muted mb-2">Đang tải dữ liệu…</div>

    <table class="table table-bordered text-center align-middle table-hover">
      <thead class="table-warning">
        <tr>
          <th style="width: 60px">#</th>
          <th>Tên sản phẩm</th>
          <th>Số lượng đã bán</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, idx) in top5Products" :key="item.productName">
          <td>{{ idx + 1 }}</td>
          <td class="fw-semibold">{{ item.productName }}</td>
          <td class="fw-semibold text-success">{{ item.totalSold }}</td>
        </tr>
        <tr v-if="!loadingTop5 && top5Products.length === 0">
          <td colspan="3">Không có dữ liệu</td>
        </tr>
      </tbody>
    </table>


  </div>
</template>

<style scoped>
.table td,
.table th {
  vertical-align: middle;
}

.row-click {
  cursor: pointer;
}
</style>