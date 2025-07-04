import { createRouter, createWebHistory } from "vue-router";

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Payment from "../components/Payment.vue";
import ProductDetail from "../components/ProductDetail.vue";
import InformationCustomer from "../components/InformationCustomer.vue";
import ForgetPass from "../components/ForgetPass.vue";
import Introduce from "../components/Introduce.vue";
import Product from "../components/Product.vue";
import Contact from "../components/Contact.vue";
import Cart from "../components/Cart.vue";
import ResetPass from "../components/ResetPass.vue";
import VerifyCode from "../components/VerifyCode.vue";
import Register from "../components/Register.vue";
import Auth from "../components/Auth.vue";
import Manage from "../components/Manage.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "app",
      component: Home,
    },
    {
      path: "/home",
      name: "home",
      component: Home,
    },

    {
      path: "/introduce",
      name: "introduce",
      component: Introduce,
    },

    {
      path: "/product",
      name: "product",
      component: Product,
    },
    {
      path: "/contact",
      name: "contact",
      component: Contact,
    },
    {
      path: "/cart",
      name: "cart",
      component: Cart,
    },
    {
      path: "/auth",
      component: Auth,
      redirect: "/auth/login",
      children: [
        {
          path: "login",
          name: "Login",
          component: Login,
        },
        {
          path: "register",
          name: "Register",
          component: Register,
        },
      ],
    },
    {
      path: "/payment",
      name: "payment",
      component: Payment,
    },
    {
      path: "/resetPass",
      name: "resetPass",
      component: ResetPass,
    },
    {
      path: "/verifyCode",
      name: "verifyCode",
      component: VerifyCode,
    },
    {
      path: "/productDetail/:id",
      name: "productDetail",
      component: ProductDetail,
    },
    {
      path: "/manage",
      name: "Manage",
      component: Manage,
    },
  ],
});

export default router;
