<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";
import { printReceipt } from "../pos/printReceipt";

import ProductList from "../pos/ProductList.vue";
import ProductVariantModal from "../pos/ProductVariantModal.vue";
import InvoiceTabs from "../pos/InvoiceTabs.vue";
import CartTable from "../pos/CartTable.vue";
import CustomerSelect from "../pos/CustomerSelect.vue";
import PaymentInfo from "../pos/PaymentInfo.vue";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

const uid = () =>
    `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 8)}`;
const normalizeId = (x) => (x === null || x === undefined ? null : String(x));
const firstNonEmpty = (...vals) =>
    vals.find(v => v !== null && v !== undefined && String(v).trim() !== "") || "";

const WALK_IN = { fullName: "Khách lẻ", phone: "" };

const products = ref([]);
const customers = ref([]);
const productVariants = ref([]);
const selectedProduct = ref(null);

const employee = ref(null);
const employeeName = ref('');

async function fetchUser() {
    let u = null;
    try { u = JSON.parse(localStorage.getItem("user") || "null"); } catch { }
    const id = u?.employeeId ?? u?.employee?.id ?? u?.EMPLOYEE_ID ?? u?.id;
    if (!id) return;

    try {
        const { data } = await api.get(`/employee/showById/${id}`);
        const emp = Array.isArray(data) ? data[0] : data;
        employee.value = emp;
        employeeName.value = emp?.fullName ?? emp?.FULL_NAME ?? emp?.name ?? '';
    } catch (e) {
        console.error("Fetch employee failed:", e);
    }
}


const newInvoice = () => ({
    id: uid(),
    customerId: null,
    items: [],
    paid: 0,
    promotionCode: "",
    selectedPromotion: null,
    promotionId: null,
    promoError: "",
    recipientPhoneNumber: "",
});

const invoices = ref([newInvoice()]);
const activeInvoiceIndex = ref(0);
const currentInvoice = computed(() => invoices.value[activeInvoiceIndex.value] || newInvoice());

const currentCustomer = computed(() => {
    const id = normalizeId(currentInvoice.value.customerId);
    if (id == null) return null;
    return customers.value.find((c) => normalizeId(c.id) === id) || null;
});

const selectedPhone = computed(() =>
    firstNonEmpty(currentInvoice.value.recipientPhoneNumber, currentCustomer.value?.phone)
);

const customerSelectModel = ref(null);

const subTotal = computed(() =>
    currentInvoice.value.items.reduce(
        (sum, i) => sum + (Number(i.price) || 0) * (Number(i.quantity) || 0),
        0
    )
);

const discountAmountList = ref([]);

const discountAmount = computed(() => {
    const inv = currentInvoice.value;
    if (!inv?.selectedPromotion) return 0;
    const total = subTotal.value;
    const promo = inv.selectedPromotion;

    if (Number(promo.type) === 1) {
        const percent = Math.max(0, Math.min(100, Number(promo.value || 0)));
        return Math.min(Math.floor((total * percent) / 100), total);
    }
    if (Number(promo.type) === 2) {
        return Math.min(Math.floor(Number(promo.value || 0)), total);
    }
    return 0;
});

const grandTotal = computed(() => Math.max(0, subTotal.value - discountAmount.value));
const change = computed(() => (Number(currentInvoice.value.paid) || 0) - grandTotal.value);

watch(invoices, (val) => {
    localStorage.setItem("invoices", JSON.stringify(val));
}, { deep: true });

const normalizeCustomer = (c) => ({
    ...c,
    fullName: firstNonEmpty(c.fullName, c.name, c.customerName),
    phone: firstNonEmpty(c.phone, c.numberPhone, c.phoneNumber, c.sdt, c.mobile),
});

const toBoolish = (v) => {
    if (v === true || v === 1) return true;
    if (v === false || v === 0 || v == null) return false;
    const s = String(v).trim().toLowerCase();
    return s === 'true' || s === '1' || s === 'active' ||
        s === 'đang hoạt động' || s === 'dang hoat dong' ||
        s === 'hoạt động' || s === 'hoat dong';
};

const isActiveRow = (row) => {
    if (!row || typeof row !== 'object') return false;
    const keys = ['active', 'isActive', 'status', 'trangThai', 'statusText'];
    for (const k of keys) {
        if (k in row) return toBoolish(row[k]);
    }
    return false;
};

const fetchData = async () => {
    try {
        const [resProduct, resCustomer] = await Promise.all([
            api.get("/product/show"),
            api.get("/customer/show"),
        ]);
        products.value = (resProduct.data || []).filter(isActiveRow);
        customers.value = (resCustomer.data || []).map(normalizeCustomer);
    } catch (err) {
        console.error("Lỗi tải sản phẩm/khách hàng:", err);
    }
};


