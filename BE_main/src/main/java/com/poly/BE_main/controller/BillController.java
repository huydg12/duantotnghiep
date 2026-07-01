package com.poly.BE_main.controller;

import com.poly.BE_main.dto.BillDTO;
import com.poly.BE_main.dto.InvoiceCustomerDTO;
import com.poly.BE_main.model.Bill;
import com.poly.BE_main.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        throw new ResponseStatusException(HttpStatus.GONE, "Use POST /bill/create");
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
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, Object> body,
                                          Authentication authentication) {
        int status = ((Number) body.get("status")).intValue();
        String note = body.get("note") == null ? null : body.get("note").toString();
        Bill updated = billService.transitionStatus(id, status, authentication.getName(), note);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{billId}/status-history")
    @PreAuthorize("@ownership.ownsBill(authentication, #billId)")
    public ResponseEntity<?> statusHistory(@PathVariable Integer billId) {
        return ResponseEntity.ok(billService.getStatusHistory(billId));
    }

    @PutMapping("/{billId}/cancel")
    @PreAuthorize("@ownership.ownsBill(authentication, #billId)")
    public ResponseEntity<?> cancel(@PathVariable Integer billId, @RequestBody(required = false) Map<String, String> body,
                                    Authentication authentication) {
        String note = body == null ? "Khách hàng hủy đơn" : body.getOrDefault("note", "Khách hàng hủy đơn");
        return ResponseEntity.ok(billService.transitionStatus(billId, 5, authentication.getName(), note));
    }

    @PutMapping("/updateEmployee/{id}")
    public Bill updateEmployee(@PathVariable int id, @RequestBody Bill i) {
        return billService.updateEmployee(id, i);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBill(@Valid @RequestBody BillDTO dto, Authentication authentication) {
        return ResponseEntity.ok(billService.createBill(dto, authentication.getName()));
    }

    @GetMapping("/invoicecustomer/{customerId}")
    @PreAuthorize("@ownership.ownsCustomer(authentication, #customerId)")
    public ResponseEntity<List<InvoiceCustomerDTO>> getInvoiceItems(@PathVariable Integer customerId) {
        List<InvoiceCustomerDTO> items = billService.getInvoicesByCustomerId(customerId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{billId}")
    @PreAuthorize("@ownership.ownsBill(authentication, #billId)")
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
            @RequestBody Bill bill) {
        Bill updated = billService.updateStatusNote(id, bill);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateAddressByBill/{id}")
    public ResponseEntity<Bill> updateAddressByBill(
            @PathVariable int id,
            @RequestBody Bill bill) {
        Bill updated = billService.updateAddressByBill(id, bill);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateBill/{id}")
    public ResponseEntity<Bill> updateBill(
            @PathVariable int id,
            @RequestBody Bill bill) {
        Bill updated = billService.updateBill(id, bill);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateBillStatus4/{id}")
    public ResponseEntity<Bill> updateStatus4(
            @PathVariable int id,
            @RequestBody Bill bill) {
        Bill updated = billService.updateStatusNote4(id, bill);
        return ResponseEntity.ok(updated);
    }

}
