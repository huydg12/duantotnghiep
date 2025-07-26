<script setup>
import { reactive, ref, computed, nextTick, onMounted } from 'vue'
import * as bootstrap from 'bootstrap'
import axios from 'axios'

const brands = ref([])
const categories = ref([])
const soles = ref([])
const sizes = ref([])
const colors = ref([])
const collars = ref([])
const products = ref([])
const productDetailList = ref([])
const selectedImages = ref([])
const previewUrls = ref([])
const mainImageIndex = ref(null)
const selectedDetailImages = ref([])
const currentDetailId = ref(null)
const newFileInput = document.createElement("input");
newFileInput.type = "file";
newFileInput.accept = "image/*";
const mainImageIndexViewer = ref(0)

async function openImageViewer(detail) {
    try {
        // Gán ID chi tiết sản phẩm hiện tại
        currentDetailId.value = detail.productDetailId;

        // Gọi API lấy ảnh theo productDetailId
        const response = await axios.get(`http://localhost:8080/image/show/${detail.productDetailId}`);
        selectedDetailImages.value = response.data;

        // Tìm index ảnh chính
        const mainIndex = selectedDetailImages.value.findIndex(img => img.isMain === true);
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0;

        // Mở modal
        const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('imageViewerModal'));
        modal.show();
    } catch (error) {
        console.error("Lỗi khi tải ảnh chi tiết:", error);
        selectedDetailImages.value = [];
    }
}

const editImage = async (detailId, imageIndex) => {
    const newFileInput = document.createElement("input");
    newFileInput.type = "file";
    newFileInput.accept = "image/*";

    newFileInput.onchange = async () => {
        const file = newFileInput.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);
        formData.append("productDetailId", detailId);
        formData.append("imageIndex", imageIndex);

        try {
            const response = await axios.post("http://localhost:8080/image/update-file", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            console.log("Ảnh đã cập nhật:", response.data);

            // Tải lại danh sách chi tiết để cập nhật ảnh
            await loadProductDetails(currentProduct.value.id);

            // Tìm lại chi tiết hiện tại để cập nhật ảnh trong modal
            const updatedDetail = productDetailList.value.find(
                (d) => d.productDetailId === detailId
            );
            if (updatedDetail) {
                selectedDetailImages.value = updatedDetail.images || [];
            }

        } catch (error) {
            console.error("Lỗi khi cập nhật ảnh:", error);
        }
    };

    newFileInput.click();
};


function handleMultipleImageChange(event) {
    const files = Array.from(event.target.files)
    if (!files.length) return

    selectedImages.value = files
    previewUrls.value = files.map(file => URL.createObjectURL(file))
    mainImageIndex.value = null
}

function selectMainImage(index) {
    mainImageIndex.value = index
}

