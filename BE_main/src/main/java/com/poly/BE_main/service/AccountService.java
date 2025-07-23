package com.poly.BE_main.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.repository.AccountRepository;
import com.poly.BE_main.repository.CustomerRepository;
import com.poly.BE_main.repository.EmployeeRepository;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmailService emailService;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, Long> otpTimestamp = new HashMap<>();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Auth
    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Optional<Customer> getCustomerByAccountId(int accountId) {
        return customerRepository.findByAccountId(accountId);
    }

    public Optional<Employee> getEmployeeByAccountId(int accountId) {
        return employeeRepository.getEmployeeByAccountId(accountId);
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

    // Account
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account create(Account account) {
        return accountRepository.save(account);
    }

    public void deleteById(Integer id) {
        accountRepository.deleteById(id);
    }

    public Account update(int id, Account account) {
        return accountRepository.findById(id).map(a -> {
            a.setIsActive(account.getIsActive());
            return accountRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu có id: " + id));
    }
}
