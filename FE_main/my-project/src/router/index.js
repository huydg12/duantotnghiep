import { createRouter, createWebHistory } from "vue-router";
// Layout
import CustomerLayout from "../components/layout/CustomerLayout.vue";

// Customer Pages
import Home from "../components/customer/Home.vue";
import Payment from "../components/customer/Payment.vue";
import ProductDetail from "../components/customer/ProductDetail.vue";
import InformationCustomer from "../components/customer/InformationCustomer.vue";
import Introduce from "../components/customer/Introduce.vue";
import Product from "../components/customer/Product.vue";
import Contact from "../components/customer/Contact.vue";
import Cart from "../components/customer/Cart.vue";
import Favorite from "../components/customer/Favorite.vue";

// Auth Pages
import Auth from "../components/auth/Auth.vue";
import Login from "../components/auth/Login.vue";
import Register from "../components/auth/Register.vue";
import ForgetPassword from "../components/auth/ForgetPassword.vue";
import ResetPassword from "../components/auth/ResetPassword.vue";
import VerifyCode from "../components/auth/VerifyCode.vue";

// Admin Page
import Manage from "../components/admin/Manage.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Layout customer
    {
      path: "/",
      component: CustomerLayout,
      children: [
        { path: "", redirect: "/home" },
        {
          path: "home",
          name: "home",
          component: Home,
        },
        { path: "product", name: "product", component: Product },
        {
          path: "productdetail/:id",
          name: "productdetail",
          component: ProductDetail,
        },
        { path: "contact", name: "contact", component: Contact },
        {
          path: "cart",
          name: "cart",
          component: Cart,
          meta: { requiresAuth: true },
        },
        {
          path: "payment",
          name: "payment",
          component: Payment,
          meta: { requiresAuth: true },
        },
        { path: "introduce", name: "introduce", component: Introduce },
        {
          path: "favorite",
          name: "favorite",
          component: Favorite,
          meta: { requiresAuth: true },
        },
        {
          path: "informationcustomer",
          name: "informationcustomer",
          component: InformationCustomer,
        },
        {
          path: "invoicecustomer",
          name: "invoicecustomer",
          component: InvoiceCustomer,
        },
      ],
    },

    // Auth
    {
      path: "/auth",
      component: Auth,
      redirect: "/auth/login",
      children: [
        {
          path: "login",
          name: "login",
          component: Login,
        },
        {
          path: "register",
          name: "register",
          component: Register,
        },
      ],
    },
    {
      path: "/auth/forgetpassword",
      name: "forgetpassword",
      component: ForgetPassword,
    },
    {
      path: "/auth/verifycode",
      name: "verifycode",
      component: VerifyCode,
    },
    {
      path: "/auth/resetpassword",
      name: "resetpassword",
      component: ResetPassword,
    },

    // Manage
    {
      path: "/manage",
      name: "manage",
      component: Manage,
      meta: { requiresAuth: true },
    },
  ],
});

import { useUserStore } from "@/stores/userStore";
import InvoiceCustomer from "../components/customer/InvoiceCustomer.vue";

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  userStore.loadUserFromLocalStorage();
  const user = userStore.user;

  const requiresAuth = to.meta.requiresAuth === true;

  // Nếu chưa đăng nhập
  if (!user) {
    if (requiresAuth) {
      return next("/auth/login");
    }
    return next(); // Vào home, introduce, v.v. được
  }

  // Nếu đã đăng nhập
  const role = user.role || user.roleName || user.roleId;

  // Ngăn không cho quay lại trang login
  if (to.path.startsWith("/auth")) {
    return next(role === "CUSTOMER" || role === 2 ? "/home" : "/manage");
  }

  // Nếu là ADMIN hoặc EMPLOYEE mà vào /home thì chuyển về /manage
  if (
    (role === "ADMIN" || role === "EMPLOYEE" || role === 1 || role === 3) &&
    to.path === "/home"
  ) {
    return next("/manage");
  }

  // Nếu là CUSTOMER mà cố vào /manage thì chuyển về /home
  if ((role === "CUSTOMER" || role === 2) && to.path.startsWith("/manage")) {
    return next("/home");
  }

  // Mặc định cho đi tiếp
  return next();
});

export default router;
