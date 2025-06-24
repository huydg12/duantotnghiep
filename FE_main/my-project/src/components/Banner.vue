<script setup>
import { computed } from 'vue';
import { RouterLink } from 'vue-router';

const props = defineProps({
    title: {
        type: String,
        required: true
    },
    breadcrumb: {
        type: String,
        required: true
    },
    backgroundImage: {
        type: String,
        required: true
    },
})

const bannerStyle = computed(() => ({
    backgroundImage: `url('${props.backgroundImage}')`
}))
</script>
<template>
    <div class="banner" :style="bannerStyle">
        <div class="overlay"></div>
        <div class="banner-content">
            <h1>{{ title }}</h1><br>

            <div class="breadcrumb">
                <RouterLink to="/home" class="home-link">Trang chủ</RouterLink> / <span class="current-page">{{ title
                    }}</span>
            </div>
        </div>
    </div>
</template>
<style scoped>
.banner {
    position: relative;
    width: 100%;
    height: 283px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
}

.overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1;
}

.banner-content {
    position: relative;
    z-index: 2;
    padding: 0 20px;
    text-align: center;
}

.banner-content h1 {
    font-size: 50px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 3px;
    display: inline;
    padding-left: 10px;
    margin: 0;
}

.breadcrumb {
    margin-top: 15px;
    font-size: 15px;
    color: #ddd;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 4px;
}

.breadcrumb .home-link {
    color: white;
    text-decoration: none;
    transition: color 0.3s;
}

.breadcrumb .home-link:hover {
    color: red;
}

.breadcrumb .current-page {
    color: red;
}
</style>
