import {createRouter, createWebHistory} from "vue-router";
import {useUserStore} from "@/stores/userStore";

const lazy = (path) => () => import(/* @vite-ignore */ path);
const CustomerLayout = () => import("../components/layout/CustomerLayout.vue");

const routes = [
    {
        path: "/",
        component: CustomerLayout,
        children: [
            {path: "", redirect: "/home"},
            {path: "home", name: "home", component: () => import("../components/customer/Home.vue"), meta: {title: "Trang chủ"}},
            {path: "product", name: "product", component: () => import("../components/customer/Product.vue"), meta: {title: "Sản phẩm"}},
            {path: "productdetail/:id", name: "productdetail", component: () => import("../components/customer/ProductDetail.vue"), meta: {title: "Chi tiết sản phẩm"}},
            {path: "contact", name: "contact", component: () => import("../components/customer/Contact.vue"), meta: {title: "Liên hệ"}},
            {path: "cart", name: "cart", component: () => import("../components/customer/Cart.vue"), meta: {requiresAuth: true, title: "Giỏ hàng"}},
            {path: "payment", name: "payment", component: () => import("../components/customer/Payment.vue"), meta: {requiresAuth: true, title: "Thanh toán"}},
            {path: "introduce", name: "introduce", component: () => import("../components/customer/Introduce.vue"), meta: {title: "Giới thiệu"}},
            {path: "favorite", name: "favorite", component: () => import("../components/customer/Favorite.vue"), meta: {requiresAuth: true, title: "Sản phẩm yêu thích"}},
            {path: "informationcustomer", name: "informationcustomer", component: () => import("../components/customer/InformationCustomer.vue"), meta: {requiresAuth: true, title: "Thông tin tài khoản"}},
            {path: "invoicecustomer", name: "invoicecustomer", component: () => import("../components/customer/InvoiceCustomer.vue"), meta: {requiresAuth: true, title: "Đơn hàng của tôi"}},
        ],
    },
    {
        path: "/auth",
        component: () => import("../components/auth/Auth.vue"),
        redirect: "/auth/login",
        children: [
            {path: "login", name: "login", component: () => import("../components/auth/Login.vue"), meta: {title: "Đăng nhập"}},
            {path: "register", name: "register", component: () => import("../components/auth/Register.vue"), meta: {title: "Đăng ký"}},
        ],
    },
    {path: "/auth/forgetpassword", name: "forgetpassword", component: () => import("../components/auth/ForgetPassword.vue"), meta: {title: "Quên mật khẩu"}},
    {path: "/auth/verifycode", name: "verifycode", component: () => import("../components/auth/VerifyCode.vue"), meta: {title: "Xác minh OTP"}},
    {path: "/auth/resetpassword", name: "resetpassword", component: () => import("../components/auth/ResetPassword.vue"), meta: {title: "Đặt lại mật khẩu"}},
    {path: "/manage", name: "manage", component: () => import("../components/admin/Manage.vue"), meta: {requiresAuth: true, staffOnly: true, title: "Quản trị"}},
    {path: "/:pathMatch(.*)*", name: "not-found", component: () => import("../components/common/NotFound.vue"), meta: {title: "Không tìm thấy trang"}},
];

const router = createRouter({history: createWebHistory(import.meta.env.BASE_URL), routes, scrollBehavior: () => ({top: 0})});

router.beforeEach((to) => {
    const userStore = useUserStore();
    userStore.loadUserFromLocalStorage();
    const user = userStore.user;
    if (to.meta.requiresAuth && !user) return {path: "/auth/login", query: {redirect: to.fullPath}};
    if (!user) return true;
    const role = user.role || user.roleName || user.roleId;
    const isCustomer = role === "CUSTOMER" || role === 2;
    const isStaff = role === "ADMIN" || role === "EMPLOYEE" || role === 1 || role === 3;
    if (to.path.startsWith("/auth")) return isCustomer ? "/home" : "/manage";
    if (to.meta.staffOnly && !isStaff) return "/home";
    return true;
});

router.afterEach((to) => {
    document.title = `${to.meta.title || "Kix Store"} | Kix Store`;
    const description = to.meta.description || "Kix Store - cửa hàng giày chính hãng, đa dạng phong cách và kích cỡ.";
    let tag = document.querySelector('meta[name="description"]');
    if (!tag) { tag = document.createElement("meta"); tag.name = "description"; document.head.appendChild(tag); }
    tag.content = description;
});

export default router;
