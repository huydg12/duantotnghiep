package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.AccountDTO;
import com.poly.BE_main.dto.ChangePasswordDTO;
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

    @PutMapping("/updateStatus/{id}")
    public AccountDTO updateStatus(@PathVariable Integer id, @RequestBody AccountDTO accountDTO) {
        return accountService.updateStatus(id, accountDTO);
    }

    @PutMapping("/changePassword/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer id,
            @RequestBody ChangePasswordDTO dto) {
        boolean success = accountService.changePassword(id, dto);

        if (!success) {
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu mới không trùng với confirm password");
            }
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng");
        }

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

}