async function uploadImages(detailId) {
    const formData = new FormData();
    selectedImages.value.forEach(file => formData.append('files', file));
    formData.append('productDetailId', detailId);
    formData.append('mainImageIndex', mainImageIndex.value ?? -1)
    try {
        await axios.post('http://localhost:8080/image/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert('Đã upload ảnh thành công!');
    } catch (err) {
        console.error('Lỗi upload ảnh:', err);
        alert('Không thể upload ảnh');
    }
}


const fetchBrands = async () => {
    try {
        const res = await axios.get('http://localhost:8080/brand/show')
        brands.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch brand:', error)
    }
}

const fetchCategories = async () => {
    try {
        const res = await axios.get('http://localhost:8080/style/show')
        categories.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch style:', error)
    }
}

const fetchSoles = async () => {
    try {
        const res = await axios.get('http://localhost:8080/sole/show')
        soles.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch sole:', error)
    }
}

const fetchSizes = async () => {
    try {
        const res = await axios.get('http://localhost:8080/size/show')
        sizes.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch size:', error)
    }
}

const fetchColors = async () => {
    try {
        const res = await axios.get('http://localhost:8080/color/show')
        colors.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch color:', error)
    }
}

const fetchCollars = async () => {
    try {
        const res = await axios.get('http://localhost:8080/collar/show')
        collars.value = res.data
    } catch (error) {
        console.error('❌ Lỗi khi fetch collar:', error)
    }
}

const fetchproduct = async () => {
    try {
        const res = await axios.get('http://localhost:8080/product/show')
        products.value = res.data
        productPage.value = 1 // Reset trang
    } catch (error) {
        console.error('Lỗi hiển thị sản phẩm:', error)
    }
}

const loadProductDetails = async (productId) => {
    try {
        const res = await axios.get(`http://localhost:8080/productDetail/show/${productId}`)
        productDetailList.value = res.data
        detailPage.value = 1 // Reset trang
    } catch (error) {
        console.error('Lỗi load chi tiết:', error)
    }
}

// Phân trang - sản phẩm
const productPage = ref(1)
const productsPerPage = 7
const paginatedProducts = computed(() => {
    const start = (productPage.value - 1) * productsPerPage
    return products.value.slice(start, start + productsPerPage)
})
const totalProductPages = computed(() => Math.ceil(products.value.length / productsPerPage))
const changeProductPage = (page) => {
    if (page >= 1 && page <= totalProductPages.value) productPage.value = page
}

// Phân trang - chi tiết
const detailPage = ref(1)
const detailsPerPage = 4
const paginatedDetails = computed(() => {
    const start = (detailPage.value - 1) * detailsPerPage
    return productDetailList.value.slice(start, start + detailsPerPage)
})
const totalDetailPages = computed(() => Math.ceil(productDetailList.value.length / detailsPerPage))
const changeDetailPage = (page) => {
    if (page >= 1 && page <= totalDetailPages.value) detailPage.value = page
}

// Form chính và chi tiết
const form = reactive({
    id: null,
    productName: '',
    brandId: null,
    categoryId: null,
    soleId: null,
    description: '',
    createdDate: new Date().toISOString().split('.')[0],
    status: 1,
    details: []
})

const detailForm = reactive({
    price: '',
    description: '',
    id: null
})

const currentProduct = ref(null)
const selectedSizes = ref([])
const selectedColors = ref([])
const selectedCollars = ref([])
const loading = ref(false)

// Các hàm thao tác
function getNameById(list, id) {
    id = Number(id)
    const found = list.find(i => i.id === id)
    return found ? (found.name ?? found.id ?? 'N/A') : 'N/A'
}

function getIdByName(list, value) {
    if (!value) return null
    if (typeof value === 'object' && value.id) return value.id
    const found = list.find(i => i.name === value || i.eu === value)
    return found?.id || null
}

function resetForm() {
    Object.assign(form, {
        id: null,
        productName: '',
        brandId: null,
        categoryId: null,
        soleId: null,
        description: '',
        status: 1,
        details: []
    })
}

function resetDetailForm() {
    detailForm.id = null
    detailForm.price = null
    detailForm.description = ''
    selectedSizes.value = []
    selectedColors.value = []
    selectedCollars.value = []
    previewUrls.value = []
    selectedImages.value = []
}

const user = ref(JSON.parse(localStorage.getItem("user") || "{}"))

function getVietnamTimeWithoutSeconds() {
    const date = new Date()
    date.setHours(date.getHours() + 7) // Múi giờ Việt Nam

    const yyyy = date.getFullYear()
    const MM = String(date.getMonth() + 1).padStart(2, '0')
    const dd = String(date.getDate()).padStart(2, '0')
    const HH = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    return `${yyyy}-${MM}-${dd}T${HH}:${mm}`
}

async function saveProduct() {
    if (!form.productName) return alert('Nhập tên sản phẩm');

    // Kiểm tra trùng tên khi thêm mới
    const isDuplicate = products.value.some(
        (p) => p.productName.trim().toLowerCase() === form.productName.trim().toLowerCase()
    );

    if (!form.id && isDuplicate) {
        alert('Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.');
        return;
    }

    const payload = { ...form, createdBy: user.value.fullName };

    try {
        if (form.id) {
            await axios.put(`http://localhost:8080/product/update/${form.id}`, payload);
            alert('Cập nhật thành công');
        } else {
            form.createdDate = getVietnamTimeWithoutSeconds();
            await axios.post('http://localhost:8080/product/add', payload);
            alert('Thêm sản phẩm thành công');
        }

        resetForm();
        fetchproduct();
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error);
        alert('Có lỗi khi lưu sản phẩm');
    }
}

