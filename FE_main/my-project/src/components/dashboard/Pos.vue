<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";

import ProductList from "../pos/ProductList.vue";
import ProductVariantModal from "../pos/ProductVariantModal.vue";
import InvoiceTabs from "../pos/InvoiceTabs.vue";
import CartTable from "../pos/CartTable.vue";
import CustomerSelect from "../pos/CustomerSelect.vue";
import PaymentInfo from "../pos/PaymentInfo.vue";

// Dữ liệu sản phẩm, khách hàng và biến thể
const products = ref([]);
const customers = ref([]);
const productVariants = ref([]);

const invoices = ref([{ customer: null, items: [], paid: 0 }]);
const activeInvoiceIndex = ref(0);

const selectedProduct = ref(null);

const currentInvoice = computed(() => invoices.value[activeInvoiceIndex.value]);
const total = computed(() =>
    currentInvoice.value.items.reduce((sum, i) => sum + i.price * i.quantity, 0)
);
const change = computed(() => currentInvoice.value.paid - total.value);

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
    currentInvoice.value.items[index].quantity = quantity;
};

const switchInvoice = (index) => (activeInvoiceIndex.value = index);
const addInvoice = () => invoices.value.push({ customer: null, items: [], paid: 0 });
const removeInvoice = (index) => invoices.value.splice(index, 1);

const checkout = () => {
    alert("Thanh toán thành công!");
    invoices.value[activeInvoiceIndex.value] = {
        customer: null,
        items: [],
        paid: 0,
    };
    localStorage.setItem("invoices", JSON.stringify(invoices.value));
};

const handleCheckout = async () => {
    const invoice = currentInvoice.value;
    const user = JSON.parse(localStorage.getItem("user"));

    if (!invoice.customer) {
        alert("Vui lòng chọn khách hàng trước khi thanh toán!");
        return;
    }

    const dto = {
        customerId: invoice.customer.id,
        employeeId: user?.employeeId,
        ptttId: 1,
        promotionId: null,
        billCode: (`HD${Date.now()}`),
        billType: "OFFLINE",
        status: 4,
        recipientName: invoice.customer.fullName,
        recipientPhoneNumber: invoice.customer.phone,
        addressMethod: "TẠI_CỬA_HÀNG",
        statusPayment: "ĐÃ_THANH_TOÁN",
        subTotal: total.value,
        discountAmount: 0,
        shippingFee: 0,
        grandTotal: total.value,
        billDetails: invoice.items.map(i => ({
            productDetailId: i.productDetailId,
            quantity: i.quantity,
            price: i.price,
            productName: i.productName,
            color: i.color,
            size: i.size
        }))
    };

    try {
        const response = await axios.post("http://localhost:8080/bill/create", dto);
        console.log("✅ Hóa đơn đã thêm:", response.data); // <-- LOG Ở ĐÂY
        alert("Thanh toán thành công!");
        // Reset invoice sau khi thanh toán
        invoices.value[activeInvoiceIndex.value] = {
            customer: null,
            items: [],
            paid: 0
        };
        localStorage.setItem("invoices", JSON.stringify(invoices.value));
    } catch (error) {
        console.error("Lỗi khi tạo hóa đơn:", error);
        alert("Lỗi khi thanh toán. Vui lòng thử lại.");
    }
};

onMounted(() => {
    const saved = localStorage.getItem("invoices");
    if (saved) {
        invoices.value = JSON.parse(saved);
    }

    fetchData();
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

            <!-- Khách hàng + Thanh toán -->
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

                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Thanh toán
                    </div>
                    <div class="card-body p-3">
                        <PaymentInfo :total="total" :modelValue="currentInvoice.paid" :change="change"
                            @update:modelValue="val => currentInvoice.paid = val" @checkout="handleCheckout" />
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
