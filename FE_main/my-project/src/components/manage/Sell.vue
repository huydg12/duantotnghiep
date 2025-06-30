<script setup>
import { ref, computed, watch } from "vue";

// Dữ liệu sản phẩm với các biến thể (size, màu, tồn kho)
const products = ref([
    {
        id: 1,
        name: "Giày Nike Air Force 1",
        image: "https://via.placeholder.com/150/EEEEEE/000000?text=Nike+AF1",
        variants: [
            { sku: "NK-AF1-WH-40", color: "Trắng", size: 40, price: 2500000, stock: 10 },
            { sku: "NK-AF1-WH-41", color: "Trắng", size: 41, price: 2500000, stock: 5 },
            { sku: "NK-AF1-WH-42", color: "Trắng", size: 42, price: 2500000, stock: 0 },
            { sku: "NK-AF1-BL-40", color: "Đen", size: 40, price: 2600000, stock: 8 },
            { sku: "NK-AF1-BL-41", color: "Đen", size: 41, price: 2600000, stock: 12 },
        ],
    },
    {
        id: 2,
        name: "Giày Adidas Ultraboost",
        image: "https://via.placeholder.com/150/000000/FFFFFF?text=Adidas+UB",
        variants: [
            { sku: "AD-UB-BL-39", color: "Đen", size: 39, price: 3200000, stock: 7 },
            { sku: "AD-UB-BL-40", color: "Đen", size: 40, price: 3200000, stock: 9 },
            { sku: "AD-UB-GY-40", color: "Xám", size: 40, price: 3100000, stock: 4 },
        ],
    },
]);

// Logic khách hàng, hóa đơn, thanh toán
const customers = ref([
    { id: 101, name: "Nguyễn Văn An", phone: "0901234567" },
    { id: 102, name: "Trần Thị Bình", phone: "0912345678" },
]);

const showCustomerSuggestions = ref(false);
const initialInvoices = JSON.parse(localStorage.getItem("invoices_state")) || [
    {
        id: "INV" + Date.now(), name: "Hoá Đơn 1", cart: [], discountInput: "",
        cashReceived: null, customerName: "", customerId: null,
    },
];
const invoices = ref(initialInvoices);
const currentInvoiceIndex = ref(0);

watch(invoices, (newInvoices) => {
    localStorage.setItem("invoices_state", JSON.stringify(newInvoices));
}, { deep: true });

const currentInvoice = computed(() => invoices.value[currentInvoiceIndex.value]);
const searchQuery = ref("");

const filteredProducts = computed(() => {
    const q = searchQuery.value.toLowerCase();
    return products.value.filter((p) => p.name.toLowerCase().includes(q));
});

const customerSuggestions = computed(() => {
    if (!currentInvoice.value || !currentInvoice.value.customerName) return [];
    const query = currentInvoice.value.customerName.toLowerCase().trim();
    if (query.length < 2) return [];
    return customers.value.filter(c => c.name.toLowerCase().includes(query) || c.phone.includes(query));
});

