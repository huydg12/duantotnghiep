<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import Banner from './Banner.vue'

const router = useRouter()
const route = useRoute()

const products = ref([])
const keyword = ref(route.query.keyword || '')

const fetchProducts = async () => {
  try {
    const response = await axios.get('http://localhost:8080/product/showSPdto')
    products.value = response.data
  } catch (error) {
    console.error('Lỗi hiển thị sản phẩm:', error)
  }
}

const fetchProductsWithKeyWord = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/product/search?keyword=${keyword.value}`)
    products.value = response.data
  } catch (error) {
    console.error('Lỗi tìm kiếm:', error)
  }
}

watch(() => route.query.keyword, async (newKeyword) => {
  keyword.value = newKeyword
  await fetchProductsWithKeyWord()
})

onMounted(() => {
  if (keyword.value) {
    fetchProductsWithKeyWord()
  } else {
    fetchProducts()
  }
})

const goToDetail = (id) => {
  router.push(`/productDetail/${id}`)
}
</script>

<template>
  <Banner title="Sản phẩm" breadcrumb="" backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />

  <div class="container-fluid p-4 p-md-5">
    <div class="row g-4">
      <div class="col-12">
        <div class="row row-cols-1 row-cols-md-2 row-cols-xl-4 g-4 mb-5">
          <div v-for="product in products" :key="product.productId" class="col">
            <div class="card h-100 shadow-sm" @click="goToDetail(product.productId)" style="cursor: pointer;">
              <div class="image-container position-relative">
                <img :src="product.image1" class="product-image image-front" :alt="product.productName" />
                <img :src="product.image2" class="product-image image-hover position-absolute top-0 start-0" :alt="product.productName" />
              </div>
              <div class="card-body d-flex flex-column">
                <h5 class="card-title">{{ product.productName }}</h5>
                <p class="card-text text-muted">{{ product.brandName }}</p>
                <p class="card-text fw-bold text-danger">{{ product.price.toLocaleString() }}₫</p>
              </div>
            </div>
          </div>
        </div>
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
</style>
