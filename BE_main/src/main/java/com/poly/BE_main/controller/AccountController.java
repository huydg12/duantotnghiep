package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.AccountDTO;
import com.poly.BE_main.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    AccountService accountService;

    @GetMapping("/show")
    public List<AccountDTO> findAll() {
        return accountService.findAll();
    }

    @PostMapping("/add")
    public AccountDTO add(@RequestBody AccountDTO accountDTO) {
        System.out.println("Thêm thành công");
        return accountService.create(accountDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        accountService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public AccountDTO update(@PathVariable Integer id, @RequestBody AccountDTO accountDTO) {
        return accountService.update(id, accountDTO);
    }

}
