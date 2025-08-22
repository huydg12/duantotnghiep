<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";

import ProductList from "../pos/ProductList.vue";
import ProductVariantModal from "../pos/ProductVariantModal.vue";
import InvoiceTabs from "../pos/InvoiceTabs.vue";
import CartTable from "../pos/CartTable.vue";
import CustomerSelect from "../pos/CustomerSelect.vue";
import PaymentInfo from "../pos/PaymentInfo.vue";

const products = ref([]);
const customers = ref([]);
const productVariants = ref([]);

const invoices = ref([{ customer: null, items: [], paid: 0 }]);
const activeInvoiceIndex = ref(0);

const selectedProduct = ref(null);

const currentInvoice = computed(() => invoices.value[activeInvoiceIndex.value]);

const subTotal = computed(() =>
    currentInvoice.value.items.reduce((sum, i) => sum + i.price * i.quantity, 0)
);

const grandTotal = computed(() =>
    Math.max(0, subTotal.value - discountAmount.value)
);

const change = computed(() => currentInvoice.value.paid - grandTotal.value);

watch(
    invoices,
    (val) => {
        localStorage.setItem("invoices", JSON.stringify(val));
    },
    { deep: true }
);

const fetchData = async () => {
    try {
        const resProduct = await axios.get("http://localhost:8080/product/show");
        const resCustomer = await axios.get("http://localhost:8080/customer/show");
        products.value = resProduct.data;
        customers.value = resCustomer.data;
        console.log(products.value);
        console.log(customers.value);
    } catch (err) {
        console.error("Lỗi khi tải dữ liệu sản phẩm hoặc khách hàng:", err);
    }
};

const selectProduct = async (product) => {
    try {
        const res = await axios.get(
            `http://localhost:8080/productDetail/show/${product.id}`
        );
        productVariants.value = res.data;
        selectedProduct.value = products.value.find((p) => p.id === product.id);
        console.log(productVariants.value);
    } catch (err) {
        console.error("Lỗi khi lấy chi tiết sản phẩm:", err);
    }
};

const addToInvoice = (variant) => {
    const existing = currentInvoice.value.items.find(
        (i) => i.productDetailId === variant.productDetailId
    );

    if (existing) {
        existing.quantity += variant.quantity;
    } else {
        currentInvoice.value.items.push({ ...variant });
    }

    selectedProduct.value = null;
    productVariants.value = [];
};

const removeItem = (index) => {
    currentInvoice.value.items.splice(index, 1);
};

const changeQuantity = (index, quantity) => {
    // GIỮ logic đơn giản như code cũ của bạn:
    currentInvoice.value.items[index].quantity = parseInt(quantity) || 1;

    // Nếu muốn "kẹp" theo tồn kho từ BE thì dùng đoạn dưới thay cho dòng trên:
    /*
    const item = currentInvoice.value.items[index];
    const q = parseInt(quantity) || 1;
    axios
      .get(`http://localhost:8080/inventory/getQuantity/${item.productDetailId}`)
      .then((invRes) => {
        const stock = invRes.data?.quantityInventory ?? 0;
        currentInvoice.value.items[index].quantity = Math.min(Math.max(1, q), stock);
        if (q > stock) alert(`Tồn kho chỉ còn ${stock}. Đã điều chỉnh!`);
      })
      .catch(() => {
        currentInvoice.value.items[index].quantity = Math.max(1, q);
      });
    */
};

const switchInvoice = (index) => (activeInvoiceIndex.value = index);
const addInvoice = () => invoices.value.push({ customer: null, items: [], paid: 0 });
const removeInvoice = (index) => invoices.value.splice(index, 1);

const discountAmountList = ref([]);
const promotionCode = ref("");
const selectedPromotion = ref(null);
const errorMessage = ref("");

const discountAmount = computed(() => {
    if (!selectedPromotion.value) return 0;

    const promo = selectedPromotion.value;
    const totalBeforeDiscount = subTotal.value;

    if (promo.type === 1) {
        // Giảm theo phần trăm
        const percent = Math.max(0, Math.min(100, Number(promo.value || 0)));
        const discount = (totalBeforeDiscount * percent) / 100;
        return Math.min(Math.floor(discount), totalBeforeDiscount);
    } else if (promo.type === 2) {
        // Giảm số tiền cố định
        return Math.min(Math.floor(Number(promo.value || 0)), totalBeforeDiscount);
    }

    return 0;
});

const fetchPromotion = async () => {
    try {
        const response = await axios.get('http://localhost:8080/promotion/show');
        discountAmountList.value = response.data;
    } catch (err) {
        console.error("Lỗi khuyến mãi", err);
    }
};

