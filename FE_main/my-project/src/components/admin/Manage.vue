<script setup>
import { ref, shallowRef, defineAsyncComponent, onMounted } from "vue";

import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
userStore.loadUserFromLocalStorage()


// shallowRef để tối ưu các component => thay đổi cả component
const activeComponent = shallowRef(
  defineAsyncComponent(() => import("../dashboard/StatisticsManagement.vue"))
);

// Ref theo dõi mục menu đang được chọn
const activeTarget = ref("StatisticsManagement");

// Map các component và dùng defineAsyncComponent
const componentMap = {
  Sell: defineAsyncComponent(() => import("../dashboard/Sell.vue")),
  ProductManagement: defineAsyncComponent(() =>
    import("../dashboard/ProductManagement.vue")
  ),
  BrandManagement: defineAsyncComponent(() =>
    import("../dashboard/BrandManagement.vue")
  ),
  CategoryManagement: defineAsyncComponent(() =>
    import("../dashboard/CategoryManagement.vue")
  ),
  CollarManagement: defineAsyncComponent(() =>
    import("../dashboard/CollarManagement.vue")
  ),
  SizeManagement: defineAsyncComponent(() =>
    import("../dashboard/SizeManagement.vue")
  ),
  SoleManagement: defineAsyncComponent(() =>
    import("../dashboard/SoleManagement.vue")
  ),
  ColorManagement: defineAsyncComponent(() =>
    import("../dashboard/ColorManagement.vue")
  ),
  ImageManagement: defineAsyncComponent(() =>
    import("../dashboard/ImageManagement.vue")
  ),
  InvoiceManagement: defineAsyncComponent(() =>
    import("../dashboard/InvoiceManagement.vue")
  ),
  StaffManagement: defineAsyncComponent(() =>
    import("../dashboard/StaffManagement.vue")
  ),
  CustomerManagement: defineAsyncComponent(() =>
    import("../dashboard/CustomerManagement.vue")
  ),
  AccountManagement: defineAsyncComponent(() =>
    import("../dashboard/AccountManagement.vue")
  ),
  PromotionManagement: defineAsyncComponent(() =>
    import("../dashboard/PromotionManagement.vue")
  ),
  ImportReceiptManagement: defineAsyncComponent(() =>
    import("../dashboard/ImportReceiptManagement.vue")
  ),
  WarehouseManagement: defineAsyncComponent(() =>
    import("../dashboard/WarehouseManagement.vue")
  ),
  StatisticsManagement: defineAsyncComponent(() =>
    import("../dashboard/StatisticsManagement.vue")
  ),
  PersonalInformation: defineAsyncComponent(() =>
    import("../dashboard/PersonalInformation.vue")
  ),
};

// Hàm thay đổi component để hiển thị
function loadContent(target) {
  activeComponent.value =
    componentMap[target] ||
    defineAsyncComponent(() => import("../dashboard/NotFound.vue"));
  activeTarget.value = target;
}

// Dữ kiệu của menu sidebar
const menuItems = [
  {
    id: "sell",
    label: "Bán hàng tại quầy",
    target: "Sell",
    icon: "fa-solid fa-cash-register",
  },
  {
    id: "product",
    label: "Sản phẩm",
    target: "ProductManagement",
    icon: "fa-solid fa-box-open",
  },
  {
    id: "attribute",
    label: "Thuộc tính",
    icon: "fa-solid fa-sliders",
    sub: [
      { label: "Hãng", target: "BrandManagement", icon: "fa-solid fa-tags" },
      { label: "Loại", target: "CategoryManagement", icon: "fa-solid fa-shapes" },
      { label: "Đế", target: "SoleManagement", icon: "fa-solid fa-shoe-prints" },
      { label: "Cổ", target: "CollarManagement", icon: "fa-solid fa-socks" },
      { label: "Size", target: "SizeManagement", icon: "fa-solid fa-ruler-horizontal" },
      { label: "Màu", target: "ColorManagement", icon: "fa-solid fa-palette" },
      { label: "Ảnh", target: "ImageManagement", icon: "fa-solid fa-image" },
    ],
  },
  {
    id: "invoice",
    label: "Hóa đơn",
    target: "InvoiceManagement",
    icon: "fa-solid fa-receipt",
  },
  {
    id: "staff",
    label: "Nhân viên",
    target: "StaffManagement",
    icon: "fa-solid fa-user-tie",
  },
  {
    id: "customer",
    label: "Khách hàng",
    target: "CustomerManagement",
    icon: "fa-solid fa-users",
  },
  {
    id: "account",
    label: "Tài khoản",
    target: "AccountManagement",
    icon: "fa-solid fa-user-cog",
  },
  {
    id: "promotion",
    label: "Khuyến mãi",
    target: "PromotionManagement",
    icon: "fa-solid fa-tag",
  },
  {
    id: "importreceipt",
    label: "Phiếu nhập",
    target: "ImportReceiptManagement",
    icon: "fa-solid fa-clipboard-list",
  },
  {
    id: "warehouse",
    label: "Kho",
    target: "WarehouseManagement",
    icon: "fa-solid fa-warehouse",
  },
  {
    id: "statistic",
    label: "Thống kê",
    target: "StatisticsManagement",
    icon: "fa-solid fa-chart-line",
  },
  {
    id: "personal",
    label: "Thông tin cá nhân",
    target: "PersonalInformation",
    icon: "fa-solid fa-id-card",
  },
  {
    id: "logout",
    label: "Đăng xuất",
    target: "logout",
    icon: "fa-solid fa-sign-out-alt",
  },
];

