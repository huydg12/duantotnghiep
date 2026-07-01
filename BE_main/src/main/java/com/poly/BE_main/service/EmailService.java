package com.poly.BE_main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOTPEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận đặt lại mật khẩu");
        message.setText("Mã xác nhận của bạn là: " + otp + "\n\nMã có hiệu lực trong 5 phút.");

        mailSender.send(message);
    }

    public void sendOrderConfirmation(String toEmail, String billCode, BigDecimal total) {
        if (toEmail == null || toEmail.isBlank()) return;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận đơn hàng " + billCode);
        message.setText("Đơn hàng " + billCode + " đã được tạo thành công. Tổng thanh toán: "
                + NumberFormat.getCurrencyInstance(Locale.of("vi", "VN")).format(total)
                + ". Chúng tôi sẽ thông báo khi trạng thái đơn thay đổi.");
        mailSender.send(message);
    }
}
