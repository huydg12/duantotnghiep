<script setup>
import { onMounted } from "vue";
import { useUserStore } from "@/stores/userStore";
import { useRouter } from "vue-router";

const userStore = useUserStore();
const router = useRouter();

onMounted(() => {
  userStore.loadUserFromLocalStorage();
  if (userStore.user) {
    if (userStore.user.roleId === 1 || userStore.user.roleId === 3) {
      router.replace("/manage");
    }
  }
});
</script>

<template>
  <div id="app">
    <RouterView />
  </div>
</template>

<style scoped></style>
