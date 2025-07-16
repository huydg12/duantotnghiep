<script setup>
import { ref, onMounted } from "vue";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

// ==== OPTIONS ====
const brands = ref([
    { id: 1, name: "Adidas" },
    { id: 2, name: "Nike" },
]);
const categories = ref([
    { id: 1, name: "Chạy bộ" },
    { id: 2, name: "Thể thao" },
]);
const soles = ref([
    { id: 1, name: "Cao su" },
    { id: 2, name: "Nhựa" },
]);
const collars = ref([
    { id: 1, name: "Cổ thấp" },
    { id: 2, name: "Cổ cao" },
]);
const sizes = ref([
    { id: 1, name: "38" },
    { id: 2, name: "39" },
    { id: 3, name: "40" },
]);
const colors = ref([
    { id: 1, name: "Đen" },
    { id: 2, name: "Trắng" },
    { id: 3, name: "Đỏ" },
]);

// ==== STATE ====
const products = ref([]);
const selectedProduct = ref(null);
const formProduct = ref({
    productName: "",
    brandId: null,
    categoryId: null,
    soleId: null,
    description: "",
    status: 1,
});
const detailForm = ref({
    collarId: null,
    sizeIds: [],
    colorIds: [],
    price: null,
    description: "",
});

// ==== INIT DATA ====
onMounted(() => {
    products.value = [
        {
            id: 1,
            productName: "Giày Adidas",
            brandId: 1,
            categoryId: 1,
            soleId: 1,
            description: "Mẫu chạy bộ",
            status: 1,
            details: [],
        },
    ];
});

// ==== UTILS ====
const getNameById = (arr, id) => arr.find(i => i.id === id)?.name || "";

// ==== ACTIONS ====
const addProduct = () => {
    const exists = products.value.some(p =>
        p.productName.trim().toLowerCase() === formProduct.value.productName.trim().toLowerCase() &&
        p.brandId === formProduct.value.brandId &&
        p.categoryId === formProduct.value.categoryId &&
        p.soleId === formProduct.value.soleId
    );

    if (exists) {
        alert("❌ Sản phẩm đã tồn tại!");
        return;
    }

    const newId = products.value.length + 1;
    products.value.push({
        id: newId,
        ...formProduct.value,
        details: [],
    });

    formProduct.value = {
        productName: "",
        brandId: null,
        categoryId: null,
        soleId: null,
        description: "",
        status: 1,
    };
};

const selectProduct = (p) => {
    selectedProduct.value = p;
};

const addDetailToProduct = () => {
    const { collarId, sizeIds, colorIds, price, description } = detailForm.value;

    if (!collarId || sizeIds.length === 0 || colorIds.length === 0 || !price) {
        alert("❌ Thiếu thông tin chi tiết!");
        return;
    }

    let addedCount = 0;

    for (const sizeId of sizeIds) {
        for (const colorId of colorIds) {
            const duplicate = selectedProduct.value.details.some(d =>
                d.collarId === collarId &&
                d.sizeId === sizeId &&
                d.colorId === colorId
            );

            if (!duplicate) {
                selectedProduct.value.details.push({
                    collarId,
                    sizeId,
                    colorId,
                    price,
                    description,
                });
                addedCount++;
            }
        }
    }

    if (addedCount === 0) {
        alert("⚠️ Tất cả chi tiết đã tồn tại!");
    } else {
        alert(`✅ Đã thêm ${addedCount} chi tiết mới.`);
    }

    detailForm.value = {
        collarId: null,
        sizeIds: [],
        colorIds: [],
        price: null,
        description: "",
    };
};
</script>

