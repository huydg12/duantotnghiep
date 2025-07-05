package com.poly.BE_main.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.repository.AccountRepository;
import com.poly.BE_main.repository.CustomerRepository;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, Long> otpTimestamp = new HashMap<>();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void forgetPassword(String email) {
        if (!accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email không tồn tại");
        }

        String otp = generateOTP();

        otpStore.put(email, otp);
        otpTimestamp.put(email, System.currentTimeMillis());
        emailService.sendOTPEmail(email, otp);
    }

    public void verifyCode(String email, String otp) {
        String saveOTP = otpStore.get(email);
        Long sentTime = otpTimestamp.get(email);

        if (saveOTP == null || sentTime == null) {
            throw new RuntimeException("Không tìm thấy mã OTP");
        }

        long currentTime = System.currentTimeMillis();
        long timeLimit = 5 * 6 * 1000;

        if (currentTime - sentTime > timeLimit) {
            otpStore.remove(email);
            otpTimestamp.remove(email);
            throw new RuntimeException("Mã OTP đã hết hạn vui lòng gửi lại");
        }

        if (!saveOTP.equals(otp)) {
            throw new RuntimeException("Mã OTP không đúng");
        }
    }

    public void resertPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        account.setPassword(newPassword);

        accountRepository.save(account);
    }

    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

}
