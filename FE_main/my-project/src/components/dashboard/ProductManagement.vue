<script setup>
import { reactive, ref, nextTick } from 'vue'
import * as bootstrap from 'bootstrap'

const brands = [
    { id: 1, name: 'Nike' },
    { id: 2, name: 'Adidas' },
    { id: 3, name: 'Puma' }
]
const categories = [
    { id: 1, name: 'Thể thao' },
    { id: 2, name: 'Thời trang' },
    { id: 3, name: 'Chạy bộ' }
]
const soles = [
    { id: 1, name: 'Cao su' },
    { id: 2, name: 'Foam' },
    { id: 3, name: 'PU' }
]
const sizes = [{ id: 39 }, { id: 40 }, { id: 41 }, { id: 42 }]
const colors = [
    { id: 1, name: 'Đen' },
    { id: 2, name: 'Trắng' },
    { id: 3, name: 'Xám' }
]
const collars = [
    { id: 1, name: 'Thấp cổ' },
    { id: 2, name: 'Cổ cao' }
]

const products = ref([])
const form = reactive({
    id: null,
    product_name: '',
    brand_id: null,
    category_id: null,
    sole_id: null,
    description: '',
    status: 1,
    details: []
})

const currentProduct = ref(null)
const detailForm = reactive({
    price: '',
    description: ''
})

const selectedSizes = ref([])
const selectedColors = ref([])
const selectedCollars = ref([])

function getNameById(list, id) {
    const found = list.find(i => i.id === id)
    return found ? found.name || found.id : 'N/A'
}

function saveProduct() {
    if (!form.product_name) return alert('Nhập tên sản phẩm')
    if (form.id) {
        const idx = products.value.findIndex(p => p.id === form.id)
        products.value[idx] = { ...form, details: [...form.details] }
    } else {
        form.id = Date.now()
        products.value.push({ ...form, details: [] })
    }
    resetForm()
}

function resetForm() {
    Object.assign(form, {
        id: null,
        product_name: '',
        brand_id: null,
        category_id: null,
        sole_id: null,
        description: '',
        status: 1,
        details: []
    })
}

function deleteProduct(id) {
    products.value = products.value.filter(p => p.id !== id)
}

function editProduct(p) {
    Object.assign(form, JSON.parse(JSON.stringify(p)))
}

function openDetailModal(product) {
    currentProduct.value = product
    nextTick(() => {
        const modal = new bootstrap.Modal(document.getElementById('productDetailModal'))
        modal.show()
    })
}

function addOrUpdateDetail() {
    if (!selectedSizes.value.length || !selectedColors.value.length || !selectedCollars.value.length || !detailForm.price) {
        return alert('Chọn đầy đủ Size, Màu, Cổ và nhập Giá')
    }

    let count = 0;
    const list = currentProduct.value.details
    for (const size_id of selectedSizes.value) {
        for (const color_id of selectedColors.value) {
            for (const collar_id of selectedCollars.value) {
                const exists = list.some(d => d.size_id === size_id && d.color_id === color_id && d.collar_id === collar_id)
                if (!exists) {
                    list.push({
                        id: Date.now() + Math.random(),
                        size_id,
                        color_id,
                        collar_id,
                        price: detailForm.price,
                        description: detailForm.description
                    });
                    count++;
                }
            }
        }
    }
    if (count > 0) {
        alert(`Đã thêm ${count} chi tiết sản phẩm.`);
    } else {
        alert('Không có chi tiết nào được thêm (có thể đã tồn tại).');
    }
    resetDetailForm()
}

function editDetail(detail) {
    selectedSizes.value = [detail.size_id]
    selectedColors.value = [detail.color_id]
    selectedCollars.value = [detail.collar_id]
    detailForm.price = detail.price
    detailForm.description = detail.description
    detailForm.id = detail.id
}

function deleteDetail(id) {
    currentProduct.value.details = currentProduct.value.details.filter(d => d.id !== id)
}

function resetDetailForm() {
    selectedSizes.value = []
    selectedColors.value = []
    selectedCollars.value = []
    detailForm.price = ''
    detailForm.description = ''
    detailForm.id = null
}
</script>