const promotionId = ref(null)
const applyPromotionCode = () => {
    const code = promotionCode.value.trim().toLowerCase();
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    const promo = discountAmountList.value.find(p => {
        const start = p.startDate ? new Date(p.startDate) : null;
        const end = p.endDate ? new Date(p.endDate) : null;
        if (end) end.setHours(23, 59, 59, 999);
        return (
            ((p.promotionCode || "").toLowerCase() === code) &&
            p.status === 1 &&
            (!start || start <= now) &&
            (!end || now <= end)
        );
    });

    if (promo) {
        selectedPromotion.value = promo;
        errorMessage.value = "";
        promotionId.value = promo.id;
    } else {
        selectedPromotion.value = null;
        promotionId.value = null;
        errorMessage.value = "Mã không hợp lệ hoặc đã hết hạn";
    }
};

const clearVoucher = () => {
    promotionCode.value = "";
    selectedPromotion.value = null;
    promotionId.value = null;
    errorMessage.value = "";
};

const handleCheckout = async () => {
    const invoice = currentInvoice.value;
    const user = JSON.parse(localStorage.getItem("user"));

    if (!invoice.customer) {
        alert("Vui lòng chọn khách hàng trước khi thanh toán!");
        return;
    }

    if (invoice.items.length === 0) {
        alert("Giỏ hàng đang trống!");
        return;
    }

    const dto = {
        customerId: invoice.customer.id,
        employeeId: user?.employeeId,
        ptttId: 1,
        promotionId: promotionId.value || null,
        billCode: `HD${Date.now()}`,
        billType: "OFFLINE",
        status: 4,
        recipientName: invoice.customer.fullName,
        recipientPhoneNumber: invoice.customer.phone,
        addressMethod: "TẠI_CỬA_HÀNG",
        statusPayment: "ĐÃ_THANH_TOÁN",

        subTotal: subTotal.value,
        discountAmount: discountAmount.value,
        grandTotal: grandTotal.value,

        billDetails: invoice.items.map((i) => ({
            productDetailId: i.productDetailId,
            quantity: i.quantity,
            price: i.price,
            productName: i.productName,
            color: i.color,
            size: i.size,
        })),
    };

    try {
        const response = await axios.post("http://localhost:8080/bill/create", dto);
        console.log("✅ Hóa đơn đã thêm:", response.data);
        alert("Thanh toán thành công!");

        // Reset invoice & voucher
        invoices.value[activeInvoiceIndex.value] = { customer: null, items: [], paid: 0 };
        promotionCode.value = ""; selectedPromotion.value = null;
        promotionId.value = null;
        errorMessage.value = "";
        localStorage.setItem("invoices", JSON.stringify(invoices.value));
    } catch (error) {
        console.error("Lỗi khi tạo hóa đơn:", error);
        alert(error?.response?.data?.message || "Lỗi khi thanh toán. Vui lòng thử lại.");
    }
};

onMounted(() => {
    const saved = localStorage.getItem("invoices");
    if (saved) {
        invoices.value = JSON.parse(saved);
    }

    fetchData();
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
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Giỏ hàng
                    </div>
                    <div class="card-body p-3">
                        <CartTable :items="currentInvoice.items" @removeItem="removeItem"
                            @changeQuantity="changeQuantity" />
                    </div>
                </div>
            </div>

            <!-- Khách hàng + Voucher + Thanh toán -->
            <div class="col-md-4 d-flex flex-column gap-3">
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Khách hàng
                    </div>
                    <div class="card-body p-3">
                        <CustomerSelect :customers="customers" :modelValue="currentInvoice.customer"
                            @update:modelValue="(val) => (currentInvoice.customer = val)" />
                    </div>
                </div>

                <!-- Mã giảm giá -->
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Mã giảm giá
                    </div>
                    <div class="card-body p-3">
                        <div class="input-group mb-2">
                            <input v-model="promotionCode" type="text" class="form-control"
                                placeholder="Nhập mã (VD: SUMMER10)" />
                            <button class="btn btn-primary" @click="applyPromotionCode()">Áp dụng</button>
                            <button class="btn btn-outline-secondary" @click="clearVoucher">Xoá</button>
                        </div>
                    </div>
                </div>

                <!-- Thanh toán -->
                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Thanh toán
                    </div>
                    <div class="card-body p-3">
                        <div class="d-flex justify-content-between">
                            <span>Tạm tính</span>
                            <span>{{ subTotal.toLocaleString() }}₫</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span>Giảm giá</span>
                            <span> - {{ discountAmount.toLocaleString() }}₫</span>
                        </div>
                        <hr class="my-2" />
                        <div class="d-flex justify-content-between fs-5">
                            <span>Tổng thanh toán</span>
                            <b>{{ grandTotal.toLocaleString() }}₫</b>
                        </div>

                        <!-- Giữ PaymentInfo để nhập tiền khách đưa & nút thanh toán -->
                        <PaymentInfo :total="grandTotal" :modelValue="currentInvoice.paid" :change="change"
                            @update:modelValue="(val) => (currentInvoice.paid = val)" @checkout="handleCheckout" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Popup biến thể sản phẩm -->
        <ProductVariantModal v-if="selectedProduct" :product="selectedProduct" :variants="productVariants" @close="
            () => {
                selectedProduct = null;
                productVariants = [];
            }
        " @confirm="addToInvoice" />
    </div>
</template>

<style scoped>
.card.shadow-sm {
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>
