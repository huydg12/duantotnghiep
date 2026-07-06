<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";

const online = ref(navigator.onLine);
const updateNetworkState = () => { online.value = navigator.onLine; };
onMounted(() => {
  window.addEventListener("online", updateNetworkState);
  window.addEventListener("offline", updateNetworkState);
});
onBeforeUnmount(() => {
  window.removeEventListener("online", updateNetworkState);
  window.removeEventListener("offline", updateNetworkState);
});
</script>

<template>
  <div id="app">
    <div v-if="!online" class="offline-banner" role="status">
      Bạn đang ngoại tuyến. Một số thao tác sẽ hoạt động lại khi có kết nối mạng.
    </div>
    <RouterView v-slot="{Component, route}">
      <Suspense>
        <component :is="Component" :key="route.fullPath"/>
        <template #fallback>
          <div class="route-loading" role="status" aria-live="polite">
            <span class="spinner-border text-danger" aria-hidden="true"></span>
            <span>Đang tải trang…</span>
          </div>
        </template>
      </Suspense>
    </RouterView>
  </div>
</template>
