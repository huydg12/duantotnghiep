package com.poly.BE_main.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void forgetPassword(String email) {
        if (!customerRepository.existsByEmail(email)) {
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

    public void resetPassword(String email, String newPassword) {
        // Tìm customer theo email
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Lấy account từ customer
        Account account = customer.getAccount();
        account.setPassword(newPassword);

        accountRepository.save(account);
    }

    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // Account
    private AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setPassword(account.getPassword());
        dto.setRoleId(account.getRoleId());
        dto.setIsActive(account.getIsActive());
        dto.setCreatedDate(account.getCreatedDate());
        return dto;
    }

    private Account toEntity(AccountDTO dto) {
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(dto.getPassword());
        account.setRoleId(dto.getRoleId());
        account.setIsActive(dto.getIsActive());
        return account;
    }

    public List<AccountDTO> findAll() {
        return accountRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AccountDTO create(AccountDTO dto) {
        Account account = toEntity(dto);
        account.setCreatedDate(java.time.LocalDateTime.now());
        return toDTO(accountRepository.save(account));
    }

    public AccountDTO update(int id, AccountDTO dto) {
        return accountRepository.findById(id).map(account -> {
            account.setPassword(dto.getPassword());
            account.setIsActive(dto.getIsActive());
            return toDTO(accountRepository.save(account));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với id: " + id));
    }

    public void deleteById(Integer id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public boolean changePassword(Integer id, ChangePasswordDTO dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account không tồn tại"));

        // kiểm tra mật khẩu hiện tại
        if (!dto.getCurrentPassword().equals(account.getPassword())) {
            return false;
        }

        // kiểm tra confirmPassword
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return false;
        }

        // update password (chưa hash)
        accountRepository.updatePasswordById(id, dto.getNewPassword());
        return true;
    }

}