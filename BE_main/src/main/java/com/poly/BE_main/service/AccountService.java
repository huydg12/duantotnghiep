package com.poly.BE_main.service;

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

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account createAccount(Account account){
        return accountRepository.save(account);
    }

    public Customer createCustomer(Customer customer){
        return customerRepository.save(customer);
    }
}
