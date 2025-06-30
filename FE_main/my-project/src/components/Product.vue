<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Banner from './Banner.vue'
import axios from 'axios'

const router = useRouter()

const goToDetail = (id) => {
  router.push('/productDetail')
}

const products = ref([])

const fetchProducts = async () => {
  try {
    const response = await axios.get('localhost:8080/product/show') // Thay thế bằng API thực tế
    products.value = response.data
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm', error)
  }
}

onMounted(() => {
  fetchProducts()
})  


// Filter states
const filters = ref({
  sort: '',
  brands: [],
  sizes: [],
  colors: [],
  genders: [],
  soles: [],
  collars: [],
  prices: []
})

// Filter sections config
const filterSections = [
  {
    title: 'Sắp xếp',
    name: 'sort',
    model: 'sort',
    type: 'radio',
    options: [
      { id: 'sort-az', label: 'Tên A-Z', value: 'az' },
      { id: 'sort-za', label: 'Tên Z-A', value: 'za' },
      { id: 'sort-price-asc', label: 'Giá tăng dần', value: 'asc' },
      { id: 'sort-price-desc', label: 'Giá giảm dần', value: 'desc' }
    ]
  },
  {
    title: 'Hãng',
    model: 'brands',
    type: 'checkbox',
    options: ['Nike', 'Adidas', 'Puma'].map(b => ({
      id: `brand-${b.toLowerCase()}`,
      label: b,
      value: b
    }))
  },
  {
    title: 'Size',
    model: 'sizes',
    type: 'checkbox',
    options: [38, 39, 40, 41, 42, 43].map(s => ({
      id: `size-${s}`,
      label: s.toString(),
      value: s.toString()
    }))
  },
  {
    title: 'Màu sắc',
    model: 'colors',
    type: 'checkbox',
    options: ['Đen', 'Trắng', 'Đỏ'].map(c => ({
      id: `color-${c.toLowerCase()}`,
      label: c,
      value: c
    }))
  },
  {
    title: 'Giới tính',
    model: 'genders',
    type: 'checkbox',
    options: ['Nam', 'Nữ', 'Unisex'].map(g => ({
      id: `gender-${g.toLowerCase()}`,
      label: g,
      value: g
    }))
  },
  {
    title: 'Loại đế',
    model: 'soles',
    type: 'checkbox',
    options: ['Đế bằng', 'Đế cao'].map(s => ({
      id: `sole-${s === 'Đế bằng' ? 'flat' : 'platform'}`,
      label: s,
      value: s
    }))
  },
  {
    title: 'Loại cổ',
    model: 'collars',
    type: 'checkbox',
    options: ['Cổ thấp', 'Cổ cao'].map(c => ({
      id: `collar-${c === 'Cổ thấp' ? 'low' : 'high'}`,
      label: c,
      value: c
    }))
  },
  {
    title: 'Khoảng giá',
    model: 'prices',
    type: 'checkbox',
    options: [
      { id: 'price-1', label: 'Dưới 1 triệu', value: 'under-1m' },
      { id: 'price-2', label: '1 - 2 triệu', value: '1-2m' },
      { id: 'price-3', label: 'Trên 2 triệu', value: 'over-2m' }
    ]
  }
]


</script>

<template>
  <Banner title="Sản phẩm" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
  <div class="container-fluid p-4 p-md-5">
    <div class="row g-4">
      <!-- Bộ lọc -->
      <div class="col-lg-3">
        <div class="bg-white rounded shadow-sm p-4">
          <h2 class="fs-4 fw-bold mb-4">Bộ lọc</h2>
          <div class="accordion" id="filterAccordion">
            <div v-for="(section, index) in filterSections" :key="section.title" class="accordion-item">
              <h2 class="accordion-header">
                <button class="accordion-button fw-semibold" :class="{ collapsed: index !== 0 }" type="button"
                  data-bs-toggle="collapse" :data-bs-target="'#collapse' + index">
                  {{ section.title }}
                </button>
              </h2>
              <div :id="'collapse' + index" class="accordion-collapse collapse" :class="{ show: index === 0 }">
                <div class="accordion-body">
                  <div v-if="section.type === 'radio'">
                    <div v-for="opt in section.options" :key="opt.id" class="form-check">
                      <input class="form-check-input" type="radio" :id="opt.id" :name="section.name" :value="opt.value"
                        v-model="filters[section.model]" />
                      <label class="form-check-label" :for="opt.id">{{ opt.label }}</label>
                    </div>
                  </div>
                  <div v-else>
                    <div v-for="opt in section.options" :key="opt.id" class="form-check">
                      <input class="form-check-input" type="checkbox" :id="opt.id" :value="opt.value"
                        v-model="filters[section.model]" />
                      <label class="form-check-label" :for="opt.id">{{ opt.label }}</label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Danh sách sản phẩm -->
      <div class="col-lg-9">
        <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4 mb-5">
          <div v-for="product in products" :key="product.id" class="col">
            <div class="card h-100 shadow-sm">
              <img :src="product.image" class="card-img-top" :alt="product.name" />
              <div class="card-body d-flex flex-column">
                <h5 class="card-title">{{ product.name }}</h5>
                <p class="card-text text-muted">{{ product.brand }}</p>
                <p class="card-text fw-bold text-danger">{{ product.price }}₫</p>
                <button class="btn btn-outline-primary mt-auto" @click="goToDetail()">Xem chi tiết</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Phân trang -->
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <li class="page-item disabled">
              <a class="page-link" href="#"><span>&laquo;</span></a>
            </li>
            <li class="page-item active"><a class="page-link" href="#">1</a></li>
            <li class="page-item"><a class="page-link" href="#">2</a></li>
            <li class="page-item"><a class="page-link" href="#">3</a></li>
            <li class="page-item">
              <a class="page-link" href="#"><span>&raquo;</span></a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>



<style scoped>
.card-img-top {
  height: 200px;
  object-fit: cover;
}
</style>
