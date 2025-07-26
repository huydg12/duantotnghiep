<script setup>
import { onMounted, ref,watch,computed } from 'vue'
import { useRouter,useRoute } from 'vue-router'
import axios from 'axios'
import Banner from "../common/Banner.vue";
import { useCartFavoriteStore } from "@/stores/cartFavoriteStore";

const store = useCartFavoriteStore()
const router = useRouter()
const route = useRoute()
const products = ref([])
const showToast = ref(false)
const showRemoveToast = ref(false);
const favoriteProductIds = ref(new Set())
const favoriteMap = ref(new Map())
let customerId = null
const searchQuery = route.query.keyword || '';
const hasSearch = computed(() => !!searchQuery);
const brandOptions = ref([])
const currentPage = ref(1)
const itemsPerPage = 9
const totalPages = computed(() => Math.ceil(products.value.length / itemsPerPage))

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return products.value.slice(start, start + itemsPerPage)
})

function setPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' }) // Cuộn lên đầu
  }
}
const fetchBrands = async () => {
  try {
    const res = await axios.get('http://localhost:8080/brand/show')
    // Giả sử res.data trả về mảng object có thuộc tính brandName
    brandOptions.value = res.data.map(b => ({
      id: `brand-${b.name.toLowerCase()}`,
      label: b.name,
      value: b.name
    }))
  } catch (error) {
    console.error('❌ Lỗi khi fetch brand:', error)
  }
}
// Filter states
const filters = ref({
  sort: '',
  brands: route.query.brand || '',
  prices: '',
  search: route.query.keyword || ''
})

const userJson = localStorage.getItem("user")
if (userJson) {
  try {
    const user = JSON.parse(userJson)
    customerId = user.customerId
    console.log("✅ Customer ID:", customerId)
  } catch (error) {
    console.error("❌ Lỗi khi parse userJson:", error)
  }
}

const goToDetail = (id) => {
  router.push(`/productdetail/${id}`)
}

const fetchProducts = async () => {
  try {
    let fetchedProducts = []

    if (filters.value.search && filters.value.search.trim() !== '') {
      const keyword = filters.value.search.trim()
      const res = await axios.get(`http://localhost:8080/product/search`, {
        params: { keyword }
      })
      fetchedProducts = res.data
    } else {
      const response = await axios.get('http://localhost:8080/product/showSPdto')
      fetchedProducts = response.data
    }

    if (filters.value.brands) {
      fetchedProducts = fetchedProducts.filter(product =>
        filters.value.brands.toLowerCase() === product.brandName.toLowerCase()
      )
    }

    if (filters.value.sort === 'az') {
      fetchedProducts.sort((a, b) => a.productName.localeCompare(b.productName))
    } else if (filters.value.sort === 'za') {
      fetchedProducts.sort((a, b) => b.productName.localeCompare(a.productName))
    } else if (filters.value.sort === 'asc') {
      fetchedProducts.sort((a, b) => a.price - b.price)
    } else if (filters.value.sort === 'desc') {
      fetchedProducts.sort((a, b) => b.price - a.price)
    }

    if (filters.value.prices) {
      fetchedProducts = fetchedProducts.filter(product => {
        const price = product.price
        const range = filters.value.prices
        if (range === 'under-1m') return price < 1_000_000
        if (range === '1-2m') return price >= 1_000_000 && price <= 2_000_000
        if (range === 'over-2m') return price > 2_000_000
        return true
      })
    }

    products.value = fetchedProducts
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm', error)
  }
}

const fetchFavorites = async () => {
  if (!customerId) return
  try {
    const res = await axios.get(`http://localhost:8080/favorite/show/${customerId}`)
    favoriteProductIds.value = new Set(res.data.map(item => item.productId))
    favoriteMap.value = new Map(res.data.map(item => [item.productId, item.favoriteId]))
  } catch (err) {
    console.error("Lỗi lấy danh sách yêu thích", err)
  }
}