const fetchPromotion = async () => {
    try {
        const { data } = await api.get("/promotion/show");
        discountAmountList.value = (data || []).filter(isActiveRow);
    } catch (err) {
        console.error("Lỗi khuyến mãi:", err);
    }
};

const selectProduct = async (product) => {
    try {
        const { data } = await api.get(`/productDetail/show/${product.id}`);
        productVariants.value = (data || []).filter(isActiveRow);
        selectedProduct.value = product;
    } catch (err) {
        console.error("Lỗi lấy chi tiết sản phẩm:", err);
    }
};


const closeVariantModal = () => {
    selectedProduct.value = null;
    productVariants.value = [];
};

const addToInvoice = (variant) => {
    if (!isActiveRow(variant)) { alert('Biến thể không hoạt động.'); return; }
    const qty = Number(variant.quantity) > 0 ? Number(variant.quantity) : 1;
    const existing = currentInvoice.value.items.find(
        (i) => i.productDetailId === variant.productDetailId
    );
    if (existing) existing.quantity += qty;
    else currentInvoice.value.items.push({ ...variant, quantity: qty });
    closeVariantModal();
};

const removeItem = (index) => {
    currentInvoice.value.items.splice(index, 1);
};

const changeQuantity = (index, quantity) => {
    const item = currentInvoice.value.items[index];
    const q = parseInt(quantity) || 1;
    api.get(`/inventory/getQuantity/${item.productDetailId}`)
        .then((invRes) => {
            const stock = invRes.data?.quantityInventory ?? 0;
            currentInvoice.value.items[index].quantity = Math.min(Math.max(1, q), stock);
            if (q > stock) alert(`Tồn kho chỉ còn ${stock}. Đã điều chỉnh!`);
        })
        .catch(() => {
            currentInvoice.value.items[index].quantity = Math.max(1, q);
        });
};

const switchInvoice = (index) => {
    activeInvoiceIndex.value = index;
    customerSelectModel.value = null;
};

const addInvoice = () => invoices.value.push(newInvoice());

const removeInvoice = (index) => {
    invoices.value.splice(index, 1);
    if (invoices.value.length === 0) {
        invoices.value.push(newInvoice());
        activeInvoiceIndex.value = 0;
    } else if (activeInvoiceIndex.value >= invoices.value.length) {
        activeInvoiceIndex.value = invoices.value.length - 1;
    }
    customerSelectModel.value = null;
};

const applyPromotionCode = () => {
    const inv = currentInvoice.value;
    const code = (inv.promotionCode || "").trim().toLowerCase();

    const now = new Date();
    now.setHours(0, 0, 0, 0);

    const promo = (discountAmountList.value || []).find((p) => {
        const codeMatch = ((p.promotionCode || "").trim().toLowerCase() === code);
        const start = p.startDate ? new Date(p.startDate) : null;
        const end = p.endDate ? new Date(p.endDate) : null;
        if (end) end.setHours(23, 59, 59, 999);
        return codeMatch && (!start || start <= now) && (!end || now <= end);
    });

    if (promo) {
        inv.selectedPromotion = promo;
        inv.promotionId = promo.id;
        inv.promoError = "";
    } else {
        inv.selectedPromotion = null;
        inv.promotionId = null;
        inv.promoError = "Mã không hợp lệ hoặc đã hết hạn";
    }
};

const clearVoucher = () => {
    const inv = currentInvoice.value;
    inv.promotionCode = "";
    inv.selectedPromotion = null;
    inv.promotionId = null;
    inv.promoError = "";
};

// thêm vào script
const customerSelectResetKey = ref(0)
const resetCustomerInput = () => {
    customerSelectModel.value = null
    customerSelectResetKey.value++
}

const onCustomerPicked = (val) => {
    const pickedIdRaw = val && typeof val === "object" ? (val.id ?? val.value ?? val.customerId) : val
    const pickedId = pickedIdRaw == null ? null : String(pickedIdRaw)

    const row = (val && typeof val === "object")
        ? val
        : customers.value.find(c => normalizeId(c.id) === pickedId) || null

    // KH không hoạt động -> báo + xoá trắng ô nhập
    if (row && !isActiveRow(row)) {
        alert("Khách hàng này đang Không hoạt động. Không thể chọn.")
        resetCustomerInput()
        return
    }
    // Hợp lệ -> set như cũ
    currentInvoice.value.customerId = pickedId

    let phone = ""
    try {
        const deep = JSON.parse(JSON.stringify(row ?? val))
        phone = firstNonEmpty(deep?.numberPhone, deep?.phone, deep?.phoneNumber, deep?.sdt, deep?.mobile)
    } catch { }
    currentInvoice.value.recipientPhoneNumber = (phone || "").trim()

    customerSelectModel.value = null // giữ ô tìm trắng sau khi đã set customerId
}

