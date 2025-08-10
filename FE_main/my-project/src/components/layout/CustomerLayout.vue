<script setup>
import Header from '@/components/layout/Header.vue';
import Footer from '@/components/layout/Footer.vue';
</script>

<template>
    <Header />
    <div id="app">
        <RouterView />
    </div>
    <Footer />
</template>

<style scoped></style>