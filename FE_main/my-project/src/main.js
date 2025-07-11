import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.js";

import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
createApp(App)
  .use(createPinia()) // <-- dòng này rất quan trọng
  .use(router)
  .mount("#app");
