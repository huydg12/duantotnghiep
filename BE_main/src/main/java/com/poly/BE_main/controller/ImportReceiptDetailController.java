package com.poly.BE_main.controller;

import com.poly.BE_main.model.ImportReceiptDetail;
import com.poly.BE_main.service.ImportReceiptDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/importReceiptDetail")
public class ImportReceiptDetailController {

    @Autowired
    ImportReceiptDetailService importReceiptDetailService;

    @GetMapping("/show")
    public List<ImportReceiptDetail> findALL() {
        return importReceiptDetailService.findAll();
    }

    @GetMapping("/showById/{importReceiptId}")
    public List<ImportReceiptDetail> findByImportReceiptId(@PathVariable Integer importReceiptId) {
        return importReceiptDetailService.findByImportReceiptId(importReceiptId);
    }

    @PostMapping("create")
    public ImportReceiptDetail create(@RequestBody ImportReceiptDetail i) {
        return importReceiptDetailService.create(i);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        importReceiptDetailService.delete(id);
    }

    @PutMapping("/update/{id}")
    public ImportReceiptDetail update(@PathVariable int id, @RequestBody ImportReceiptDetail i) {
        return importReceiptDetailService.update(id, i);
    }
}
