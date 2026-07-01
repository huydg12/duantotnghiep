import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.js";

import {createApp} from "vue";
import axios from "axios";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import {createPinia} from "pinia";

axios.defaults.baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";
axios.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

const nativeFetch = window.fetch.bind(window);
window.fetch = (input, init = {}) => {
    const url = typeof input === "string" ? input : input.url;
    const token = localStorage.getItem("accessToken");
    if (token && url.startsWith("http://localhost:8080")) {
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
