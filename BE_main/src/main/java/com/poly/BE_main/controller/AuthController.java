package com.poly.BE_main.controller;

import com.poly.BE_main.dto.*;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.service.AccountService;
import com.poly.BE_main.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AuthController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {
        Optional<Account> found = accountService.getAccountByUsername(request.getUsername());
        if (found.isEmpty() || !accountService.passwordMatchesAndUpgrade(found.get(), request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        Account account = found.get();
        if (!account.getIsActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is inactive");
        }
        AuthDTO user = new AuthDTO(account);
        if (account.getRoleId() == 2) {
            accountService.getCustomerByAccountId(account.getId()).ifPresent(c -> user.setCustomerId(c.getId()));
        } else {
            accountService.getEmployeeByAccountId(account.getId()).ifPresent(e -> user.setEmployeeId(e.getId()));
        }
        String token = jwtUtil.generateToken(account.getUsername(), account.getRoleId(), account.getId());
        return ResponseEntity.ok(new LoginResponseDTO(token, user));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO request) {
        if (accountService.getAccountByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(accountService.encodePassword(request.getPassword()));
        account.setIsActive(true);
        account.setRoleId(2);
        account = accountService.createAccount(account);

        Customer customer = new Customer();
        customer.setFullName(request.getFullname());
        customer.setNumberPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setGender(true);
        customer.setActive(true);
        customer.setAccount(account);
        return ResponseEntity.ok(accountService.createCustomer(customer));
    }

    @PostMapping("/forgetpassword")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPasswordDTO request) {
        accountService.forgetPassword(request.getEmail());
        return ResponseEntity.ok("If the email exists, an OTP has been sent");
    }

    @PostMapping("/verifycode")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeDTO request) {
        accountService.verifyCode(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok("OTP is valid");
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        accountService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
}
