package com.poly.BE_main.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.poly.BE_main.dto.AccountDTO;
import com.poly.BE_main.dto.LoginDTO;
import com.poly.BE_main.dto.RegisterDTO;
import com.poly.BE_main.dto.LoginResponseDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.service.AccountService;
import com.poly.BE_main.utils.JwtUtil;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AccountController {
    @Autowired
    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        Optional<Account> accountOptional = accountService.getAccountByUsername(loginDTO.getUsername());

        if (accountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản không tồn tại");
        }
        Account account = accountOptional.get();
        if (!loginDTO.getPassword().equals(account.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
        }

        // Tạo JWT
        String token = jwtUtil.generateToken(account.getUsername());

        AccountDTO accountDTO = new AccountDTO(account);
        LoginResponseDTO response = new LoginResponseDTO(token, accountDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> login(@RequestBody RegisterDTO registerDTO) {
        Account account = new Account();
        account.setPhone(registerDTO.getPhone());
        account.setEmail(registerDTO.getEmail());
        account.setUsername(registerDTO.getUsername());
        account.setPassword(registerDTO.getPassword());
        account.setActive(true);
        account.setRoleId(3);

        account = accountService.createAccount(account);

        Customer customer = new Customer();
        customer.setFullName(registerDTO.getFullname());
        customer.setNumberPhone(registerDTO.getPhone());
        customer.setEmail(registerDTO.getEmail());
        customer.setAccount(account);

        accountService.createCustomer(customer);

        return ResponseEntity.ok("Đăng ký thành công");
    }

}