function handleClick(target) {
  if (target === 'logout') {
    userStore.logout()

    // Điều hướng bằng replace để không quay lại được
    router.replace("/auth/login").then(() => {
      // Reload để clear cache nội dung đã xem
      window.location.reload();
    });
  } else {
    loadContent(target)
  }
}

onMounted(() => {
  console.log('Thông tin user đang đăng nhập:', userStore.user)
})
</script>

<template>
  <div class="d-flex vh-100">
    <div class="sidebar">
      <div class="flex-grow-1 sidebar-scroll">
        <div class="text-center py-4 border-bottom border-secondary user-profile">
          <div
            class="bg-white rounded-circle mx-auto d-flex align-items-center justify-content-center shadow-sm user-icon">
            <i class="fa-solid fa-user text-dark fa-2x"></i>
          </div>
          <h2 class="mt-3 fs-5 fw-semibold">
            Xin chào, <span class="fw-bold">Admin</span>
          </h2>
        </div>

        <ul class="nav flex-column p-3">
          <li v-for="item in menuItems" :key="item.id" class="nav-item">
            <a v-if="!item.sub" href="#" class="nav-link" :class="{ active: activeTarget === item.target }"
              @click.prevent="handleClick(item.target)">
              <i :class="item.icon"></i> {{ item.label }}
            </a>

            <template v-else>
              <a class="nav-link d-flex align-items-center" data-bs-toggle="collapse" :href="'#' + item.id"
                role="button" aria-expanded="false" :aria-controls="item.id">
                <i :class="item.icon"></i>
                <span>{{ item.label }}</span>
                <i class="fa-solid fa-chevron-down ms-auto"></i>
              </a>
              <div class="collapse submenu-collapse" :id="item.id">
                <ul class="nav flex-column">
                  <li v-for="sub in item.sub" :key="sub.target">
                    <a href="#" class="nav-link" :class="{ active: activeTarget === sub.target }"
                      @click.prevent="loadContent(sub.target)">
                      <i :class="sub.icon"></i> {{ sub.label }}
                    </a>
                  </li>
                </ul>
              </div>
            </template>
          </li>
        </ul>
      </div>
    </div>

    <main class="flex-grow-1 p-4 main-content">
      <Transition name="fade" mode="out-in">
        <component :is="activeComponent" />
      </Transition>
    </main>
  </div>
</template>

<style scoped>
@import 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css';
@import 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css';

.sidebar {
  width: 288px;
  background-color: #212529;
  color: white;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.sidebar .user-profile .user-icon {
  width: 96px;
  height: 96px;
}

.sidebar .nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 0.75rem 1.25rem;
  display: flex;
  align-items: center;
  border-radius: 0.375rem;
  margin: 10px;
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
}

.sidebar .nav-link.active,
.sidebar .nav-link:hover {
  background-color: #495057;
  color: white;
}

.sidebar .nav-link i:first-child {
  width: 20px;
  text-align: center;
  margin-right: 0.75rem;
}

.submenu-collapse {
  background-color: rgba(0, 0, 0, 0.2);
}

.submenu-collapse .nav-link {
  padding: 0.6rem 1rem 0.6rem 3rem;
  font-size: 0.9rem;
}


.nav-link[data-bs-toggle="collapse"] .fa-chevron-down {
  transition: transform 0.3s ease;
}

.nav-link[data-bs-toggle="collapse"][aria-expanded="true"] .fa-chevron-down {
  transform: rotate(180deg);
}

.main-content {
  background-color: #f8f9fa;
  overflow-y: auto;
}

.sidebar-scroll {
  overflow-y: scroll;
}

.sidebar-scroll::-webkit-scrollbar {
  width: 8px;
}

.sidebar-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
}

.sidebar:hover .sidebar-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.5);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
