package com.poly.BE_main.controller;

import com.poly.BE_main.model.ImportReceipt;
import com.poly.BE_main.service.ImportReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/importReceipt")
public class ImportReceiptController {

    @Autowired
    ImportReceiptService importReceiptService;

    @GetMapping("/show")
    public List<ImportReceipt> findAll() {
        return importReceiptService.findAll();
    }

    @PostMapping("/create")
    public ImportReceipt create(@RequestBody ImportReceipt i) {
        return importReceiptService.create(i);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        importReceiptService.delete(id);
    }

    @PutMapping("/update/{id}")
    public ImportReceipt update(@PathVariable int id, @RequestBody ImportReceipt i) {
        return importReceiptService.update(id, i);
    }

    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        int status = body.get("status");
        ImportReceipt updated = importReceiptService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}