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

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.loadUserFromLocalStorage()
  const user = userStore.user

  if (user) {
    // Không cho admin/employee vào home
    if ((user.role === 'ADMIN' || user.role === 'EMPLOYEE') && to.path === '/home') {
      return next('/manage')
    }

    // Không cho đã đăng nhập quay lại login
    if (to.path === '/auth/login') {
      return next(user.role === 'CUSTOMER' ? '/home' : '/manage')
    }

    next() // Cho đi tiếp nếu không vi phạm
  } else {
    // Chưa đăng nhập thì chỉ chặn các trang yêu cầu đăng nhập như /cart, /informationcustomer, /manage
    const protectedPaths = ['/cart', '/informationcustomer', '/manage']
    if (protectedPaths.includes(to.path)) {
      return next('/auth/login')
    }

    next() // Cho đi tiếp vào /home và các trang công khai khác
  }
})


export default router;