function editProduct(p) {
    Object.assign(form, JSON.parse(JSON.stringify(p)))
}

async function deleteProduct(id) {
    if (!confirm('Bạn có chắc muốn xoá sản phẩm này?')) return
    try {
        await axios.delete(`http://localhost:8080/product/delete/${id}`)
        alert('Đã xoá sản phẩm')
        fetchproduct()
    } catch (error) {
        console.error('Lỗi xoá sản phẩm:', error)
        alert('Không thể xoá sản phẩm')
    }
}

async function openDetailModal(product) {
    currentProduct.value = product
    await loadProductDetails(product.id)
    await nextTick()
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('productDetailModal'))
    modal.show()
}

async function saveProductDetails() {
    if (loading.value) return;
    loading.value = true;

    if (!currentProduct.value?.id) {
        loading.value = false;
        return;
    }

    if (!detailForm.price || !detailForm.description ||
        selectedSizes.value.length === 0 || selectedColors.value.length === 0 || selectedCollars.value.length === 0) {
        alert('Vui lòng điền đầy đủ thông tin trước khi lưu');
        loading.value = false;
        return;
    }

    try {
        if (detailForm.id) {
            // === Cập nhật chi tiết ===
            const updatedDetail = {
                product: { id: currentProduct.value.id },
                size: { id: selectedSizes.value[0] },
                color: { id: selectedColors.value[0] },
                collar: { id: selectedCollars.value[0] },
                price: detailForm.price,
                description: detailForm.description,
                status: 1
            };

            await axios.put(`http://localhost:8080/productDetail/update/${detailForm.id}`, updatedDetail);
            alert('Cập nhật chi tiết thành công');
        } else {
            // === Thêm mới ===
            const newDetails = [];

            for (const size of selectedSizes.value) {
                for (const color of selectedColors.value) {
                    for (const collar of selectedCollars.value) {
                        newDetails.push({
                            product: { id: currentProduct.value.id },
                            size: { id: size },
                            color: { id: color },
                            collar: { id: collar },
                            price: detailForm.price,
                            description: detailForm.description,
                            status: 1,
                        });
                    }
                }
            }

            const addedDetailIds = [];
            for (const detail of newDetails) {
                const response = await axios.post('http://localhost:8080/productDetail/add', detail);
                addedDetailIds.push(response.data.id);
            }

            for (const detailId of addedDetailIds) {
                if (selectedImages.value.length > 0) {
                    await uploadImages(detailId);
                }
            }

            alert('Lưu chi tiết và ảnh thành công!');
        }

        resetDetailForm();
        await loadProductDetails(currentProduct.value.id);
    } catch (err) {
        console.error('Lỗi khi lưu chi tiết:', err);
        alert('Không thể lưu chi tiết!');
    } finally {
        loading.value = false;
    }
}



function editDetail(detail) {
    selectedSizes.value = [detail.size?.id ?? getIdByName(sizes.value, detail.size)]
    selectedColors.value = [detail.color?.id ?? getIdByName(colors.value, detail.color)]
    selectedCollars.value = [detail.collar?.id ?? getIdByName(collars.value, detail.collar)]
    detailForm.price = detail.price
    detailForm.description = detail.description
    detailForm.id = detail.productDetailId
}

