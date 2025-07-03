package com.poly.BE_main.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Account;
import com.poly.BE_main.repository.AccountRepository;

@Service
public class AccountService {
@Autowired
AccountRepository accountRepository;

public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}
