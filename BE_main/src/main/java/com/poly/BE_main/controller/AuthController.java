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

import com.poly.BE_main.dto.AuthDTO;
import com.poly.BE_main.dto.ForgetPasswordDTO;
import com.poly.BE_main.dto.LoginDTO;
import com.poly.BE_main.dto.RegisterDTO;
import com.poly.BE_main.dto.ResetPasswordDTO;
import com.poly.BE_main.dto.VerifyCodeDTO;
import com.poly.BE_main.dto.LoginResponseDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.service.AccountService;
import com.poly.BE_main.utils.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AuthController(AccountService accountService, JwtUtil jwtUtil) {
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

        AuthDTO accountDTO = new AuthDTO(account);
        if (account.getRoleId() == 2) {
            Optional<Customer> customerOptional = accountService.getCustomerByAccountId(account.getId());
            customerOptional.ifPresent(customer -> accountDTO.setCustomerId(customer.getId()));
        }

        if (account.getRoleId() == 1) {
            Optional<Employee> employeeOptional = accountService.getEmployeeByAccountId(account.getId());
            employeeOptional.ifPresent(employee -> accountDTO.setEmployeeId(employee.getId()));
        }

        LoginResponseDTO response = new LoginResponseDTO(token, accountDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> login(@RequestBody RegisterDTO registerDTO) {
        Account account = new Account();
        account.setUsername(registerDTO.getUsername());
        account.setPassword(registerDTO.getPassword());
        account.setIsActive(true);
        account.setRoleId(2);

        account = accountService.createAccount(account);

        Customer customer = new Customer();
        customer.setFullName(registerDTO.getFullname());
        customer.setNumberPhone(registerDTO.getPhone());
        customer.setEmail(registerDTO.getEmail());
        customer.setGender("Nam");
        customer.setStatus(1);
        customer.setAccount(account);

        customer = accountService.createCustomer(customer); // Gán lại để lấy ID

        return ResponseEntity.ok(customer);
    }

    @PostMapping("/forgetpassword")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordDTO forgetPasswordDTO) {
        accountService.forgetPassword(forgetPasswordDTO.getEmail());
        return ResponseEntity.ok("OTP đã được gửi");
    }

    @PostMapping("/verifycode")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeDTO verifyCodeDTO) {
        accountService.verifyCode(verifyCodeDTO.getEmail(), verifyCodeDTO.getOtpCode());
        return ResponseEntity.ok("OTP hợp lệ");
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resertPasswordDTO) {
        accountService.resetPassword(resertPasswordDTO.getEmail(), resertPasswordDTO.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @GetMapping("/show")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }

}