const toggleFavorite = async (productId) => {
  if (!customerId) {
    alert("Bạn cần đăng nhập để sử dụng tính năng này")
    return
  }

  const isFav = favoriteProductIds.value.has(productId)
  try {
    if (isFav) {
      const favoriteId = favoriteMap.value.get(productId)
      await axios.delete(`http://localhost:8080/favorite/delete/${favoriteId}`)
      favoriteProductIds.value.delete(productId)
      favoriteMap.value.delete(productId)
      await store.fetchFavoriteItems(customerId);
      showRemoveToast.value = true
      setTimeout(() => {
      showRemoveToast.value = false
    }, 3000)
    } else {
      const res = await axios.post(`http://localhost:8080/favorite/add`, {
        customerId, productId
      })
      const newFavoriteId = res.data.favoriteId || res.data.id
      favoriteMap.value.set(productId, newFavoriteId)
      favoriteProductIds.value.add(productId)
      await store.fetchFavoriteItems(customerId);
      showToast.value = true
      setTimeout(() => {
      showToast.value = false
    }, 3000)
    }
    await fetchFavorites()
  } catch (err) {
    console.error("Lỗi toggle yêu thích", err)
  }
}
watch(
  () => [filters.value.search, filters.value.brands, filters.value.sort, filters.value.prices],
  () => {
    currentPage.value = 1
    fetchProducts()
  }
)
// Theo dõi khi route thay đổi query
watch(() => router.currentRoute.value.query.brand, (newBrand) => {
  if (newBrand) {
    filters.value.brands = newBrand
  } else {
    filters.value.brands = ''
  }
  fetchProducts()
})
onMounted(() => {
  const selectedBrand = router.options.history.state?.brandFromHeader || ''
  if (selectedBrand) {
    filters.value.brands = selectedBrand
    fetchProducts() // => Gọi luôn để hiển thị sản phẩm theo brand
    // Xóa brandFromHeader khỏi state để khi quay lại không còn giữ nữa
    history.replaceState({ ...history.state, brandFromHeader: null }, '')
  }
filters.value.search = route.query.keyword || ''
  fetchBrands()
  fetchFavorites()

  if (!selectedBrand) {
    fetchProducts()
  }


})


// Filter sections config
const filterSections = computed(() => [
  {
    title: 'Sắp xếp',
    name: 'sort',
    model: 'sort',
    type: 'radio',
    options: [
      { id: 'sort-default', label: 'Tất cả', value: '' },
      { id: 'sort-az', label: 'Tên A-Z', value: 'az' },
      { id: 'sort-za', label: 'Tên Z-A', value: 'za' },
      { id: 'sort-price-asc', label: 'Giá tăng dần', value: 'asc' },
      { id: 'sort-price-desc', label: 'Giá giảm dần', value: 'desc' }
    ]
  },
  {
    title: 'Hãng',
    model: 'brands',
    type: 'radio',
    options: brandOptions.value
  },
    {
    title: 'Khoảng giá',
    model: 'prices',
    type: 'radio',
    options: [
      { id: 'price-1', label: 'Dưới 1 triệu', value: 'under-1m' },
      { id: 'price-2', label: '1 - 2 triệu', value: '1-2m' },
      { id: 'price-3', label: 'Trên 2 triệu', value: 'over-2m' }
    ]
  },
])
const handleRadioClick = (model, value) => {
  if (filters.value[model] === value) {
    filters.value[model] = '' // Bỏ chọn nếu chọn lại
  } else {
    filters.value[model] = value
  }

    if (model === 'brands') {
      router.replace({ path: '/product' }) // Xóa ?brand khỏi URL
    }
  

  fetchProducts()
}
</script>

<template>
  <Banner title="Sản phẩm" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

<div v-if="hasSearch" class="mb-4 search-result-header">
  <br />
  <h3 class="fw-semibold mb-1">Kết quả tìm kiếm cho: {{ searchQuery }} ({{ products.length }})</h3>
