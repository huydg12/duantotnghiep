<script setup>
import { ref, computed, onMounted } from "vue";
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

// Fetch dữ liệu ban đầu
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
        (i) => i.detailId === variant.id
    );
    if (existing) {
        existing.quantity++;
    } else {
        currentInvoice.value.items.push({ ...variant, quantity: 1 });
    }
    selectedProduct.value = null;
    productVariants.value = [];
};

const removeItem = (index) => currentInvoice.value.items.splice(index, 1);
const changeQuantity = (index, quantity) =>
    (currentInvoice.value.items[index].quantity = quantity);

const switchInvoice = (index) => (activeInvoiceIndex.value = index);
const addInvoice = () =>
    invoices.value.push({ customer: null, items: [], paid: 0 });
const removeInvoice = (index) => invoices.value.splice(index, 1);

const checkout = () => {
    alert("Thanh toán thành công!");
    invoices.value[activeInvoiceIndex.value] = {
        customer: null,
        items: [],
        paid: 0,
    };
};

onMounted(fetchData);
</script>

<template>
    <div class="container py-4">
        <!-- Tabs hóa đơn -->
        <InvoiceTabs :invoices="invoices" :activeIndex="activeInvoiceIndex" @switch="switchInvoice" @add="addInvoice"
            @remove="removeInvoice" class="mb-4" />

        <div class="row g-4">
            <!-- Danh sách sản phẩm -->
            <div class="col-md-8">
                <div class="card h-100 shadow-sm">
                    <div class="card-header bg-primary text-white fw-bold">
                        Danh sách sản phẩm
                    </div>
                    <div class="card-body p-3" style="max-height: 600px;">
                        <ProductList :products="products" @select="selectProduct" />
                    </div>
                </div>
            </div>

            <!-- Khách hàng + Giỏ hàng + Thanh toán -->
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
                        Giỏ hàng
                    </div>
                    <div class="card-body p-3">
                        <CartTable :items="currentInvoice.items" @removeItem="removeItem"
                            @changeQuantity="changeQuantity" />
                    </div>
                </div>

                <div class="card shadow-sm">
                    <div class="card-header bg-secondary text-white fw-semibold">
                        Thanh toán
                    </div>
                    <div class="card-body p-3">
                        <PaymentInfo :total="total" :modelValue="currentInvoice.paid" :change="change"
                            @update:modelValue="(val) => (currentInvoice.paid = val)" @checkout="checkout" />
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
