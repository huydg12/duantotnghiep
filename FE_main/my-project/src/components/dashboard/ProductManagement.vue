<script setup>
import { reactive, ref, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import * as bootstrap from 'bootstrap'
import axios from 'axios'

// Axios
const API = axios.create({ baseURL: 'http://localhost:8080' })

// State
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
const mainImageIndexViewer = ref(0)

const currentProduct = ref(null)
const selectedSizes = ref([])
const selectedColors = ref([])
const selectedCollars = ref([])
const loading = ref(false)

const productDetailModalRef = ref(null)
const imageViewerModalRef = ref(null)

const productFilters = ref({
    q: '',
    brandId: 'all',
    categoryId: 'all',
    soleId: 'all',
    status: 'all',
})

const detailFilters = ref({
    sizeId: 'all',
    colorId: 'all',
    collarId: 'all',
    status: 'all',
})

const colorSearch = ref('')
const sizeSearch = ref('')
const collarSearch = ref('')

// Map cache
const brandMap = computed(() => new Map(brands.value.map(x => [Number(x.id), x.name])))
const categoryMap = computed(() => new Map(categories.value.map(x => [Number(x.id), x.name])))
const soleMap = computed(() => new Map(soles.value.map(x => [Number(x.id), x.name])))

function getNameById(list, id) {
    const nId = Number(id)
    if (list === brands.value) return brandMap.value.get(nId) ?? 'N/A'
    if (list === categories.value) return categoryMap.value.get(nId) ?? 'N/A'
    if (list === soles.value) return soleMap.value.get(nId) ?? 'N/A'
    const found = list.find(i => Number(i.id) === nId)
    return found ? (found.name ?? found.eu ?? 'N/A') : 'N/A'
}

function getIdByName(list, value) {
    if (!value) return null
    if (typeof value === 'object' && value.id) return value.id
    const found = list.find(i => i.name === value || i.eu === value)
    return found?.id || null
}

// Form
const form = reactive({
    id: null,
    productName: '',
    brandId: null,
    categoryId: null,
    soleId: null,
    description: '',
    createdDate: new Date().toISOString().split('.')[0],
    active: 1,
    details: []
})
const detailForm = reactive({ price: '', description: '', id: null })

// Helpers
const norm = (v) =>
    (v ?? '').toString().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim()

const asArray = (v) => Array.isArray(v) ? v : (v == null || v === '' ? [] : [v])

const user = ref(JSON.parse(localStorage.getItem("user") || "{}"))
const safeUserName = () => user.value?.fullName || 'System'

function getVietnamTimeWithoutSeconds() {
    const d = new Date(), pad = n => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const toBoolish = (v) => {
    if (v === true || v === 1) return true
    if (v === false || v === 0) return false
    if (v == null) return false
    const s = String(v).trim().toLowerCase()
    return s === 'true' || s === '1' || s === 'active'
}
const rowActive = (row, fallback = true) => {
    if (!row || typeof row !== 'object') return false
    const keys = ['active', 'isActive', 'status', 'is_active', 'IS_ACTIVE']
    for (const k of keys) if (k in row) return toBoolish(row[k])
    return fallback
}

// API
const fetchBrands = async () => { const { data } = await API.get('/brand/show'); brands.value = (data || []).filter(rowActive) }
const fetchCategories = async () => { const { data } = await API.get('/category/show'); categories.value = (data || []).filter(rowActive) }
const fetchSoles = async () => { const { data } = await API.get('/sole/show'); soles.value = (data || []).filter(rowActive) }
const fetchSizes = async () => { const { data } = await API.get('/size/show'); sizes.value = (data || []).filter(rowActive) }
const fetchColors = async () => { const { data } = await API.get('/color/show'); colors.value = (data || []).filter(rowActive) }
const fetchCollars = async () => { const { data } = await API.get('/collar/show'); collars.value = (data || []).filter(rowActive) }
const fetchProducts = async () => { products.value = (await API.get('/product/show')).data; productPage.value = 1 }

// CRUD Sản phẩm
function resetForm() {
    Object.assign(form, {
        id: null, productName: '', brandId: null, categoryId: null, soleId: null,
        description: '', active: 1, details: []
    })
}

async function saveProduct() {
    if (!form.productName) return alert('Nhập tên sản phẩm')
    const isDup = products.value.some(p => (p.productName || '').trim().toLowerCase() === form.productName.trim().toLowerCase())
    if (!form.id && isDup) { alert('Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.'); return }
    const payload = { ...form, createdBy: safeUserName() }
    try {
        if (form.id) { await API.put(`/product/update/${form.id}`, payload); alert('Cập nhật thành công') }
        else { form.createdDate = getVietnamTimeWithoutSeconds(); await API.post('/product/add', payload); alert('Thêm sản phẩm thành công') }
        resetForm(); await fetchProducts()
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error)
        alert('Có lỗi khi lưu sản phẩm')
    }
}

function editProduct(p) { Object.assign(form, JSON.parse(JSON.stringify(p))) }

async function changeStatus(id) {
    if (!confirm('Bạn có chắc muốn chuyển trạng thái sản phẩm này?')) return
    try {
        await API.put(`/product/updateStatus/${id}`, { id })
        alert('Đã chuyển trạng thái sản phẩm')
        await fetchProducts()
    } catch (error) {
        console.error('Lỗi chuyển trạng thái sản phẩm:', error.response ? error.response.data : error.message)
        alert('Không thể chuyển trạng thái sản phẩm')
    }
}

const productsFiltered = computed(() => {
    const q = norm(productFilters.value.q)
    const b = productFilters.value.brandId
    const c = productFilters.value.categoryId
    const s = productFilters.value.soleId
    const st = productFilters.value.status

    return products.value.filter(p => {
        const okText = !q || norm(p.productName).includes(q)

        const okBrand = b === 'all' || Number(p.brandId) === Number(b)
        const okCat = c === 'all' || Number(p.categoryId) === Number(c)
        const okSole = s === 'all' || Number(p.soleId) === Number(s)

        const active = rowActive(p, true)
        const okStatus =
            st === 'all' ||
            (st === 'active' && active) ||
            (st === 'inactive' && !active)

        return okText && okBrand && okCat && okSole && okStatus
    })
})

const productPage = ref(1)
const productsPerPage = 7
const totalProductPages = computed(() => Math.max(1, Math.ceil(productsFiltered.value.length / productsPerPage)))
const paginatedProducts = computed(() => {
    const start = (productPage.value - 1) * productsPerPage
    return productsFiltered.value.slice(start, start + productsPerPage)
})
const changeProductPage = (p) => { if (p >= 1 && p <= totalProductPages.value) productPage.value = p }
watch(productFilters, () => { productPage.value = 1 }, { deep: true })

const loadProductDetails = async (productId) => {
    const list = (await API.get(`/productDetail/show/${productId}`)).data || []
    productDetailList.value = list
    detailPage.value = 1
    await hydrateCurrentPage(true)
}
async function openDetailModal(product) {
    currentProduct.value = product
    await loadProductDetails(product.id)
    await nextTick()
    bootstrap.Modal.getOrCreateInstance(productDetailModalRef.value).show()
}

function toIdSafe(val, listRef) {
    if (val == null) return null
    if (typeof val === 'object' && 'id' in val) return Number(val.id)
    const byName = getIdByName(listRef.value, val)
    if (byName != null) return Number(byName)
    const n = Number(val); return Number.isNaN(n) ? null : n
}

function tripleKey(sizeId, colorId, collarId) { return `${Number(sizeId)}|${Number(colorId)}|${Number(collarId)}` }

function tripleFromRow(row) {
    const sizeId = toIdSafe(row.size, sizes)
    const colorId = toIdSafe(row.color, colors)
    const collarId = toIdSafe(row.collar, collars)
    return { sizeId, colorId, collarId, key: tripleKey(sizeId, colorId, collarId) }
}

const existingDetailKeySet = computed(() => {
    const set = new Set()
    for (const d of productDetailList.value) {
        const { sizeId, colorId, collarId } = tripleFromRow(d)
        if (sizeId != null && colorId != null && collarId != null) {
            set.add(tripleKey(sizeId, colorId, collarId))
        }
    }
    return set
})

function detailIdOf(row) {
    const id = row?.productDetailId ?? row?.id ?? row?.detailId ?? row?.PRODUCT_DETAIL_ID
    if (id == null) { console.warn('⚠️ Thiếu ID chi tiết:', row); return undefined }
    return Number(id)
}

const isMainFlag = (v) => v === true || v === 1 || v === '1' || v === 'true'

function fileBaseName(name) { return (name ?? '').trim().toLowerCase() }

function imgBaseName(img) {
    const known = img.fileName || img.filename || img.name
    if (known) return fileBaseName(known)
    if (img.url) {
        const tail = img.url.split('/').pop().split('?')[0]
        return fileBaseName(tail)
    }
    return ''
}

function resetDetailForm() {
    detailForm.id = null
    detailForm.price = null
    detailForm.description = ''
    selectedSizes.value = []
    selectedColors.value = null
    selectedCollars.value = null
    previewUrls.value.forEach(u => URL.revokeObjectURL(u))
    previewUrls.value = []
    selectedImages.value = []
    mainImageIndex.value = null
}

function editDetail(detail) {
    selectedSizes.value = [detail.size?.id ?? getIdByName(sizes.value, detail.size)]
    selectedColors.value = (detail.color?.id ?? getIdByName(colors.value, detail.color))
    selectedCollars.value = (detail.collar?.id ?? getIdByName(collars.value, detail.collar))
    detailForm.price = detail.price
    detailForm.description = detail.description
    detailForm.id = detailIdOf(detail)
}

async function changeDetailStatus(id) {
    if (!confirm('Bạn có chắc muốn chuyển trạng thái biến thể này?')) return
    try {
        await API.put(`/productDetail/updateStatus/${id}`, { id })
        alert('Đã chuyển trạng thái biến thể')
        await loadProductDetails(currentProduct.value.id)
    } catch (error) {
        console.error('Lỗi chuyển trạng thái biến thể:', error.response ? error.response.data : error.message)
        alert('Không thể chuyển trạng thái biến thể')
    }
}

async function saveProductDetails() {
    if (loading.value) return
    loading.value = true
    try {
        if (!currentProduct.value?.id) return
        const isEdit = !!detailForm.id

        const sizesArr = asArray(selectedSizes.value)
        const colorsArr = asArray(selectedColors.value)
        const collarsArr = asArray(selectedCollars.value)

        const missingBase =
            !detailForm.price || !detailForm.description ||
            sizesArr.length === 0 || colorsArr.length === 0 || collarsArr.length === 0
        if (missingBase) { alert('Vui lòng điền đầy đủ thông tin.'); return }

        if (!isEdit && selectedImages.value.length === 0) {
            alert('Vui lòng chọn ít nhất 1 ảnh cho chi tiết mới.')
            return
        }

        if (isEdit) {
            const orig = productDetailList.value.find(d => detailIdOf(d) === detailForm.id) || {}
            const origTriple = tripleFromRow(orig)

            const sameSize = Number(selectedSizes.value[0]) === Number(origTriple.sizeId)
            const sameColor = Number(selectedColors.value[0]) === Number(origTriple.colorId)
            const sameCollar = Number(selectedCollars.value[0]) === Number(origTriple.collarId)
            const samePrice = Number(detailForm.price) === Number(orig.price)
            const sameDesc = String(detailForm.description ?? '').trim() === String(orig.description ?? '').trim()

            const needUpdateDetail = !(sameSize && sameColor && sameCollar && samePrice && sameDesc)
            const needUploadImages = selectedImages.value.length > 0

            if (!needUpdateDetail && !needUploadImages) { alert('Không có thay đổi để cập nhật.'); return }

            if (!(sameSize && sameColor && sameCollar)) {
                const targetKey = tripleKey(sizesArr[0], colorsArr[0], collarsArr[0])
                const duplicated = productDetailList.value.some(d => {
                    const id = detailIdOf(d); if (id === detailForm.id) return false
                    const { key } = tripleFromRow(d); return key === targetKey
                })
                if (duplicated) { alert('Chi tiết (Size/Màu/Cổ) này đã tồn tại ở chi tiết khác.'); return }
            }

            if (needUpdateDetail) {
                const updatedDetail = {
                    product: { id: currentProduct.value.id },
                    size: { id: sizesArr[0] },
                    color: { id: colorsArr[0] },
                    collar: { id: collarsArr[0] },
                    price: detailForm.price,
                    description: detailForm.description,
                    status: 1
                }
                await API.put(`/productDetail/update/${detailForm.id}`, updatedDetail)
            }

            if (needUploadImages) {
                const filesCopy = [...selectedImages.value]
                const mainIdxCopy = (mainImageIndex.value != null && mainImageIndex.value >= 0) ? mainImageIndex.value : -1
                await uploadImages(detailForm.id, filesCopy, mainIdxCopy)
            }

            alert('Cập nhật chi tiết thành công')
        } else {
            const wantKeys = new Set(), wantTriples = []
            for (const size of sizesArr) {
                for (const color of colorsArr) {
                    for (const collar of collarsArr) {
                        const k = tripleKey(size, color, collar)
                        if (!wantKeys.has(k)) { wantKeys.add(k); wantTriples.push({ size, color, collar, key: k }) }
                    }
                }
            }

            const duplicates = [], payloads = []
            for (const t of wantTriples) {
                if (existingDetailKeySet.value.has(t.key)) duplicates.push(t)
                else {
                    payloads.push({
                        product: { id: currentProduct.value.id },
                        size: { id: t.size },
                        color: { id: t.color },
                        collar: { id: t.collar },
                        price: detailForm.price,
                        description: detailForm.description,
                        active: true
                    })
                }
            }

            if (payloads.length === 0) {
                alert(duplicates.length ? `Tất cả ${duplicates.length} chi tiết đã tồn tại, không thể thêm trùng.` : 'Không có chi tiết hợp lệ để thêm.')
                return
            }

            const addResults = await Promise.allSettled(payloads.map(p => API.post('/productDetail/add', p)))
            const successIds = []; let failed = 0
            addResults.forEach(r => { if (r.status === 'fulfilled') successIds.push(r.value.data.id); else failed++ })

            if (selectedImages.value.length > 0 && successIds.length > 0) {
                const filesCopy = [...selectedImages.value]
                const mainIdxCopy = (mainImageIndex.value != null && mainImageIndex.value >= 0) ? mainImageIndex.value : -1
                await Promise.all(successIds.map(id => uploadImages(id, filesCopy, mainIdxCopy)))
            }
            alert(`Đã thêm ${successIds.length} chi tiết.${duplicates.length ? ` Bỏ qua ${duplicates.length} chi tiết trùng.` : ''}${failed ? ` ${failed} chi tiết thêm thất bại.` : ''}`)
        }

        resetDetailForm()
        await loadProductDetails(currentProduct.value.id)
    } catch (err) {
        console.error('Lỗi khi lưu chi tiết:', err)
        alert('Không thể lưu chi tiết!')
    } finally {
        loading.value = false
    }
}

async function hydrateMainImages(list) {
    const pairs = list.map(item => ({ item, id: detailIdOf(item) })).filter(p => !!p.id)
    if (!pairs.length) return
    const calls = pairs.map(p => API.get(`/image/show/${p.id}`).catch(() => ({ data: [] })))
    const results = await Promise.allSettled(calls)
    results.forEach((r, i) => {
        const item = pairs[i].item
        const imgs = r.status === 'fulfilled' ? (r.value.data || []) : []
        item.images = imgs
        const main = imgs.find(it => isMainFlag(it.main)) || imgs[0]
        item.mainImageUrl = main ? (typeof main === 'string' ? main : (main.url || '')) : ''
    })
    productDetailList.value = [...productDetailList.value]
}

function currentPageItems() {
    const start = (detailPage.value - 1) * detailsPerPage
    return detailsFiltered.value.slice(start, start + detailsPerPage)
}

async function hydrateCurrentPage(force = false) {
    const pageItems = currentPageItems()
    const need = force ? pageItems : pageItems.filter(i => !i.mainImageUrl)
    if (need.length) await hydrateMainImages(need)
}

async function refreshRowMainImage(productDetailId) {
    if (!productDetailId) return
    try {
        const { data } = await API.get(`/image/show/${productDetailId}`)
        const row = productDetailList.value.find(d => detailIdOf(d) === productDetailId)
        if (!row) return
        row.images = data || []
        const main = row.images.find(i => isMainFlag(i.main)) || row.images[0]
        row.mainImageUrl = main ? (typeof main === 'string' ? main : (main.url || '')) : ''
        productDetailList.value = productDetailList.value.map(d => detailIdOf(d) === productDetailId ? { ...row } : d)
    } catch (e) { console.error('refreshRowMainImage error:', e) }
}

function handleMultipleImageChange(e) {
    const files = Array.from(e.target.files || [])
    if (!files.length) return
    const MAX_MB = 5, valid = []
    for (const f of files) {
        const okType = /^image\//.test(f.type), okSize = f.size <= MAX_MB * 1024 * 1024
        if (!okType) { alert(`File ${f.name} không phải ảnh hợp lệ.`); continue }
        if (!okSize) { alert(`File ${f.name} vượt ${MAX_MB}MB.`); continue }
        valid.push(f)
    }
    if (!valid.length) { e.target.value = ''; return }
    previewUrls.value.forEach(u => URL.revokeObjectURL(u))
    selectedImages.value = valid
    previewUrls.value = valid.map(f => URL.createObjectURL(f))
    mainImageIndex.value = null
    e.target.value = ''
}

onBeforeUnmount(() => previewUrls.value.forEach(u => URL.revokeObjectURL(u)))
const selectMainImage = (i) => mainImageIndex.value = i

async function uploadImages(detailId, files, mainIndex) {
    const form = new FormData()
        ; (files || []).forEach(f => form.append('files', f))
    form.append('productDetailId', String(detailId))
    form.append('mainImageIndex', (Number.isInteger(mainIndex) && mainIndex >= 0) ? String(mainIndex) : '-1')
    try {
        return await API.post('/image/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    } catch (err) { console.error('uploadImages error:', err); throw err }
}

async function openImageViewer(detail) {
    try {
        const id = detailIdOf(detail)
        if (!id) return
        currentDetailId.value = id
        const { data } = await API.get(`/image/show/${id}`)
        selectedDetailImages.value = data || []
        const mainIndex = selectedDetailImages.value.findIndex(img => isMainFlag(img.main))
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0
        await nextTick()
        bootstrap.Modal.getOrCreateInstance(imageViewerModalRef.value).show()
    } catch (error) {
        console.error("Lỗi khi tải ảnh chi tiết:", error)
        selectedDetailImages.value = []
    }
}

const editImage = async (detailId, imageIndex) => {
    const input = document.createElement("input")
    input.type = "file"; input.accept = "image/*"
    input.onchange = async () => {
        const file = input.files?.[0]; if (!file) return
        const form = new FormData()
        form.append("file", file)
        form.append("productDetailId", detailId)
        form.append("imageIndex", imageIndex)
        try {
            await API.post("/image/update-file", form, { headers: { "Content-Type": "multipart/form-data" } })
            const { data } = await API.get(`/image/show/${detailId}`)
            selectedDetailImages.value = data || []
            const mainIdx = selectedDetailImages.value.findIndex(i => isMainFlag(i.main))
            mainImageIndexViewer.value = mainIdx !== -1 ? mainIdx : 0
            await refreshRowMainImage(detailId)
        } catch (e) { console.error("Lỗi khi cập nhật ảnh:", e) }
    }
    input.click()
}

async function setMainImage(img) {
    try {
        const detailId = img.productDetailId || currentDetailId.value
        await API.put(`/image/set-main/${img.id}`, null, { params: { productDetailId: detailId } })
        const { data } = await API.get(`/image/show/${detailId}`)
        selectedDetailImages.value = data || []
        const mainIndex = selectedDetailImages.value.findIndex(i => isMainFlag(i.main))
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0
        await refreshRowMainImage(detailId)
        alert("Đã đặt ảnh chính thành công.")
    } catch (err) { console.error("Lỗi khi đặt ảnh chính:", err); alert("Không thể đặt ảnh chính.") }
}

async function deleteImage(imageId, productDetailId) {
    if (!confirm("Bạn có chắc muốn xoá ảnh này?")) return
    try {
        await API.delete(`/image/delete/${imageId}`)
        const { data } = await API.get(`/image/show/${productDetailId}`)
        selectedDetailImages.value = data || []
        const mainIndex = selectedDetailImages.value.findIndex(i => isMainFlag(i.main))
        mainImageIndexViewer.value = mainIndex !== -1 ? mainIndex : 0
        await refreshRowMainImage(productDetailId)
        alert("Đã xoá ảnh thành công.")
    } catch (err) { console.error("Lỗi khi xoá ảnh:", err); alert("Không thể xoá ảnh.") }
}

async function addImagesForDetail(detailId) {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.multiple = true

    input.onchange = async () => {
        const files = Array.from(input.files || [])
        if (!files.length) return

        try {
            const { data: existing = [] } = await API.get(`/image/show/${detailId}`)
            const nameToIndex = new Map()
            existing.forEach((img, idx) => nameToIndex.set(imgBaseName(img), idx))

            const toUpdate = []
            const toUpload = []
            for (const f of files) {
                const base = fileBaseName(f.name)
                if (nameToIndex.has(base)) toUpdate.push({ file: f, index: nameToIndex.get(base) })
                else toUpload.push(f)
            }
            for (const u of toUpdate) {
                const form = new FormData()
                form.append('file', u.file)
                form.append('productDetailId', detailId)
                form.append('imageIndex', u.index)
                await API.post('/image/update-file', form, { headers: { 'Content-Type': 'multipart/form-data' } })
            }
            if (toUpload.length) await uploadImages(detailId, toUpload, -1)

            const { data } = await API.get(`/image/show/${detailId}`)
            selectedDetailImages.value = data || []
            const mainIdx = selectedDetailImages.value.findIndex(i => isMainFlag(i.main))
            mainImageIndexViewer.value = mainIdx !== -1 ? mainIdx : 0
            await refreshRowMainImage(detailId)
        } catch (e) {
            console.error(e)
            alert('Không thể thêm/cập nhật ảnh.')
        }
    }
    input.click()
}

const detailsFiltered = computed(() => {
    const sz = detailFilters.value.sizeId
    const co = detailFilters.value.colorId
    const cl = detailFilters.value.collarId
    const st = detailFilters.value.status

    return productDetailList.value.filter(d => {
        const sizeId = toIdSafe(d.size, sizes)
        const colorId = toIdSafe(d.color, colors)
        const collarId = toIdSafe(d.collar, collars)

        const okSize = sz === 'all' || Number(sizeId) === Number(sz)
        const okColor = co === 'all' || Number(colorId) === Number(co)
        const okCollar = cl === 'all' || Number(collarId) === Number(cl)

        const active = rowActive(d, true)
        const okStatus =
            st === 'all' ||
            (st === 'active' && active) ||
            (st === 'inactive' && !active)

        return okSize && okColor && okCollar && okStatus
    })
})

// Pagination chi tiết
const detailPage = ref(1)
const detailsPerPage = 4
const totalDetailPages = computed(() => Math.max(1, Math.ceil(detailsFiltered.value.length / detailsPerPage)))
const paginatedDetails = computed(() => {
    const start = (detailPage.value - 1) * detailsPerPage
    return detailsFiltered.value.slice(start, start + detailsPerPage)
})
const changeDetailPage = (p) => { if (p >= 1 && p <= totalDetailPages.value) detailPage.value = p }

// Reset trang khi đổi filter chi tiết
watch(detailFilters, () => { detailPage.value = 1; hydrateCurrentPage(false) }, { deep: true })

// Lọc danh sách biến thể theo từ khoá (giữ nguyên cho UI chọn)
const filteredColors = computed(() =>
    colors.value.filter(c => norm(c.name).includes(norm(colorSearch.value)))
)
const filteredSizes = computed(() =>
    sizes.value.filter(s => norm(s.eu ?? s.name ?? '').includes(norm(sizeSearch.value)))
)
const filteredCollars = computed(() =>
    collars.value.filter(c => norm(c.name).includes(norm(collarSearch.value)))
)

// Lifecycle
onMounted(async () => {
    try {
        await Promise.all([
            fetchProducts(),
            fetchBrands(),
            fetchCategories(),
            fetchSoles(),
            fetchSizes(),
            fetchColors(),
            fetchCollars(),
        ])
    } catch (e) {
        console.error('Lỗi khởi tạo:', e)
    }
})
watch(detailPage, () => { hydrateCurrentPage(false) })
</script>

<template>
    <div class="container mt-4">
        <h3>Quản lý Sản phẩm</h3>

        <!-- Form sản phẩm -->
        <div class="card mb-4">
            <div class="card-body row g-3">
                <div class="col-md-4">
                    <label>Tên sản phẩm</label>
                    <input v-model="form.productName" class="form-control" required />
                </div>
                <div class="col-md-2">
                    <label>Thương hiệu</label>
                    <select v-model.number="form.brandId" class="form-select" required>
                        <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Danh mục</label>
                    <select v-model.number="form.categoryId" class="form-select" required>
                        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label>Đế giày</label>
                    <select v-model.number="form.soleId" class="form-select" required>
                        <option v-for="s in soles" :key="s.id" :value="s.id">{{ s.name }}</option>
                    </select>
                </div>
                <div class="col-md-12">
                    <label>Mô tả</label>
                    <textarea v-model="form.description" class="form-control" required></textarea>
                </div>
                <div class="col-md-12">
                    <button class="btn btn-success me-2" @click="saveProduct">{{ form.id ? 'Cập nhật' : 'Thêm'
                    }}</button>
                    <button class="btn btn-secondary" @click="resetForm">Làm mới</button>
                </div>
            </div>
        </div>

        <!-- Bộ lọc sản phẩm -->
        <div class="card mb-3">
            <div class="card-body row g-2 align-items-end">
                <div class="col-md-4">
                    <label class="form-label small">Tìm theo tên</label>
                    <input class="form-control" v-model="productFilters.q" placeholder="Nhập tên sản phẩm..." />
                </div>
                <div class="col-md-2">
                    <label class="form-label small">Thương hiệu</label>
                    <select v-model="productFilters.brandId" class="form-select">
                        <option value="all">Tất cả</option>
                        <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label small">Danh mục</label>
                    <select v-model="productFilters.categoryId" class="form-select">
                        <option value="all">Tất cả</option>
                        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label small">Đế giày</label>
                    <select v-model="productFilters.soleId" class="form-select">
                        <option value="all">Tất cả</option>
                        <option v-for="s in soles" :key="s.id" :value="s.id">{{ s.name }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label small">Trạng thái</label>
                    <select v-model="productFilters.status" class="form-select">
                        <option value="all">Tất cả</option>
                        <option value="active">Hoạt động</option>
                        <option value="inactive">Không hoạt động</option>
                    </select>
                </div>
            </div>
        </div>

        <!-- Header danh sách -->
        <div class="d-flex align-items-center justify-content-between mb-2">
            <h5 class="mb-0">Danh sách sản phẩm</h5>
            <small class="text-muted">Hiển thị {{ paginatedProducts.length }} / {{ productsFiltered.length }} kết
                quả</small>
        </div>

        <!-- Bảng sản phẩm -->
        <table class="table table-bordered">
            <thead>
                <tr class="text-center">
                    <th style="width: 60px">ID</th>
                    <th style="width: 180px">Tên</th>
                    <th style="width: 100px">Thương hiệu</th>
                    <th style="width: 80px">Danh mục</th>
                    <th style="width: 60px">Đế</th>
                    <th style="width: 120px">Trạng thái</th>
                    <th style="width: 180px">Mô Tả</th>
                    <th style="width: 180px">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="p in paginatedProducts" :key="p.id">
                    <td class="text-center">{{ p.id }}</td>
                    <td class="text-center">{{ p.productName }}</td>
                    <td class="text-center">{{ getNameById(brands, p.brandId) }}</td>
                    <td class="text-center">{{ getNameById(categories, p.categoryId) }}</td>
                    <td class="text-center">{{ getNameById(soles, p.soleId) }}</td>
                    <td class="text-center">
                        <span v-if="rowActive(p)" class="badge bg-success">Hoạt động</span>
                        <span v-else class="badge bg-danger">Không hoạt động</span>
                    </td>
                    <td>{{ p.description }}</td>
                    <td>
                        <button class="btn btn-warning btn-sm me-2" @click="editProduct(p)">Sửa</button>
                        <button class="btn btn-danger btn-sm me-2" @click="changeStatus(p.id)">Chuyển trạng
                            thái</button>
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
        <div class="modal fade" id="productDetailModal" tabindex="-1" ref="productDetailModalRef">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Chi tiết sản phẩm: {{ currentProduct?.productName }}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>

                    <div class="modal-body">
                        <!-- Nhóm biến thể để TẠO/SỬA -->
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label fw-semibold mb-1">Màu sắc</label>
                                <input class="form-control form-control-sm mb-2" v-model="colorSearch"
                                    placeholder="Tìm màu..." />
                                <div class="option-box border rounded p-2">
                                    <div v-if="!filteredColors.length" class="text-muted small">Không có màu phù hợp
                                    </div>
                                    <div class="form-check" v-for="c in filteredColors" :key="c.id">
                                        <input class="form-check-input" type="radio" :id="`color_${c.id}`" :value="c.id"
                                            v-model="selectedColors" required />
                                        <label class="form-check-label" :for="`color_${c.id}`">{{ c.name }}</label>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label fw-semibold mb-1">Kích cỡ</label>
                                <input class="form-control form-control-sm mb-2" v-model="sizeSearch"
                                    placeholder="Tìm size..." />
                                <div class="option-box border rounded p-2">
                                    <div v-if="!filteredSizes.length" class="text-muted small">Không có size phù hợp
                                    </div>
                                    <div class="form-check" v-for="s in filteredSizes" :key="s.id">
                                        <input class="form-check-input" type="checkbox" :id="`size_${s.id}`"
                                            :value="s.id" v-model="selectedSizes" required />
                                        <label class="form-check-label" :for="`size_${s.id}`">{{ s.eu }}</label>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label fw-semibold mb-1">Kiểu cổ</label>
                                <input class="form-control form-control-sm mb-2" v-model="collarSearch"
                                    placeholder="Tìm kiểu cổ..." />
                                <div class="option-box border rounded p-2">
                                    <div v-if="!filteredCollars.length" class="text-muted small">Không có kiểu cổ phù
                                        hợp</div>
                                    <div class="form-check" v-for="c in filteredCollars" :key="c.id">
                                        <input class="form-check-input" type="radio" :id="`collar_${c.id}`"
                                            :value="c.id" v-model="selectedCollars" required />
                                        <label class="form-check-label" :for="`collar_${c.id}`">{{ c.name }}</label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row g-3 gx-4 mt-3">
                            <!-- Cột trái -->
                            <div class="col-md-6">
                                <label>Giá</label>
                                <input type="number" class="form-control" v-model="detailForm.price" required />
                                <label class="mt-2">Mô tả</label>
                                <input type="text" class="form-control" v-model="detailForm.description" />
                                <button class="btn btn-primary mt-3 w-100" @click="saveProductDetails">
                                    {{ detailForm.id ? '✔ Cập nhật' : 'Thêm' }}
                                </button>
                                <button class="btn btn-secondary mt-2 w-100" @click="resetDetailForm">Làm mới</button>
                            </div>

                            <!-- Cột phải: Ảnh -->
                            <div class="col-md-6">
                                <label>Ảnh sản phẩm</label>
                                <template v-if="!detailForm.id">
                                    <input type="file" multiple accept="image/*" class="form-control"
                                        @change="handleMultipleImageChange" />
                                    <div class="mt-3 d-flex flex-wrap gap-2 justify-content-start"
                                        v-if="previewUrls.length">
                                        <div v-for="(url, index) in previewUrls" :key="index" class="position-relative"
                                            style="cursor: pointer;" @click="selectMainImage(index)">
                                            <img :src="url" alt="Preview" class="img-thumbnail"
                                                :style="{ width: '120px', height: '120px', objectFit: 'cover', border: mainImageIndex === index ? '3px solid red' : '1px solid #ccc', boxShadow: mainImageIndex === index ? '0 0 6px red' : 'none' }" />
                                            <span v-if="mainImageIndex === index"
                                                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">Chính</span>
                                        </div>
                                    </div>
                                </template>
                                <template v-else>
                                    <div class="d-flex gap-2">
                                        <button class="btn btn-outline-secondary w-50"
                                            @click="addImagesForDetail(detailForm.id)">Thêm ảnh</button>
                                        <button class="btn btn-outline-primary w-50"
                                            @click="openImageViewer({ productDetailId: detailForm.id })">Quản lý
                                            ảnh</button>
                                    </div>
                                    <small class="text-muted d-block mt-2">
                                        “Thêm ảnh” sẽ tải và lưu ngay; “Quản lý ảnh” để đặt ảnh chính / thay / xoá.
                                    </small>
                                </template>
                            </div>
                        </div>

                        <div class="card mt-4 mb-2">
                            <div class="card-body row g-2 align-items-end">
                                <div class="col-md-3">
                                    <label class="form-label small">Size</label>
                                    <select class="form-select" v-model="detailFilters.sizeId">
                                        <option value="all">Tất cả</option>
                                        <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.eu ?? s.name }}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small">Màu</label>
                                    <select class="form-select" v-model="detailFilters.colorId">
                                        <option value="all">Tất cả</option>
                                        <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.name }}</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small">Kiểu cổ</label>
                                    <select class="form-select" v-model="detailFilters.collarId">
                                        <option value="all">Tất cả</option>
                                        <option v-for="c in collars" :key="c.id" :value="c.id">{{ c.name }}</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small">Trạng thái</label>
                                    <select class="form-select" v-model="detailFilters.status">
                                        <option value="all">Tất cả</option>
                                        <option value="active">Hoạt động</option>
                                        <option value="inactive">Không hoạt động</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <!-- Header bảng chi tiết -->
                        <div class="d-flex align-items-center justify-content-between mt-2 mb-2">
                            <h6 class="mb-0">Danh sách chi tiết</h6>
                            <small class="text-muted">Hiển thị {{ paginatedDetails.length }} / {{ detailsFiltered.length
                            }} dòng</small>
                        </div>

                        <!-- Bảng chi tiết -->
                        <div class="table-responsive">
                            <table class="table table-bordered table-sm">
                                <thead class="table-light text-center">
                                    <tr>
                                        <th>Size</th>
                                        <th>Màu</th>
                                        <th>Cổ</th>
                                        <th>Giá</th>
                                        <th>Mô tả</th>
                                        <th>Trạng thái</th>
                                        <th>Ảnh</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody class="align-middle">
                                    <tr v-for="productDetail in paginatedDetails"
                                        :key="productDetail.productDetailId ?? productDetail.id">
                                        <td class="text-center">{{ productDetail.size }}</td>
                                        <td>{{ productDetail.color }}</td>
                                        <td>{{ productDetail.collar }}</td>
                                        <td class="text-center">{{ productDetail.price }}</td>
                                        <td>{{ productDetail.description }}</td>
                                        <td class="text-center">
                                            <span v-if="rowActive(productDetail)" class="badge bg-success">Hoạt
                                                động</span>
                                            <span v-else class="badge bg-danger">Không hoạt động</span>
                                        </td>
                                        <td class="text-center">
                                            <div v-if="productDetail.mainImageUrl">
                                                <img :src="productDetail.mainImageUrl" alt="Ảnh chính"
                                                    class="img-thumbnail"
                                                    style="width: 60px; height: 60px; object-fit: cover; cursor: pointer;"
                                                    @click="openImageViewer(productDetail)" />
                                            </div>
                                            <div v-else>
                                                <button class="btn btn-sm btn-outline-secondary"
                                                    @click="addImagesForDetail(productDetail.productDetailId ?? productDetail.id)">
                                                    Thêm ảnh
                                                </button>
                                            </div>
                                        </td>
                                        <td class="text-center">
                                            <button class="btn btn-sm btn-warning me-1"
                                                @click="editDetail(productDetail)">Sửa</button>
                                            <button class="btn btn-sm btn-danger"
                                                @click="changeDetailStatus(productDetail.productDetailId ?? productDetail.id)">
                                                Chuyển trạng thái
                                            </button>
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

        <!-- Modal Xem/Sửa ảnh -->
        <div class="modal fade" id="imageViewerModal" tabindex="-1" aria-hidden="true" ref="imageViewerModalRef">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content shadow rounded-4">
                    <div class="modal-header bg-primary text-white">
                        <h5 class="modal-title">Ảnh chi tiết sản phẩm</h5>
                        <div class="ms-auto d-flex gap-2">
                            <button class="btn btn-sm btn-light" @click="addImagesForDetail(currentDetailId)">Thêm
                                ảnh</button>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                aria-label="Close"></button>
                        </div>
                    </div>
                    <div class="modal-body">
                        <div class="d-flex flex-wrap gap-3 justify-content-center">
                            <div v-for="(img, index) in selectedDetailImages" :key="img.id || index"
                                class="d-flex flex-column align-items-center" style="width: 140px;">
                                <img :src="img.url" alt="Ảnh" class="img-thumbnail shadow"
                                    style="width: 120px; height: 120px; object-fit: cover; border-radius: 10px;"
                                    :style="{ border: mainImageIndexViewer === index ? '3px solid red' : '1px solid #ccc' }"
                                    @click="setMainImage(img)" />
                                <span v-if="mainImageIndexViewer === index" class="badge bg-danger mt-1"
                                    style="font-size: 12px;">Chính</span>
                                <div class="btn-group mt-2" role="group">
                                    <button class="btn btn-sm btn-outline-primary"
                                        @click="editImage(img.productDetailId || currentDetailId, index)"><i
                                            class="bi bi-pencil"></i></button>
                                    <button class="btn btn-sm btn-outline-danger"
                                        @click="deleteImage(img.id, img.productDetailId || currentDetailId)"><i
                                            class="bi bi-trash"></i></button>
                                </div>
                            </div>
                            <div v-if="!selectedDetailImages.length" class="text-muted">
                                Chưa có ảnh — bấm “Thêm ảnh” để tải ảnh lên.
                            </div>
                        </div>
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

.option-box {
    max-height: 260px;
    overflow: auto;
}
</style>
