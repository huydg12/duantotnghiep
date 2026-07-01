package com.poly.BE_main.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.BE_main.dto.AccountDTO;
import com.poly.BE_main.dto.ChangePasswordDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.repository.AccountRepository;
import com.poly.BE_main.repository.CustomerRepository;
import com.poly.BE_main.repository.EmployeeRepository;

@Service
public class AccountService {
    private static final long OTP_TTL_MS = 5 * 60 * 1000L;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Long> otpTimestamp = new ConcurrentHashMap<>();
    private final Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
            EmployeeRepository employeeRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Account> getAccountByUsername(String username) { return accountRepository.findByUsername(username); }
    public Optional<Customer> getCustomerByAccountId(int accountId) { return customerRepository.findByAccountId(accountId); }
    public Optional<Employee> getEmployeeByAccountId(int accountId) { return employeeRepository.getEmployeeByAccountId(accountId); }
    public Account createAccount(Account account) { return accountRepository.save(account); }
    public Customer createCustomer(Customer customer) { return customerRepository.save(customer); }
    public Employee createEmployee(Employee employee) { return employeeRepository.save(employee); }
    public String encodePassword(String rawPassword) { return passwordEncoder.encode(rawPassword); }

    @Transactional
    public boolean passwordMatchesAndUpgrade(Account account, String rawPassword) {
        boolean bcrypt = account.getPassword() != null && account.getPassword().startsWith("$2");
        boolean matches = bcrypt ? passwordEncoder.matches(rawPassword, account.getPassword())
                : rawPassword.equals(account.getPassword());
        if (matches && !bcrypt) {
            account.setPassword(passwordEncoder.encode(rawPassword));
            accountRepository.save(account);
        }
        return matches;
    }

    public void forgetPassword(String email) {
        if (!customerRepository.existsByEmail(email)) return;
        String otp = String.valueOf(secureRandom.nextInt(900000) + 100000);
        otpStore.put(email, otp);
        otpTimestamp.put(email, System.currentTimeMillis());
        otpAttempts.put(email, 0);
        emailService.sendOTPEmail(email, otp);
    }

    public void verifyCode(String email, String otp) {
        String savedOtp = otpStore.get(email);
        Long sentAt = otpTimestamp.get(email);
        if (savedOtp == null || sentAt == null || System.currentTimeMillis() - sentAt > OTP_TTL_MS) {
            clearOtp(email);
            throw new IllegalArgumentException("OTP is invalid or expired");
        }
        if (otpAttempts.merge(email, 1, Integer::sum) > MAX_OTP_ATTEMPTS) {
            clearOtp(email);
            throw new IllegalArgumentException("Too many invalid OTP attempts");
        }
        if (!savedOtp.equals(otp)) throw new IllegalArgumentException("OTP is invalid or expired");
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        verifyCode(email, otp);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("OTP is invalid or expired"));
        Account account = customer.getAccount();
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        clearOtp(email);
    }

    private void clearOtp(String email) {
        otpStore.remove(email); otpTimestamp.remove(email); otpAttempts.remove(email);
    }

    private AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId()); dto.setUsername(account.getUsername());
        dto.setPassword(null); dto.setRoleId(account.getRoleId());
        dto.setIsActive(account.getIsActive()); dto.setCreatedDate(account.getCreatedDate());
        return dto;
    }

    private Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setRoleId(dto.getRoleId()); account.setIsActive(dto.getIsActive());
        return account;
    }

    public List<AccountDTO> findAll() { return accountRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList()); }
    public AccountDTO create(AccountDTO dto) {
        Account account = toEntity(dto); account.setCreatedDate(java.time.LocalDateTime.now());
        return toDTO(accountRepository.save(account));
    }
    public AccountDTO update(int id, AccountDTO dto) {
        return accountRepository.findById(id).map(account -> {
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) account.setPassword(passwordEncoder.encode(dto.getPassword()));
            account.setIsActive(dto.getIsActive()); return toDTO(accountRepository.save(account));
        }).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }
    public AccountDTO updateStatus(int id, AccountDTO dto) {
        return accountRepository.findById(id).map(account -> {
            account.setIsActive(!account.getIsActive()); return toDTO(accountRepository.save(account));
        }).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }
    public AccountDTO updatePassword(int id, AccountDTO dto) {
        return accountRepository.findById(id).map(account -> {
            account.setPassword(passwordEncoder.encode("ChangeMe@123")); return toDTO(accountRepository.save(account));
        }).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }
    public void deleteById(Integer id) { accountRepository.deleteById(id); }

    @Transactional
    public boolean changePassword(Integer id, ChangePasswordDTO dto) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!passwordMatchesAndUpgrade(account, dto.getCurrentPassword())) return false;
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) return false;
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account); return true;
    }
}
