package com.poly.BE_main.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.poly.BE_main.dto.BillDTO;
import com.poly.BE_main.dto.InvoiceCustomerDTO;
import com.poly.BE_main.model.Bill;
import com.poly.BE_main.service.BillService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    BillService billService;

    @GetMapping("/show")
    public List<Bill> findALl() {
        return billService.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Bill> create(@RequestBody Bill bill) {
        // Gán Bill vào từng BillDetail để duy trì quan hệ hai chiều
        if (bill.getBillDetails() != null) {
            bill.getBillDetails().forEach(detail -> detail.setBill(bill));
        }

        Bill savedBill = billService.create(bill);
        return ResponseEntity.ok(savedBill);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        billService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Bill update(@PathVariable int id, @RequestBody Bill i) {
        return billService.update(id, i);
    }

    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        int status = body.get("status");
        Bill updated = billService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }   

    @PutMapping("/updateEmployee/{id}")
    public Bill updateEmployee(@PathVariable int id, @RequestBody Bill i) {
        return billService.updateEmployee(id, i);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBill(@RequestBody BillDTO dto) {
        try {
            Bill createdBill = billService.createBill(dto);
            return ResponseEntity.ok(createdBill);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo hóa đơn: " + e.getMessage());
        }
    }

    @GetMapping("/invoicecustomer/{customerId}")
    public ResponseEntity<List<InvoiceCustomerDTO>> getInvoiceItems(@PathVariable Integer customerId) {
        List<InvoiceCustomerDTO> items = billService.getInvoicesByCustomerId(customerId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBillById(@PathVariable int billId) {
        Optional<Bill> bill = billService.findById(billId);

        if (bill.isPresent()) {
            return ResponseEntity.ok(bill.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Trả về 404 nếu không tìm thấy
        }
    }

    @PutMapping("/updateSubTotal/{billId}")
    public ResponseEntity<String> updateSubTotal(@PathVariable int billId,
            @RequestBody Map<String, BigDecimal> requestBody) {
        try {
            BigDecimal subTotal = requestBody.get("subTotal");
            billService.updateSubTotal(billId, subTotal);
            return ResponseEntity.ok("SubTotal đã được cập nhật thành công.");
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để dễ dàng chẩn đoán
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật subTotal");
        }
    }
    @PutMapping("/updateStatusNote/{id}")
    public ResponseEntity<Bill> updateStatusNote(
            @PathVariable int id,
            @RequestBody Bill bill
    ) {
        Bill updated = billService.updateStatusNote(id, bill);
        return ResponseEntity.ok(updated);
    }
    @PutMapping("/updateAddressByBill/{id}")
    public ResponseEntity<Bill> updateAddressByBill(
            @PathVariable int id,
            @RequestBody Bill bill
    ) {
        Bill updated = billService.updateAddressByBill(id, bill);
        return ResponseEntity.ok(updated);
    }
        @PutMapping("/updateBill/{id}")
    public ResponseEntity<Bill> updateBill(
            @PathVariable int id,
            @RequestBody Bill bill
    ) {
        Bill updated = billService.updateBill(id, bill);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateBillStatus4/{id}")
    public ResponseEntity<Bill> updateStatus4(
            @PathVariable int id,
            @RequestBody Bill bill
    ) {
        Bill updated = billService.updateStatusNote4(id, bill);
        return ResponseEntity.ok(updated);
    }

}