<template>
    <div class="container py-4">
        <h3 class="text-center mb-4">👟 Quản lý sản phẩm</h3>

        <!-- THÊM SẢN PHẨM -->
        <div class="border rounded p-3 mb-4 bg-light">
            <h5>➕ Thêm sản phẩm</h5>
            <div class="row g-2">
                <div class="col-md-3">
                    <input v-model="formProduct.productName" class="form-control" placeholder="Tên sản phẩm" />
                </div>
                <div class="col-md-2">
                    <v-select v-model="formProduct.brandId" :options="brands" label="name" :reduce="i => i.id"
                        placeholder="Thương hiệu" />
                </div>
                <div class="col-md-2">
                    <v-select v-model="formProduct.categoryId" :options="categories" label="name" :reduce="i => i.id"
                        placeholder="Danh mục" />
                </div>
                <div class="col-md-2">
                    <v-select v-model="formProduct.soleId" :options="soles" label="name" :reduce="i => i.id"
                        placeholder="Đế giày" />
                </div>
                <div class="col-md-1">
                    <select v-model="formProduct.status" class="form-select">
                        <option :value="1">Hoạt động</option>
                        <option :value="0">Ẩn</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button class="btn btn-primary w-100" @click="addProduct">Lưu</button>
                </div>
            </div>
        </div>

        <!-- DANH SÁCH SẢN PHẨM -->
        <div class="border rounded p-3 mb-4">
            <h5>📦 Danh sách sản phẩm</h5>
            <table class="table table-bordered text-center mt-2 align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Tên</th>
                        <th>Thương hiệu</th>
                        <th>Danh mục</th>
                        <th>Đế</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="p in products" :key="p.id">
                        <td>{{ p.productName }}</td>
                        <td>{{ getNameById(brands, p.brandId) }}</td>
                        <td>{{ getNameById(categories, p.categoryId) }}</td>
                        <td>{{ getNameById(soles, p.soleId) }}</td>
                        <td>
                            <span :class="p.status ? 'badge bg-success' : 'badge bg-secondary'">
                                {{ p.status ? 'Hoạt động' : 'Ẩn' }}
                            </span>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-info" @click="selectProduct(p)">Chi tiết</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- CHI TIẾT SẢN PHẨM -->
        <div v-if="selectedProduct" class="border rounded p-3">
            <h5>🧩 Chi tiết: {{ selectedProduct.productName }}</h5>

            <!-- FORM ADD DETAIL -->
            <div class="row g-3 mt-3">
                <!-- Combo chọn Cổ -->
                <div class="col-12 col-md-4">
                    <label class="form-label">Cổ giày</label>
                    <v-select v-model="detailForm.collarId" :options="collars" label="name" :reduce="i => i.id"
                       multiple placeholder="Chọn cổ" />
                    <div class="mt-1">
                        <strong>Cổ:</strong>
                        <span v-if="detailForm.collarId">
                            {{ getNameById(collars, detailForm.collarId) }}
                        </span>
                    </div>
                </div>

                <!-- Combo chọn Size (nhiều) -->
                <div class="col-12 col-md-4">
                    <label class="form-label">Size (nhiều)</label>
                    <v-select v-model="detailForm.sizeIds" :options="sizes" label="name" :reduce="i => i.id" multiple
                        placeholder="Chọn size" />
                    <div class="mt-1">
                        <strong>Size:</strong>
                        <span v-if="detailForm.sizeIds?.length">
                            <span v-for="id in detailForm.sizeIds" :key="id" class="badge bg-primary me-1">
                                {{ getNameById(sizes, id) }}
                            </span>
                        </span>
                    </div>
                </div>

                <!-- Combo chọn Màu (nhiều) -->
                <div class="col-12 col-md-4">
                    <label class="form-label">Màu sắc (nhiều)</label>
                    <v-select v-model="detailForm.colorIds" :options="colors" label="name" :reduce="i => i.id" multiple
                        placeholder="Chọn màu" />
                    <div class="mt-1">
                        <strong>Màu:</strong>
                        <span v-if="detailForm.colorIds?.length">
                            <span v-for="id in detailForm.colorIds" :key="id" class="badge bg-success me-1">
                                {{ getNameById(colors, id) }}
                            </span>
                        </span>
                    </div>
                </div>


                <div class="col-12 col-md-4">
                    <label class="form-label">Giá</label>
                    <input v-model="detailForm.price" type="number" class="form-control" placeholder="Giá" />
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label">Mô tả</label>
                    <input v-model="detailForm.description" class="form-control" placeholder="Mô tả" />
                </div>
                <div class="col-12 col-md-2 d-flex align-items-end">
                    <button class="btn btn-success w-100" @click="addDetailToProduct">➕ Thêm chi tiết</button>
                </div>
            </div>

            <!-- DANH SÁCH CHI TIẾT -->
            <table class="table table-striped table-bordered text-center mt-3">
                <thead>
                    <tr>
                        <th>Cổ</th>
                        <th>Size</th>
                        <th>Màu</th>
                        <th>Giá</th>
                        <th>Mô tả</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(d, i) in selectedProduct.details" :key="i">
                        <td>{{ getNameById(collars, d.collarId) }}</td>
                        <td>{{ getNameById(sizes, d.sizeId) }}</td>
                        <td>{{ getNameById(colors, d.colorId) }}</td>
                        <td>{{ d.price }}</td>
                        <td>{{ d.description }}</td>
                    </tr>
                    <tr v-if="selectedProduct.details.length === 0">
                        <td colspan="5" class="text-muted">Chưa có chi tiết nào.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>
<style scoped>
.container {
    max-width: 900px;
    margin: auto;
}

/* Fix chiều cao và bo góc cho vue-select */
.v-select {
    font-size: 14px;
    min-height: 38px;
    border-radius: 0.375rem;
    /* bo tròn giống bootstrap */
    border: 1px solid #ced4da;
    /* viền màu giống input bootstrap */
}

/* Bỏ shadow mặc định của vue-select */
.v-select .vs__dropdown-toggle {
    border-radius: 0.375rem;
    min-height: 38px;
    padding: 0 12px;
    border: 1px solid #ced4da;
    transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

/* Hover và focus giống input bootstrap */
.v-select .vs__dropdown-toggle:hover {
    border-color: #86b7fe;
}

.v-select.vs--open .vs__dropdown-toggle,
.v-select .vs__dropdown-toggle:focus {
    border-color: #86b7fe;
    box-shadow: 0 0 0 0.25rem rgb(13 110 253 / 0.25);
    outline: 0;
}

/* Phần placeholder màu xám nhạt */
.v-select .vs__placeholder {
    color: #6c757d;
    font-size: 14px;
}

/* Text input trong vue-select */
.v-select input[type="search"] {
    font-size: 14px;
    padding: 0;
    margin: 0;
}

/* Dropdown menu bo góc */
.v-select .vs__dropdown-menu {
    border-radius: 0.375rem;
    box-shadow: 0 0.5rem 1rem rgb(0 0 0 / 0.15);
    border: 1px solid #ced4da;
}

/* Item trong dropdown */
.v-select .vs__dropdown-option {
    font-size: 14px;
    padding: 6px 12px;
    cursor: pointer;
}

.v-select .vs__dropdown-option--highlight {
    background-color: #0d6efd;
    color: white;
}

/* Multiselect tags (nếu dùng) */
.v-select .vs__selected {
    background-color: #0d6efd;
    color: white;
    border-radius: 0.25rem;
    padding: 0 6px;
    margin-right: 4px;
    font-size: 13px;
}
</style>