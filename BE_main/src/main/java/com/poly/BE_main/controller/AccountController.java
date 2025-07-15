package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Account;
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
    public List<Account> findAll(){
        return accountService.findAll();
    }
    
    @PostMapping("/add")
    public Account add(@RequestBody Account account) {
        System.out.println("Thêm thành công");
        return accountService.create(account);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        accountService.deleteById(id);
    }
    
    @PutMapping("/update/{id}")
    public Account update(@PathVariable Integer id, @RequestBody Account account) {
        return accountService.update(id, account);
    }
    
}
