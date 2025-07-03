package com.poly.BE_main.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.poly.BE_main.dto.AccountDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.service.AccountService;



// @CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AccountController {
    @Autowired
    AccountService accountService;

   @PostMapping("/login")
    public ResponseEntity<Account> login(@RequestBody AccountDTO accountDTO) {
        // 1. Lấy thông tin tài khoản từ database dựa vào username
        // Sử dụng phương thức findByUsername đã có trong service của bạn
        var accountOptional = accountService.getAccountByUsername(accountDTO.getUsername());

        // 2. Kiểm tra xem tài khoản có tồn tại và mật khẩu có khớp không
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            // **LƯU Ý QUAN TRỌNG**: Trong thực tế, bạn không bao giờ lưu mật khẩu dạng plain text.
            // Bạn phải mã hóa mật khẩu khi đăng ký và so sánh mật khẩu đã được mã hóa ở đây.
            // Ví dụ: if (passwordEncoder.matches(loginRequest.getPassword(), account.getPassword()))
            if (accountDTO.getPassword().equals(account.getPassword())) {
                // Đăng nhập thành công, trả về thông tin tài khoản và status 200 OK
                return ResponseEntity.ok(account);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
