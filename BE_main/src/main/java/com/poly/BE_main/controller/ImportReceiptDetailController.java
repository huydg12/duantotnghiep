package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.poly.BE_main.model.ImportReceiptDetail;
import com.poly.BE_main.service.ImportReceiptDetailService;

@RestController
@RequestMapping("/importReceiptDetail")
public class ImportReceiptDetailController {
    
    @Autowired
    ImportReceiptDetailService importReceiptDetailService;

    @GetMapping("/show")
    public List<ImportReceiptDetail> findALL(){
        return importReceiptDetailService.findAll();
    }

    @PostMapping("create")
    public ImportReceiptDetail create(@RequestBody ImportReceiptDetail i){
        return importReceiptDetailService.create(i);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        importReceiptDetailService.delete(id);
    }

    @PutMapping("/update/{id}")
    public ImportReceiptDetail update(@PathVariable int id, @RequestBody ImportReceiptDetail i){
        return importReceiptDetailService.update(id, i);
    }
}