const subtotal = computed(() => {
    if (!currentInvoice.value) return 0;
    return currentInvoice.value.cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

const discountAmountValue = computed(() => {
    if (!currentInvoice.value) return 0;
    const val = currentInvoice.value.discountInput;
    if (val.includes("%")) {
        const percent = parseFloat(val.replace("%", "")) || 0;
        return (subtotal.value * percent) / 100;
    }
    return parseFloat(val) || 0;
});

const total = computed(() => Math.max(0, subtotal.value - discountAmountValue.value));

const cartItemCount = computed(() => {
    if (!currentInvoice.value) return 0;
    return currentInvoice.value.cart.reduce((count, item) => count + item.quantity, 0);
});

const change = computed(() => {
    if (!currentInvoice.value) return 0;
    const cash = currentInvoice.value.cashReceived;
    return cash && cash >= total.value ? cash - total.value : 0;
});

const formatCurrency = (val) => new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val || 0);

// State để quản lý popup chọn thuộc tính
const selectedProductForModal = ref(null);
const selectedColor = ref(null);
const selectedSize = ref(null);

// Mở popup
const openAttributeModal = (product) => {
    selectedProductForModal.value = product;
    selectedColor.value = null;
    selectedSize.value = null;
};

const closeModal = () => {
    selectedProductForModal.value = null;
};

// Computed để lọc thuộc tính trong popup
const availableColors = computed(() => {
    if (!selectedProductForModal.value) return [];
    const colors = selectedProductForModal.value.variants.map(v => v.color);
    return [...new Set(colors)];
});

const availableSizes = computed(() => {
    if (!selectedProductForModal.value || !selectedColor.value) return [];
    return selectedProductForModal.value.variants
        .filter(v => v.color === selectedColor.value)
        .map(v => ({ size: v.size, stock: v.stock }));
});

const finalSelectedVariant = computed(() => {
    if (!selectedProductForModal.value || !selectedColor.value || !selectedSize.value) return null;
    return selectedProductForModal.value.variants.find(
        v => v.color === selectedColor.value && v.size === selectedSize.value
    );
});

// Logic giỏ hàng làm việc với biến thể (variant) và sku
const addVariantToCart = () => {
    if (!finalSelectedVariant.value) return;

    const variant = finalSelectedVariant.value;
    if (variant.stock <= 0) {
        alert("Sản phẩm này đã hết hàng!");
        return;
    }

    const cart = currentInvoice.value.cart;
    const foundItem = cart.find(item => item.sku === variant.sku);

    if (foundItem) {
        if (foundItem.quantity < variant.stock) {
            foundItem.quantity++;
        } else {
            alert(`Xin lỗi, chỉ còn ${variant.stock} sản phẩm trong kho.`);
        }
    } else {
        cart.push({
            productId: selectedProductForModal.value.id,
            name: selectedProductForModal.value.name,
            ...variant,
            quantity: 1,
        });
    }
    closeModal();
};

const updateQuantity = (item) => {
    if (item.quantity < 1) item.quantity = 1;
    if (item.quantity > item.stock) {
        alert(`Số lượng không thể vượt quá tồn kho (${item.stock})`);
        item.quantity = item.stock;
    }
};

const removeFromCart = (sku) => {
    currentInvoice.value.cart = currentInvoice.value.cart.filter(item => item.sku !== sku);
};

// Các hàm xử lý hóa đơn, khách hàng...
const resetCurrentInvoice = () => {
    const inv = currentInvoice.value;
    inv.cart = [];
    inv.discountInput = "";
    inv.cashReceived = null;
    inv.customerName = "";
    inv.customerId = null;
};
const clearCart = () => {
    if (confirm("Hủy toàn bộ hóa đơn này? Thao tác này không thể hoàn tác.")) {
        resetCurrentInvoice();
    }
};

const processPayment = () => {
    if (currentInvoice.value.cart.length === 0) return alert("Chưa có sản phẩm trong giỏ hàng.");
    if (currentInvoice.value.cashReceived < total.value) return alert("Số tiền khách đưa chưa đủ.");
    const customer = currentInvoice.value.customerName || "Khách lẻ";
    const paymentSuccessMessage = `Thanh toán thành công!\nKhách hàng: ${customer}\nTổng tiền: ${formatCurrency(total.value)}\nTiền khách đưa: ${formatCurrency(currentInvoice.value.cashReceived)}\nTiền thừa: ${formatCurrency(change.value)}`;
    alert(paymentSuccessMessage);
    resetCurrentInvoice();
};

const addNewInvoice = () => {
    invoices.value.push({
        id: "INV" + Date.now(), name: `Hoá Đơn ${invoices.value.length + 1}`, cart: [], discountInput: "",
        cashReceived: null, customerName: "", customerId: null,
    });
    currentInvoiceIndex.value = invoices.value.length - 1;
};

const removeInvoice = (indexToRemove) => {
    if (confirm(`Bạn có chắc chắn muốn xóa "${invoices.value[indexToRemove].name}"?`)) {
        if (indexToRemove < currentInvoiceIndex.value) {
            currentInvoiceIndex.value--;
        }
        invoices.value.splice(indexToRemove, 1);
        if (currentInvoiceIndex.value >= invoices.value.length) {
            currentInvoiceIndex.value = invoices.value.length - 1;
        }
    }
};

const setCashReceived = (amount) => {
    currentInvoice.value.cashReceived = amount;
};

const selectCustomer = (customer) => {
    currentInvoice.value.customerName = customer.name;
    currentInvoice.value.customerId = customer.id;
    showCustomerSuggestions.value = false;
};

const handleCustomerInputFocus = () => {
    showCustomerSuggestions.value = true;
};

const handleCustomerInputBlur = () => {
    setTimeout(() => { showCustomerSuggestions.value = false; }, 200);
};

watch(() => currentInvoice.value?.customerName, (newName) => {
    if (currentInvoice.value && !newName) {
        currentInvoice.value.customerId = null;
    }
});

</script>

<template>
    <div id="app">
        <div class="nav nav-tabs mb-3">
            <button class="nav-link" :class="{ active: currentInvoiceIndex === i }" v-for="(inv, i) in invoices"
                :key="inv.id" @click="currentInvoiceIndex = i">
                {{ inv.name }}
                <i class="bi bi-x-circle ms-2 text-danger" @click.stop="removeInvoice(i)"
                    v-if="invoices.length > 1"></i>
            </button>
            <button class="btn btn-sm btn-outline-primary ms-2" @click="addNewInvoice">
                <i class="bi bi-plus-circle"></i> Hóa đơn mới
            </button>
        </div>

        <main class="main-content container-fluid mt-3" v-if="currentInvoice">
            <div class="row h-100">
                <div class="col-lg-5">
                    <div class="card h-100">
                        <div class="card-body d-flex flex-column">
                            <div class="input-group mb-3">
                                <span class="input-group-text"><i class="bi bi-search"></i></span>
                                <input type="text" class="form-control" placeholder="Tìm sản phẩm (theo tên)..."
                                    v-model="searchQuery" />
                            </div>
                            <div class="product-grid">
                                <div class="card product-card" v-for="product in filteredProducts" :key="product.id"
                                    @click="openAttributeModal(product)">
                                    <img :src="product.image" class="card-img-top" :alt="product.name" />
                                    <div class="card-body p-2 text-center">
                                        <h6 class="card-title fs-sm mb-1 text-truncate"> {{ product.name }} </h6>
                                        <p class="card-text text-danger fw-bold">
                                            Từ {{ formatCurrency(product.variants[0].price) }}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="card h-100">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title text-primary"><i class="bi bi-receipt-cutoff me-2"></i>Chi Tiết Hóa
                                Đơn</h5>
                            <hr />

                            <div class="mb-3 position-relative">
                                <label for="customerNameInput" class="form-label">
                                    Tìm khách hàng (Tên hoặc SĐT)
                                    <i v-if="currentInvoice.customerId"
                                        class="bi bi-check-circle-fill text-success"></i>
                                </label>
                                <input type="text" id="customerNameInput" class="form-control"
                                    v-model="currentInvoice.customerName" placeholder="Nhập tên hoặc SĐT để tìm..."
                                    autocomplete="off" @focus="handleCustomerInputFocus"
                                    @blur="handleCustomerInputBlur" />
                                <ul v-if="showCustomerSuggestions && customerSuggestions.length > 0"
                                    class="customer-suggestions">
                                    <li v-for="customer in customerSuggestions" :key="customer.id"
                                        @click="selectCustomer(customer)">
                                        <div class="fw-bold">{{ customer.name }}</div>
                                        <small class="text-muted">{{ customer.phone }}</small>
                                    </li>
                                </ul>
                            </div>

                            <div class="cart-items flex-grow-1">
                                <table class="table table-sm table-hover">
                                    <thead>
                                        <tr>
                                            <th>Sản Phẩm</th>
                                            <th class="text-center">SL</th>
                                            <th class="text-end">Thành Tiền</th>
                                            <th class="text-center">Xóa</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-if="currentInvoice.cart.length === 0">
                                            <td colspan="4" class="text-center text-muted py-5">
                                                <i class="bi bi-cart-x fs-1"></i>
                                                <p>Chưa có sản phẩm</p>
                                            </td>
                                        </tr>
                                        <tr v-for="item in currentInvoice.cart" :key="item.sku">
                                            <td>
                                                <div class="fw-bold">{{ item.name }}</div>
                                                <small class="text-muted">{{ item.color }} - Size: {{ item.size
                                                    }}</small>
                                                <div><small>{{ formatCurrency(item.price) }}</small></div>
                                            </td>
                                            <td class="text-center align-middle">
                                                <input type="number" min="1" :max="item.stock"
                                                    class="form-control form-control-sm quantity-input"
                                                    v-model.number="item.quantity" @change="updateQuantity(item)" />
                                            </td>
                                            <td class="text-end align-middle fw-bold">
                                                {{ formatCurrency(item.price * item.quantity) }}
                                            </td>
                                            <td class="text-center align-middle">
                                                <button class="btn btn-sm btn-outline-danger"
                                                    @click="removeFromCart(item.sku)">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                            <hr />
                            <div class="summary">
                                <div class="d-flex justify-content-between mb-2">
                                    <span>Tổng cộng ({{ cartItemCount }} sản phẩm):</span>
                                    <span class="fw-bold">{{ formatCurrency(subtotal) }}</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span>Giảm giá:</span>
                                    <span class="fw-bold text-success">-{{ formatCurrency(discountAmountValue) }}</span>
                                </div>
                                <div
                                    class="d-flex justify-content-between align-items-center total-row pt-2 border-top">
                                    <span>KHÁCH CẦN TRẢ:</span>
                                    <span>{{ formatCurrency(total) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3">
                    <div class="payment-section h-100 d-flex flex-column">
                        <h5 class="text-primary mb-3"><i class="bi bi-cash-coin me-2"></i>Thanh Toán</h5>
                        <div class="mb-3">
                            <label for="discountInput" class="form-label">Giảm giá (VND hoặc %)</label>
                            <input type="text" id="discountInput" class="form-control"
                                v-model="currentInvoice.discountInput" />
                        </div>
                        <div class="mb-3">
                            <label for="cashReceivedInput" class="form-label">Tiền khách đưa</label>
                            <input type="number" id="cashReceivedInput" class="form-control"
                                v-model.number="currentInvoice.cashReceived" min="0" />
                        </div>
                        <div class="mb-3 d-flex flex-wrap gap-2">
                            <button @click="setCashReceived(100000)" class="btn btn-sm btn-outline-info">100K</button>
                            <button @click="setCashReceived(200000)" class="btn btn-sm btn-outline-info">200K</button>
                            <button @click="setCashReceived(500000)" class="btn btn-sm btn-outline-info">500K</button>
                            <button @click="setCashReceived(total)" class="btn btn-sm btn-outline-success">Vừa
                                đủ</button>
                        </div>
                        <div class="d-flex justify-content-between fs-5 mb-3">
                            <span>Tiền thừa:</span>
                            <span class="fw-bold text-info">{{ formatCurrency(change) }}</span>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-danger btn-lg w-100 mb-2" @click="clearCart"
                                :disabled="currentInvoice.cart.length === 0">
                                <i class="bi bi-x-circle me-2"></i>HỦY
                            </button>
                            <button class="btn btn-success btn-lg w-100" @click="processPayment"
                                :disabled="currentInvoice.cart.length === 0 || !currentInvoice.cashReceived || currentInvoice.cashReceived < total">
                                <i class="bi bi-check-circle me-2"></i>THANH TOÁN
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </main>

        <div class="modal-overlay" v-if="selectedProductForModal" @click.self="closeModal">
            <div class="modal-content">
                <button class="btn-close" @click="closeModal"></button>
                <h4 class="mb-3">{{ selectedProductForModal.name }}</h4>

                <div class="mb-3">
                    <h6 class="form-label">Màu sắc:</h6>
                    <div class="d-flex flex-wrap gap-2">
                        <button v-for="color in availableColors" :key="color" class="btn btn-sm"
                            :class="selectedColor === color ? 'btn-primary' : 'btn-outline-secondary'"
                            @click="selectedColor = color; selectedSize = null">
                            {{ color }}
                        </button>
                    </div>
                </div>

                <div class="mb-4" v-if="selectedColor">
                    <h6 class="form-label">Size:</h6>
                    <div class="d-flex flex-wrap gap-2">
                        <button v-for="item in availableSizes" :key="item.size" class="btn btn-sm"
                            :class="selectedSize === item.size ? 'btn-primary' : 'btn-outline-secondary'"
                            :disabled="item.stock <= 0" @click="selectedSize = item.size">
                            {{ item.size }}
                            <span v-if="item.stock <= 0" class="badge bg-danger ms-1">Hết</span>
                        </button>
                    </div>
                </div>

                <div v-if="finalSelectedVariant" class="alert alert-info">
                    <div class="d-flex justify-content-between">
                        <span>Giá:</span>
                        <span class="fw-bold">{{ formatCurrency(finalSelectedVariant.price) }}</span>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>Tồn kho:</span>
                        <span class="fw-bold">{{ finalSelectedVariant.stock }}</span>
                    </div>
                </div>

                <button class="btn btn-success w-100 mt-3"
                    :disabled="!finalSelectedVariant || finalSelectedVariant.stock <= 0" @click="addVariantToCart">
                    <i class="bi bi-cart-plus me-2"></i>Thêm vào giỏ hàng
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* CSS cho popup chọn thuộc tính */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1050;
}

.modal-content {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    width: 90%;
    max-width: 500px;
    position: relative;
}

.modal-content .btn-close {
    position: absolute;
    top: 1rem;
    right: 1rem;
}

/* CSS cho gợi ý khách hàng */
.customer-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    z-index: 1000;
    border: 1px solid #dee2e6;
    border-top: none;
    border-radius: 0 0 0.375rem 0.375rem;
    background-color: white;
    list-style-type: none;
    padding: 0;
    margin: 0;
    max-height: 200px;
    overflow-y: auto;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.customer-suggestions li {
    padding: 0.5rem 1rem;
    cursor: pointer;
}

.customer-suggestions li:hover {
    background-color: #f8f9fa;
}

.customer-suggestions li:not(:last-child) {
    border-bottom: 1px solid #eee;
}

/* Các style khác */
#app {
    padding: 1rem;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
    max-height: 70vh;
    overflow-y: auto;
    padding-right: 10px;
}

.product-card {
    cursor: pointer;
    border: 1px solid #ddd;
    transition: all 0.2s ease-in-out;
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-card img {
    width: 100%;
    height: 100px;
    object-fit: cover;
}

.cart-items {
    max-height: 45vh;
    overflow-y: auto;
}

.quantity-input {
    width: 60px;
    text-align: center;
}

.total-row {
    font-size: 1.3rem;
    font-weight: bold;
    color: #dc3545;
}

.payment-section {
    background: #f8f9fa;
    padding: 1rem;
    border-radius: 8px;
    border: 1px solid #eee;
}

.product-grid::-webkit-scrollbar,
.cart-items::-webkit-scrollbar {
    width: 8px;
}

.product-grid::-webkit-scrollbar-track,
.cart-items::-webkit-scrollbar-track {
    background: #f1f1f1;
}

.product-grid::-webkit-scrollbar-thumb,
.cart-items::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
}

.product-grid::-webkit-scrollbar-thumb:hover,
.cart-items::-webkit-scrollbar-thumb:hover {
    background: #555;
}
</style>