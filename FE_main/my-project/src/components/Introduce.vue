<script setup>
import Banner from './Banner.vue';
</script>
<template>
    <Banner title="Giới thiệu" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
    <!-- Nội dung giới thiệu -->
    <main class="container py-5">
        <div class="content-box">
            <h1 class="page-title mb-4">Giới thiệu</h1>
            <p class="mb-4 lh-lg">
                Giày là một trong những phụ kiện không thể thiếu góp phần tạo nên phong cách thời trang của mỗi người.
                Chính vì điều đó Kix Store mong muốn đem đến những mẫu giày mới nhất được cập nhật thường xuyên giúp các
                bạn
                dễ dàng hơn trong việc lựa chọn và cập nhật những style thịnh hành của thời trang giày dép trong nước và
                thế giới.
            </p>
            <p class="lh-lg">
                Kix Store có đa dạng các loại giày đến từ các hãng nổi tiếng sẵn sàng phục vụ quý khách nhanh chóng
                thông
                qua việc bán tại cửa hàng
                tại <strong>867 Trường Chinh, Kiến An, Hải Phòng</strong> và đặt hàng online trên website.
            </p>
        </div>
    </main>

</template>

<style>
body {
    background-color: #f8f9fa;
    color: #212529;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.content-box {
    background-color: #ffffff;
    padding: 3rem;
    border-radius: 0.5rem;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1);
}

.page-title {
    font-size: 2rem;
    font-weight: s;

    border-bottom: 2px solid #dee2e6;
    padding-bottom: 1rem;
}
</style>