</div>
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
                    <input
                      class="form-check-input"
                      type="radio"
                      :id="opt.id"
                      :name="section.name"
                      :value="opt.value"
                      v-model="filters[section.model]"
                      @click="handleRadioClick(section.model, opt.value)"
                    />
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
      <div v-for="product in paginatedProducts" :key="product.id" class="col">
        <div class="card h-100 shadow-sm" @click="goToDetail(product.productId)" style="cursor: pointer;">
          <!-- Icon yêu thích -->
          <i
            class="fa-heart fa position-absolute top-0 end-0 m-2 favorite-icon transition"
            :class="{
              'fas text-danger scale-up': favoriteMap.has(product.productId),
              'far text-secondary': !favoriteMap.has(product.productId)
            }"
            @click.stop="toggleFavorite(product.productId)"
          ></i>
            <div class="image-container position-relative">
              <img
                :src="product.image1"
                class="product-image image-front"
                :alt="product.productName"
              />
              <img
                :src="product.image2"
                class="product-image image-hover position-absolute top-0 start-0"
                :alt="product.productName"
              />
            </div>
          
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ product.productName }}</h5>
            <p class="card-text text-muted">{{ product.brandName }}</p>
            <p class="card-text fw-bold text-danger">{{ product.price }}₫</p>
            <!-- <button class="btn btn-outline-primary mt-auto" @click="goToDetail(product.productId)">
              Xem chi tiết
            </button> -->
          </div>
        </div>
      </div>
    </div>

      <nav aria-label="Page navigation" class="mt-4">
        <ul class="pagination justify-content-center">
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <a class="page-link" href="#" @click.prevent="setPage(currentPage - 1)">
              &laquo;
            </a>
          </li>

          <li class="page-item"
              v-for="page in totalPages"
              :key="page"
              :class="{ active: page === currentPage }">
            <a class="page-link" href="#" @click.prevent="setPage(page)">
              {{ page }}
            </a>
          </li>

          <li class="page-item" :class="{ disabled: currentPage === totalPages }">
            <a class="page-link" href="#" @click.prevent="setPage(currentPage + 1)">
              &raquo;
            </a>
          </li>
        </ul>
      </nav>
            </div>
          </div>
        </div>
          <!-- Toast thông báo thêm vào giỏ thành công -->
        <div
          v-if="showToast"
          class="position-fixed top-0 end-0 p-3"
          style="z-index: 1055;"
        >
          <div class="toast align-items-center show bg-success text-white border-0">
            <div class="d-flex">
              <div class="toast-body">
                ✅ Đã thêm vào mục yêu thích!
              </div>
              <button
                type="button"
                class="btn-close btn-close-white me-2 m-auto"
                @click="showToast = false"
              ></button>
            </div>
          </div>
        </div>

  <!-- Toast xóa khỏi yêu thích -->
<div
  v-if="showRemoveToast"
  class="position-fixed top-0 end-0 p-3"
  style="z-index: 1055;"
>
  <div class="toast align-items-center show bg-danger text-white border-0">
    <div class="d-flex">
      <div class="toast-body">
        ❌ Đã xóa khỏi mục yêu thích!
      </div>
      <button
        type="button"
        class="btn-close btn-close-white me-2 m-auto"
        @click="showRemoveToast = false"
      ></button>
    </div>
  </div>
</div>
</template>



<style scoped>

.image-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-radius: 8px;
}

.image-front,
.image-hover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.image-hover {
  opacity: 0;
  z-index: 1;
}

.image-container:hover .image-hover {
  opacity: 1;
}

.image-container:hover .image-front {
  opacity: 0;
}

.card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: scale(1.03);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
  z-index: 2;
}
.favorite-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #ccc;
  font-size: 1.5rem;
  z-index: 10;
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-icon.active {
  color: hotpink;
}

.favorite-icon:hover {
  color: red;
}
.scale-up {
  transform: scale(1.2);
}
.toast {
  animation: slideIn 0.5s ease-out, fadeOut 0.5s ease-in 2.5s forwards;
  min-width: 250px;
  max-width: 300px;
  border-radius: 0.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0%);
    opacity: 1;
  }
}

@keyframes fadeOut {
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
.search-result-header {
  padding-left: 1.5rem; /* Cách lề trái 24px */
}

@media (min-width: 768px) {
  .search-result-header {
    padding-left: 5rem; /* Cách lề trái 48px khi màn hình lớn */
  }
}
</style>
