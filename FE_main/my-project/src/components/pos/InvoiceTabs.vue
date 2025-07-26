<template>
    <ul class="nav nav-tabs">
        <li v-for="(invoice, index) in invoices" :key="index" class="nav-item">
            <button class="nav-link" :class="{ active: index === activeIndex }" @click="$emit('switch', index)">
                Hóa đơn {{ index + 1 }}
                <span @click.stop="$emit('remove', index)" class="text-danger ms-2">&times;</span>
            </button>
        </li>
        <li class="nav-item">
            <button class="nav-link" @click="$emit('add')">+</button>
        </li>
    </ul>
</template>

<script setup>
defineProps(['invoices', 'activeIndex'])
defineEmits(['switch', 'add', 'remove'])
</script>
