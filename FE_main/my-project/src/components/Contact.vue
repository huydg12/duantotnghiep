<script setup>
import Banner from './Banner.vue';
</script>
<template>
  <Banner title="Thông tin liên hệ" breadcrumb='' backgroundImage="https://i.postimg.cc/py5ywZCZ/kv-basas-mobile-Banner-4-2019.jpg" />
  <main class="container-xl py-5">
    <div class="row g-5 align-items-start">
      <!-- Google Map -->
      <div class="col-lg-6 map-container">
        <iframe
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3729.214795239776!2d106.64883387488325!3d20.8230310807753!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x314a70bd730b201d%3A0xab7b4b5c986bd11!2zODY3IMSQLiBUcsaw4budbmcgQ2hpbmgsIEzDo20gSMOgLCBLaeG6v24gQW4sIEjhuqNpIFBow7JuZywgVmnhu4d0IE5hbQ!5e0!3m2!1svi!2s!4v1750154235243!5m2!1svi!2s"
          width="600" height="450" style="border: 0" allowfullscreen="" loading="lazy"
          referrerpolicy="no-referrer-when-downgrade"></iframe>
      </div>

      <!-- Contact Form -->
      <div class="col-lg-6">
        <div class="contact-form-wrapper">
          <h2 class="contact-title h3">Liên hệ chúng tôi</h2>
          <p class="mb-4 text-muted">
            Vui lòng điền thông tin bên dưới, chúng tôi sẽ liên lạc với bạn
            trong thời gian sớm nhất.
          </p>

          <form class="needs-validation" novalidate>
            <div class="mb-3">
              <label for="fullName" class="form-label fw-medium">Họ và tên*</label>
              <input type="text" class="form-control" id="fullName" required placeholder="Nhập họ và tên" />
              <div class="invalid-feedback">Vui lòng nhập họ và tên.</div>
            </div>

            <div class="mb-3">
              <label for="email" class="form-label fw-medium">Email*</label>
              <input type="email" class="form-control" id="email" required placeholder="Nhập địa chỉ Email" />
              <div class="invalid-feedback">
                Vui lòng nhập một địa chỉ email hợp lệ.
              </div>
            </div>

            <div class="mb-3">
              <label for="phone" class="form-label fw-medium">Điện thoại*</label>
              <input type="tel" class="form-control" id="phone" required placeholder="Nhập số điện thoại" />
              <div class="invalid-feedback">Vui lòng nhập số điện thoại.</div>
            </div>

            <div class="mb-3">
              <label for="content" class="form-label fw-medium">Nội dung*</label>
              <textarea class="form-control" id="content" rows="5" required
                placeholder="Nội dung bạn muốn liên hệ..."></textarea>
              <div class="invalid-feedback">
                Vui lòng nhập nội dung liên hệ.
              </div>
            </div>

            <button type="submit" class="btn btn-primary px-4 py-2">
              Gửi liên hệ ngay
            </button>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<style>
:root {
  --color-primary: #ef4444;
  --color-primary-dark: #dc2626;
  --color-dark: #000;
  --color-light: #fff;
  --color-secondary-text: #6c757d;
  --font-family-main: "Be Vietnam Pro", sans-serif;
}

body {
  font-family: var(--font-family-main);
  background-color: #f8f9fa;
}

.contact-form-wrapper {
  background-color: #fff;
  padding: 2.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1);
}

.contact-title {
  font-weight: 700;
  text-transform: uppercase;
  color: var(--color-dark);
}

.map-container iframe {
  border-radius: 0.5rem;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

.btn-primary:hover {
  background-color: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
}

.form-control:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 0.25rem rgba(239, 68, 68, 0.25);
}
</style>