async function deleteDetail(id) {
    if (!confirm('Bạn có chắc muốn xoá chi tiết này?')) return
    try {
        await axios.delete(`http://localhost:8080/productDetail/delete/${id}`)
        alert('Đã xoá sản phẩm chi tiết')
        await loadProductDetails(currentProduct.value.id)
    } catch (err) {
        console.error('Lỗi xoá sản phẩm chi tiết:', err)
        alert('Không thể xoá chi tiết sản phẩm')
    }
}

async function setMainImage(img) {
    try {
        await axios.put(`http://localhost:8080/image/set-main/${img.id}`, null, {
            params: {
                productDetailId: img.productDetailId
            }
        });

        // Gọi lại API để load danh sách ảnh mới
        const response = await axios.get(`http://localhost:8080/image/show/${img.productDetailId}`);
        selectedDetailImages.value = response.data;

        // Cập nhật ảnh chính (main)
        const mainIndex = selectedDetailImages.value.findIndex(i => i.main  === true);
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0;

        alert("Đã đặt ảnh chính thành công.");
    } catch (err) {
        console.error("Lỗi khi đặt ảnh chính:", err);
        alert("Không thể đặt ảnh chính.");
    }
}

async function deleteImage(imageId, productDetailId) {
    if (!confirm("Bạn có chắc muốn xoá ảnh này?")) return;
    try {
        // Gửi yêu cầu xóa ảnh theo ID
        await axios.delete(`http://localhost:8080/image/delete/${imageId}`);

        // Sau khi xóa, gọi lại API lấy danh sách ảnh mới theo productDetailId
        const response = await axios.get(`http://localhost:8080/image/show/${productDetailId}`);
        selectedDetailImages.value = response.data;

        // Tìm lại ảnh chính (main) mới nếu còn
        const mainIndex = selectedDetailImages.value.findIndex(i => i.main === true);
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0;

        alert("Đã xoá ảnh thành công.");
    } catch (err) {
        console.error("Lỗi khi xoá ảnh:", err);
        alert("Không thể xoá ảnh.");
    }
}


// Fetch on mounted
onMounted(() => {
    fetchproduct()
    fetchBrands()
    fetchCategories()
    fetchSoles()
    fetchSizes()
    fetchColors()
    fetchCollars()
})
</script>