<template>
    <div class="container mt-4">
        <h3>Quản lý Sản phẩm</h3>

        <!-- Form sản phẩm -->
        <div class="card mb-4">
            <div class="card-body row g-3">
                <div class="col-md-4">
                    <label>Tên sản phẩm</label>
                    <input v-model="form.product_name" class="form-control" />
                </div>
                <div class="col-md-2">
                    <label>Thương hiệu</label>
                    <select v-model.number="form.brand_id" class="form-select">
                        <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Danh mục</label>
                    <select v-model.number="form.category_id" class="form-select">
                        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Đế giày</label>
                    <select v-model.number="form.sole_id" class="form-select">
                        <option v-for="s in soles" :key="s.id" :value="s.id">{{ s.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label d-block">Trạng thái</label>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" v-model.number="form.status" :value="1"
                            id="active" />
                        <label class="form-check-label" for="active">Hoạt động</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" v-model.number="form.status" :value="0"
                            id="inactive" />
                        <label class="form-check-label" for="inactive">Ẩn</label>
                    </div>
                </div>
                <div class="col-md-12">
                    <label>Mô tả</label>
                    <textarea v-model="form.description" class="form-control"></textarea>
                </div>
                <div class="col-md-12">
                    <button class="btn btn-success me-2" @click="saveProduct">{{ form.id ? 'Cập nhật' : 'Thêm'
                    }}</button>
                    <button class="btn btn-secondary" @click="resetForm">Làm mới</button>
                </div>
            </div>
        </div>

        <!-- Bảng sản phẩm -->
        <h5>Danh sách sản phẩm</h5>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>Tên</th>
                    <th>Thương hiệu</th>
                    <th>Danh mục</th>
                    <th>Đế</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="p in products" :key="p.id">
                    <td>{{ p.product_name }}</td>
                    <td>{{ getNameById(brands, p.brand_id) }}</td>
                    <td>{{ getNameById(categories, p.category_id) }}</td>
                    <td>{{ getNameById(soles, p.sole_id) }}</td>
                    <td>{{ p.status === 1 ? 'Hoạt động' : 'Ẩn' }}</td>
                    <td>
                        <button class="btn btn-warning btn-sm me-2" @click="editProduct(p)">Sửa</button>
                        <button class="btn btn-danger btn-sm me-2" @click="deleteProduct(p.id)">Xoá</button>
                        <button class="btn btn-info btn-sm" @click="openDetailModal(p)">Chi tiết</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <!-- Modal Chi tiết -->
        <div class="modal fade" id="productDetailModal" tabindex="-1">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Chi tiết sản phẩm: {{ currentProduct?.product_name }}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Form chi tiết -->
                        <div class="row g-2">
                            <div class="col-md-3">
                                <label class="form-label">Size</label>
                                <div class="form-check" v-for="s in sizes" :key="s.id">
                                    <input class="form-check-input" type="checkbox" :value="s.id"
                                        v-model="selectedSizes" />
                                    <label class="form-check-label">{{ s.id }}</label>
                                </div>
                            </div>

                            <div class="col-md-3">
                                <label class="form-label">Màu</label>
                                <div class="form-check" v-for="c in colors" :key="c.id">
                                    <input class="form-check-input" type="checkbox" :value="c.id"
                                        v-model="selectedColors" />
                                    <label class="form-check-label">{{ c.name }}</label>
                                </div>
                            </div>

                            <div class="col-md-3">
                                <label class="form-label">Cổ</label>
                                <div class="form-check" v-for="c in collars" :key="c.id">
                                    <input class="form-check-input" type="checkbox" :value="c.id"
                                        v-model="selectedCollars" />
                                    <label class="form-check-label">{{ c.name }}</label>
                                </div>
                            </div>

                            <div class="col-md-3">
                                <label>Giá</label>
                                <input type="number" class="form-control" v-model="detailForm.price" />
                                <label class="mt-2">Mô tả</label>
                                <input type="text" class="form-control" v-model="detailForm.description" />
                                <button class="btn btn-primary mt-2 w-100" @click="addOrUpdateDetail">
                                    {{ detailForm.id ? '✔ Cập nhật' : '+ Thêm' }}
                                </button>
                            </div>
                        </div>


                        <!-- Bảng chi tiết -->
                        <table class="table table-bordered table-sm mt-3">
                            <thead>
                                <tr>
                                    <th>Size</th>
                                    <th>Màu</th>
                                    <th>Cổ</th>
                                    <th>Giá</th>
                                    <th>Mô tả</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="d in currentProduct?.details || []" :key="d.id">
                                    <td>{{ getNameById(sizes, d.size_id) }}</td>
                                    <td>{{ getNameById(colors, d.color_id) }}</td>
                                    <td>{{ getNameById(collars, d.collar_id) }}</td>
                                    <td>{{ d.price }}</td>
                                    <td>{{ d.description }}</td>
                                    <td>
                                        <button class="btn btn-sm btn-warning me-1" @click="editDetail(d)">Sửa</button>
                                        <button class="btn btn-sm btn-danger" @click="deleteDetail(d.id)">Xoá</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.card {
    box-shadow: 0 0 8px #ccc;
    border-radius: 10px;
}
</style>
