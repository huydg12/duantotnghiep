package com.poly.BE_main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Bill;
import com.poly.BE_main.model.ImportReceipt;
import com.poly.BE_main.service.ImportReceiptService;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/importReceipt")
public class ImportReceiptController {

    @Autowired
    ImportReceiptService importReceiptService;

    @GetMapping("/show")
    public List<ImportReceipt> findAll(){
        return importReceiptService.findALL();
    }

    @PostMapping("/create")
    public ImportReceipt create(@RequestBody ImportReceipt i ){
        return importReceiptService.create(i);
    }

    @DeleteMapping("/delete/{id}")
    public void  delete(@PathVariable Integer id){
        importReceiptService.delete(id);
    }

    @PutMapping("/update/{id}")
    public ImportReceipt update(@PathVariable int id, @RequestBody ImportReceipt i){
        return importReceiptService.update(id, i);
    }
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, Integer> body) {
    int status = body.get("status");
    ImportReceipt updated = importReceiptService.updateStatus(id, status);
    return ResponseEntity.ok(updated);
    }
}