<template>
    <div class="container mt-4">
        <h3>Quản lý Sản phẩm</h3>

        <!-- Form sản phẩm -->
        <div class="card mb-4">
            <div class="card-body row g-3">
                <div class="col-md-4">
                    <label>Tên sản phẩm</label>
                    <input v-model="form.productName" class="form-control" />
                </div>
                <div class="col-md-2">
                    <label>Thương hiệu</label>
                    <select v-model.number="form.brandId" class="form-select">
                        <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Danh mục</label>
                    <select v-model.number="form.categoryId" class="form-select">
                        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Đế giày</label>
                    <select v-model.number="form.soleId" class="form-select">
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
                    <button class="btn btn-success me-2" @click="saveProduct">{{ form.id ? 'Cập nhật' :
                        'Thêm' }}</button>
                    <button class="btn btn-secondary" @click="resetForm">Làm mới</button>
                </div>
            </div>
        </div>

        <!-- Bảng sản phẩm -->
        <h5>Danh sách sản phẩm</h5>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên</th>
                    <th>Thương hiệu</th>
                    <th>Danh mục</th>
                    <th>Đế</th>
                    <th>Trạng thái</th>
                    <!-- <th>Ngày Tạo</th> -->
                    <th>Mô Tả</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="p in paginatedProducts" :key="p.id">
                    <td>{{ p.id }}</td>
                    <td>{{ p.productName }}</td>
                    <td>{{ getNameById(brands, p.brandId) }}</td>
                    <td>{{ getNameById(categories, p.categoryId) }}</td>
                    <td>{{ getNameById(soles, p.soleId) }}</td>
                    <td>{{ p.status === 1 ? 'Hoạt động' : 'Ẩn' }}</td>
                    <!-- <td>{{ p.createdDate }}</td> -->
                    <td>{{ p.description }}</td>
                    <td>
                        <button class="btn btn-warning btn-sm me-2" @click="editProduct(p)">Sửa</button>
                        <button class="btn btn-danger btn-sm me-2" @click="deleteProduct(p.id)">Xoá</button>
                        <button class="btn btn-info btn-sm" @click="openDetailModal(p)">Chi tiết</button>
                    </td>
                </tr>
            </tbody>
        </table>
        <!-- Pagination sản phẩm -->
        <nav class="mt-3">
            <ul class="pagination justify-content-center">
                <li class="page-item" :class="{ disabled: productPage === 1 }">
                    <button class="page-link" @click="changeProductPage(productPage - 1)">«</button>
                </li>
                <li v-for="page in totalProductPages" :key="page" :class="{ active: page === productPage }"
                    class="page-item">
                    <button class="page-link" @click="changeProductPage(page)">{{ page }}</button>
                </li>
                <li class="page-item" :class="{ disabled: productPage === totalProductPages }">
                    <button class="page-link" @click="changeProductPage(productPage + 1)">»</button>
                </li>
            </ul>
        </nav>

        <!-- Modal Chi tiết -->
        <div class="modal fade" id="productDetailModal" tabindex="-1">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Chi tiết sản phẩm: {{ currentProduct?.productName }}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>

                    <div class="modal-body">
                        <!-- Form chi tiết -->
                        <div class="row g-3">
                            <!-- Các chọn lọc -->
                            <div class="col-md-4">
                                <label class="form-label fw-semibold">Màu sắc</label>
                                <select class="form-select" multiple v-model="selectedColors">
                                    <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.name }}</option>
                                </select>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label fw-semibold">Kích cỡ</label>
                                <select class="form-select" multiple v-model="selectedSizes">
                                    <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.eu }}</option>
                                </select>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label fw-semibold">Kiểu cổ</label>
                                <select class="form-select" multiple v-model="selectedCollars">
                                    <option v-for="c in collars" :key="c.id" :value="c.id">{{ c.name }}</option>
                                </select>
                            </div>
                        </div>

                        <div class="row g-3 gx-4 mt-3">
                            <!-- Cột trái: Thông tin -->
                            <div class="col-md-6">
                                <label>Giá</label>
                                <input type="number" class="form-control" v-model="detailForm.price" />

                                <label class="mt-2">Mô tả</label>
                                <input type="text" class="form-control" v-model="detailForm.description" />

                                <button class="btn btn-primary mt-3 w-100" @click="saveProductDetails">
                                    {{ detailForm.id ? '✔ Cập nhật' : 'Thêm' }}
                                </button>

                                <button class="btn btn-secondary mt-2 w-100" @click="resetDetailForm">Làm mới</button>
                            </div>

                            <!-- Cột phải: Chọn ảnh -->
                            <div class="col-md-6">
                                <label>Ảnh sản phẩm (có thể chọn nhiều)</label>
                                <input type="file" multiple accept="image/*" class="form-control"
                                    @change="handleMultipleImageChange" 
                                    />

                                <div class="mt-3 d-flex flex-wrap gap-2 justify-content-start"
                                    v-if="previewUrls.length">
                                    <div v-for="(url, index) in previewUrls" :key="index" class="position-relative"
                                        style="cursor: pointer;" @click="selectMainImage(index)">
                                        <img :src="url" alt="Preview" class="img-thumbnail" :style="{
                                            width: '120px',
                                            height: '120px',
                                            objectFit: 'cover',
                                            border: mainImageIndex === index ? '3px solid red' : '1px solid #ccc',
                                            boxShadow: mainImageIndex === index ? '0 0 6px red' : 'none'
                                        }" />
                                        <span v-if="mainImageIndex === index"
                                            class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                                            Chính
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- Modal Xem/Sửa ảnh - căn giữa -->
                        <div class="modal fade" id="imageViewerModal" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered modal-lg">
                                <div class="modal-content shadow rounded-4">
                                    <div class="modal-header bg-primary text-white">
                                        <h5 class="modal-title">Ảnh chi tiết sản phẩm</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="d-flex flex-wrap gap-3 justify-content-center">
                                            <!-- Chỉ 1 vòng lặp duy nhất -->
                                            <div v-for="(img, index) in selectedDetailImages" :key="index"
                                                class="d-flex flex-column align-items-center" style="width: 140px;">
                                                <img :src="img.url" alt="Ảnh" class="img-thumbnail shadow"
                                                    style="width: 120px; height: 120px; object-fit: cover; border-radius: 10px;"
                                                    :style="{
                                                        border: mainImageIndexViewer === index ? '3px solid red' : '1px solid #ccc'
                                                    }" @click="setMainImage(img)" />

                                                <span v-if="mainImageIndexViewer === index" class="badge bg-danger mt-1"
                                                    style="font-size: 12px;">
                                                    Chính
                                                </span>

                                                <div class="btn-group mt-2" role="group">
                                                    <button class="btn btn-sm btn-outline-primary"
                                                        @click="editImage(img.id, index)">
                                                        <i class="bi bi-pencil"></i>
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-danger"
                                                        @click="deleteImage(img.id, img.productDetailId)">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Bảng chi tiết -->
                        <div class="table-responsive mt-4">
                            <table class="table table-bordered table-sm">
                                <thead class="table-light text-center">
                                    <tr>
                                        <th>Size</th>
                                        <th>Màu</th>
                                        <th>Cổ</th>
                                        <th>Giá</th>
                                        <th>Mô tả</th>
                                        <th>Ảnh</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody class="align-middle">
                                    <tr v-for="productDetail in paginatedDetails" :key="productDetail.productDetailId">
                                        <td class="text-center">{{ productDetail.size }}</td>
                                        <td>{{ productDetail.color }}</td>
                                        <td>{{ productDetail.collar }}</td>
                                        <td class="text-center">{{ productDetail.price }}</td>
                                        <td>{{ productDetail.description }}</td>
                                        <td class="text-center">
                                            <div v-if="productDetail.images && productDetail.images.length">
                                                <img :src="productDetail.images[0]" alt="Ảnh chính"
                                                    class="img-thumbnail"
                                                    style="width: 60px; height: 60px; object-fit: cover; cursor: pointer;"
                                                    @click="openImageViewer(productDetail)" />
                                            </div>
                                        </td>
                                        <td class="text-center">
                                            <button class="btn btn-sm btn-warning me-1"
                                                @click="editDetail(productDetail)">Sửa</button>
                                            <button class="btn btn-sm btn-danger"
                                                @click="deleteDetail(productDetail.productDetailId)">Xoá</button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <!-- Pagination chi tiết -->
                        <nav class="mt-3">
                            <ul class="pagination justify-content-center">
                                <li class="page-item" :class="{ disabled: detailPage === 1 }">
                                    <button class="page-link" @click="changeDetailPage(detailPage - 1)">«</button>
                                </li>
                                <li v-for="page in totalDetailPages" :key="page"
                                    :class="{ active: page === detailPage }" class="page-item">
                                    <button class="page-link" @click="changeDetailPage(page)">{{ page }}</button>
                                </li>
                                <li class="page-item" :class="{ disabled: detailPage === totalDetailPages }">
                                    <button class="page-link" @click="changeDetailPage(detailPage + 1)">»</button>
                                </li>
                            </ul>
                        </nav>
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

.pagination .active .page-link {
    background-color: #198754;
    border-color: #198754;
    color: #fff;
}

.form-check-inline {
    margin-right: 10px;
    margin-bottom: 5px;
    min-width: 60px;
}
</style>
