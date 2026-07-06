import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.js";

import {createApp} from "vue";
import axios from "axios";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import {createPinia} from "pinia";

axios.defaults.baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";
axios.defaults.timeout = 15000;
axios.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && !window.location.pathname.startsWith("/auth")) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("user");
            router.replace({path: "/auth/login", query: {redirect: window.location.pathname}});
        }
        window.dispatchEvent(new CustomEvent("api:error", {detail: {
            status: error.response?.status,
            message: error.response?.data?.message || (error.code === "ECONNABORTED" ? "Yêu cầu quá thời gian chờ" : "Không thể kết nối máy chủ"),
        }}));
        return Promise.reject(error);
    }
);

const nativeFetch = window.fetch.bind(window);
const apiOrigin = new URL(axios.defaults.baseURL, window.location.origin).origin;
window.fetch = (input, init = {}) => {
    const url = typeof input === "string" ? input : input.url;
    const token = localStorage.getItem("accessToken");
    const target = new URL(url, window.location.origin);
    if (token && target.origin === apiOrigin) {
        const headers = new Headers(init.headers || (typeof input !== "string" ? input.headers : undefined));
        headers.set("Authorization", `Bearer ${token}`);
        init = {...init, headers};
    }
    return nativeFetch(input, init);
};
createApp(App)
    .use(createPinia()) // <-- dòng này rất quan trọng
    .use(router)
    .mount("#app");
