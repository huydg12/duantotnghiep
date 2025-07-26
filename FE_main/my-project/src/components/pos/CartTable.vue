<template>
  <table class="table table-bordered">
    <thead>
      <tr>
        <th>Tên</th>
        <th>Màu</th>
        <th>Size</th>
        <th>Giá</th>
        <th>Số lượng</th>
        <th>Tổng</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(item, i) in items" :key="i">
        <td>{{ item.productName }}</td>
        <td>{{ item.color }}</td>
        <td>{{ item.size }}</td>
        <td>{{ item.price.toLocaleString() }}</td>
        <td>
          <input type="number" class="form-control" v-model.number="item.quantity" min="1"
            @input="$emit('changeQuantity', i, item.quantity)" />
        </td>
        <td>{{ (item.price * item.quantity).toLocaleString() }}</td>
        <td><button class="btn btn-danger btn-sm" @click="$emit('removeItem', i)">X</button></td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
defineProps(['items'])
defineEmits(['removeItem', 'changeQuantity'])
</script>