const clearSelectedCustomer = () => {
    currentInvoice.value.customerId = null;
    currentInvoice.value.recipientPhoneNumber = "";
    customerSelectModel.value = null;
};

const handleCheckout = async () => {
    const inv = currentInvoice.value;

    let user = null;
    try { user = JSON.parse(localStorage.getItem("user") || "null"); } catch { }

    if (!inv || inv.items.length === 0) {
        alert("Giỏ hàng đang trống!");
        return;
    }

    const name = (currentCustomer.value?.fullName || WALK_IN.fullName).trim();
    const phone = firstNonEmpty(
        selectedPhone?.value,
        currentCustomer.value?.phone,
        WALK_IN.phone,
        ""
    ).trim();

    const dto = {
        customerId: inv.customerId || null,
        employeeId: user?.employeeId,
        ptttId: 1,
        promotionId: inv.promotionId || null,
        billCode: `HD${Date.now()}`,
        billType: "OFFLINE",
        status: 4,
        recipientName: name,
        recipientPhoneNumber: phone,
        addressMethod: "TẠI_CỬA_HÀNG",
        statusPayment: "ĐÃ_THANH_TOÁN",
        subTotal: subTotal.value,
        discountAmount: discountAmount.value,
        grandTotal: grandTotal.value,
        billDetails: inv.items.map((i) => ({
            productDetailId: i.productDetailId,
            quantity: i.quantity,
            price: i.price,
            productName: i.productName,
            color: i.color,
            size: i.size,
        })),
    };

    try {
        const res = await api.post("/bill/create", dto);

        const bfs = res?.data || {};
        // Ưu tiên dữ liệu server nếu có
        const employeeFromServer = bfs.employeeName ?? bfs.employee?.fullName ?? "";
        const customerFromServer = bfs.customerName ?? bfs.customer?.fullName ?? "";
        const customerPhone = firstNonEmpty(
            bfs.recipientPhoneNumber,
            bfs.phoneNumber,
            bfs.customerPhone,
            bfs.customer?.phone,
            bfs.customer?.phoneNumber,
            dto.recipientPhoneNumber,   
            phone                      
        );
        const itemsSrc =
            Array.isArray(bfs.billDetails) && bfs.billDetails.length
                ? bfs.billDetails
                : dto.billDetails;

        const bill = {
            billCode: bfs.billCode ?? dto.billCode,
            createdAt: bfs.createdDate ?? new Date().toISOString(),

            employeeName: firstNonEmpty(
                employeeFromServer,
                (typeof employeeName !== "undefined" && employeeName?.value) ? employeeName.value : "",
                user?.fullName
            ),

            customerName: firstNonEmpty(
                customerFromServer,
                currentCustomer.value?.fullName,
                WALK_IN.fullName
            ),

            customerPhone: customerPhone,
            items: itemsSrc.map((d) => ({
                productName: d.productName,
                color: d.color,
                size: d.size,
                quantity: d.quantity,
                price: d.price,
                lineTotal: (Number(d.price) || 0) * (Number(d.quantity) || 0),
            })),
            subTotal: bfs.subTotal ?? dto.subTotal,
            discountAmount: bfs.discountAmount ?? dto.discountAmount,
            grandTotal: bfs.grandTotal ?? dto.grandTotal,
            paid: inv.paid,
            change: (Number(inv.paid) || 0) - (bfs.grandTotal ?? dto.grandTotal),
        };

        alert("Thanh toán thành công!");
        await printReceipt(bill); // mở PDF ở tab mới

        // Reset
        invoices.value[activeInvoiceIndex.value] = newInvoice();
        customerSelectModel.value = null;
        localStorage.setItem("invoices", JSON.stringify(invoices.value));
    } catch (error) {
        console.error("Lỗi khi tạo hóa đơn:", error);
        alert(error?.response?.data?.message || "Lỗi khi thanh toán. Vui lòng thử lại.");
    }
};



