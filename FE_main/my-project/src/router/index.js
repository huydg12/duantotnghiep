import { createRouter, createWebHistory } from "vue-router";

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Payment from "../components/Payment.vue";
import ProductDetail from "../components/ProductDetail.vue";
import InformationCustomer from "../components/InformationCustomer.vue";
import ForgetPassword from "../components/ForgetPassword.vue";
import Introduce from "../components/Introduce.vue";
import Product from "../components/Product.vue";
import Contact from "../components/Contact.vue";
import Cart from "../components/Cart.vue";
import ResertPassword from "../components/ResertPassword.vue";
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
      path: "/informationcustomer",
      name: "informationcustomer",
      component: InformationCustomer,
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
      path: "/productdetail/:id",
      name: "productdetail",
      component: ProductDetail,
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
      path: "/payment",
      name: "payment",
      component: Payment,
    },
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
      path: "/forgetpassword",
      name: "forgetpassword",
      component: ForgetPassword,
    },
    {
      path: "/verifycode",
      name: "verifycode",
      component: VerifyCode,
    },
    {
      path: "/resertpassword",
      name: "resertpassword",
      component: ResertPassword,
    },
    {
      path: "/manage",
      name: "manage",
      component: Manage,
    },
  ],
});

export default router;