onMounted(() => {
    const saved = localStorage.getItem("invoices");
    if (saved) {
        try {
            const parsed = JSON.parse(saved);
            if (Array.isArray(parsed) && parsed.length > 0) {
                invoices.value = parsed.map((inv) => {
                    const copy = { ...inv };
                    if (!copy.id) copy.id = uid();
                    if (copy.customer && !copy.customerId) copy.customerId = copy.customer.id ?? null;
                    delete copy.customer;
                    copy.customerId = copy.customerId == null ? null : String(copy.customerId);

                    if (copy.promotionCode === undefined) copy.promotionCode = "";
                    if (copy.selectedPromotion === undefined) copy.selectedPromotion = null;
                    if (copy.promotionId === undefined) copy.promotionId = null;
                    if (copy.promoError === undefined) copy.promoError = "";
                    if (copy.paid === undefined) copy.paid = 0;
                    if (copy.recipientPhoneNumber === undefined) copy.recipientPhoneNumber = "";
                    if (!Array.isArray(copy.items)) copy.items = [];
                    return copy;
                });
            }
        } catch { }
    }
    if (invoices.value.length === 0) invoices.value = [newInvoice()];
    if (activeInvoiceIndex.value >= invoices.value.length) activeInvoiceIndex.value = 0;

    customerSelectModel.value = null;

    fetchData();
    fetchUser();

    fetchPromotion();
});
</script>

<template>
    <div class="container py-4">
        <!-- Tabs hóa đơn -->
        <InvoiceTabs :invoices="invoices" :activeIndex="activeInvoiceIndex" @switch="switchInvoice" @add="addInvoice"
            @remove="removeInvoice" class="mb-4" />

        <div class="row g-4">
            <!-- Tìm kiếm + Thêm giỏ hàng -->
            <div class="col-md-8 pe-2">
                <div class="card h-150 shadow-sm mb-4">
                    <div class="card-body p-3" style="max-height: 600px;">
                        <ProductList :products="products" @select="selectProduct" />
                    </div>
                </div>

                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">Giỏ hàng</div>
                    <div class="card-body p-3">
                        <CartTable :items="currentInvoice.items" @removeItem="removeItem"
                            @changeQuantity="changeQuantity" />
                    </div>
                </div>
            </div>

            <!-- Khách hàng + Voucher + Thanh toán -->
            <div class="col-md-4 d-flex flex-column gap-3">
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">Khách hàng</div>
                    <div class="card-body p-3">
                        <!-- Ô tìm khách luôn trắng -->
                        <CustomerSelect
                            :key="currentInvoice.id + ':' + (currentInvoice.customerId ?? '') + ':' + customerSelectResetKey"
                            :customers="customers" :modelValue="customerSelectModel"
                            @update:modelValue="onCustomerPicked" />
                        <!-- Hiển thị khách đã chọn -->
                        <div class="d-flex align-items-center mt-2 flex-wrap gap-2">
                            <div class="small mb-0">
                                <span class="text-muted">Khách đã chọn: </span>
                                <b v-if="currentCustomer">
                                    {{ currentCustomer.fullName }}
                                    <span v-if="selectedPhone"> - {{ selectedPhone }}</span>
                                </b>
                                <b v-else>{{ WALK_IN.fullName }}</b>
                            </div>
                            <button class="btn btn-link btn-sm text-danger p-0 align-baseline"
                                @click="clearSelectedCustomer" :disabled="!currentInvoice.customerId">
                                Xoá khách
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Mã giảm giá -->
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">Mã giảm giá</div>
                    <div class="card-body p-3">
                        <div class="input-group mb-2">
                            <input v-model="currentInvoice.promotionCode" type="text" class="form-control"
                                placeholder="Nhập mã (VD: SUMMER10)" />
                            <button class="btn btn-primary" @click="applyPromotionCode()">Áp dụng</button>
                            <button class="btn btn-outline-secondary" @click="clearVoucher">Xoá</button>
                        </div>
                        <p v-if="currentInvoice.promoError" class="text-danger small mb-0">
                            {{ currentInvoice.promoError }}
                        </p>
                    </div>
                </div>

                <!-- Thanh toán -->
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">Thanh toán</div>
                    <div class="card-body p-3">
                        <div class="d-flex justify-content-between">
                            <span>Tạm tính</span>
                            <span>{{ subTotal.toLocaleString() }}₫</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span>Giảm giá</span>
                            <span>- {{ discountAmount.toLocaleString() }}₫</span>
                        </div>
                        <hr class="my-2" />
                        <div class="d-flex justify-content-between fs-5">
                            <span>Tổng thanh toán</span>
                            <b>{{ grandTotal.toLocaleString() }}₫</b>
                        </div>

                        <PaymentInfo :total="grandTotal" :modelValue="currentInvoice.paid" :change="change"
                            @update:modelValue="(val) => (currentInvoice.paid = val)" @checkout="handleCheckout" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Popup biến thể sản phẩm -->
        <ProductVariantModal v-if="selectedProduct" :product="selectedProduct" :variants="productVariants"
            @close="closeVariantModal" @confirm="addToInvoice" />
    </div>
</template>

<style scoped>
.card.shadow-sm {
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.btn-link {
    text-decoration: underline;
}
</style>
