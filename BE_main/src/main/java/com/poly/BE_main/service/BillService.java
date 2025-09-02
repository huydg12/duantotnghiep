package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.poly.BE_main.dto.BillDTO;
import com.poly.BE_main.dto.BillDetailDTO;
import com.poly.BE_main.dto.InvoiceCustomerDTO;
import com.poly.BE_main.dto.InvoiceItemCustomerDTO;
import com.poly.BE_main.model.Bill;
import com.poly.BE_main.model.BillDetail;
import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.repository.BillDetailRepository;
import com.poly.BE_main.repository.BillRepository;
import com.poly.BE_main.repository.InventoryRepository;

@Service
public class BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    BillDetailRepository billDetailRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Bill create(Bill bill) {
        // Gán bill cho từng billDetail (quan hệ 2 chiều)
        for (BillDetail detail : bill.getBillDetails()) {
            detail.setBill(bill);
        }

        // Lưu cả bill và các billDetail liên quan
        return billRepository.save(bill);
    }

    public void delete(Integer id) {
        billRepository.deleteById(id);
    }

    public Bill update(int id, Bill bUpdate) {
        return billRepository.findById(id).map(i -> {
            i.setCustomerId(bUpdate.getCustomerId());
            i.setEmployeeId(bUpdate.getEmployeeId());
            i.setPtttId(bUpdate.getPtttId());
            i.setCode(bUpdate.getCode());
            i.setBillType(bUpdate.getBillType());
            i.setStatus(bUpdate.getStatus());
            i.setCreatedBy(bUpdate.getCreatedBy());
            i.setCreatedDate(bUpdate.getCreatedDate());
            i.setShippingDate(bUpdate.getShippingDate());
            i.setDateOfPayment(bUpdate.getDateOfPayment());
            i.setRecipientName(bUpdate.getRecipientName());
            i.setRecipientPhoneNumber(bUpdate.getRecipientPhoneNumber());
            i.setReceiverAddress(bUpdate.getReceiverAddress());
            i.setAddressMethod(bUpdate.getAddressMethod());
            i.setEstimatedDeliveryDate(bUpdate.getEstimatedDeliveryDate());
            i.setModifiedBy(bUpdate.getModifiedBy());
            i.setModifiedDate(bUpdate.getModifiedDate());
            i.setNote(bUpdate.getNote());
            i.setStatusPayment(bUpdate.getStatusPayment());
            i.setSubTotal(bUpdate.getSubTotal());
            i.setDiscountAmount(bUpdate.getDiscountAmount());
            i.setShippingFee(bUpdate.getShippingFee());
            i.setGrandTotal(bUpdate.getGrandTotal());
            return billRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn có id:" + id));
    }

    public Bill updateStatus(Integer id, Integer status) {
        return billRepository.findById(id).map(bill -> {
            bill.setStatus(status);
            return billRepository.save(bill);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));
    }

    public Bill updateEmployee(int id, Bill bUpdate) {
        return billRepository.findById(id).map(i -> {
            i.setEmployeeId(bUpdate.getEmployeeId());
            return billRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn có id:" + id));
    }

    @Transactional
    public Bill createBill(BillDTO dto) {
        if (dto.getBillDetails() == null || dto.getBillDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hóa đơn không có sản phẩm.");
        }

        // 1) Trừ kho + tính subTotal
        BigDecimal subTotal = BigDecimal.ZERO;

        for (BillDetailDTO d : dto.getBillDetails()) {
            if (d.getProductDetailId() == null || d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dòng hóa đơn không hợp lệ.");
            }

            // Khoá ghi tồn kho theo productDetailId
            Inventory inv = inventoryRepository.lockByProductDetailId(d.getProductDetailId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Không thấy tồn kho cho sản phẩm: " + d.getProductDetailId()));

            if (inv.getQuantity() < d.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Sản phẩm " + d.getProductDetailId() + " không đủ hàng. Còn: " + inv.getQuantity());
            }

            // Trừ kho
            inv.setQuantity(inv.getQuantity() - d.getQuantity());

            // Cộng subTotal
            BigDecimal price = d.getPrice() == null ? BigDecimal.ZERO : d.getPrice();
            subTotal = subTotal.add(price.multiply(BigDecimal.valueOf(d.getQuantity())));
        }

        // 2) Lấy discount/shipping từ DTO (nếu có) và tính grandTotal
        BigDecimal discount = dto.getDiscountAmount() == null ? BigDecimal.ZERO : dto.getDiscountAmount();
        BigDecimal shipping = dto.getShippingFee() == null ? BigDecimal.ZERO : dto.getShippingFee();
        BigDecimal grandTotal = subTotal.subtract(discount).add(shipping);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0)
            grandTotal = BigDecimal.ZERO;

        // 3) Tạo Bill và gán đầy đủ các trường
        Bill bill = new Bill();
        bill.setCustomerId(dto.getCustomerId());
        bill.setEmployeeId(dto.getEmployeeId());
        bill.setPtttId(dto.getPtttId());
        bill.setPromotionId(dto.getPromotionId());
        bill.setCode(dto.getBillCode());
        bill.setBillType(dto.getBillType());
        bill.setStatus(dto.getStatus());
        bill.setRecipientName(dto.getRecipientName());
        bill.setRecipientPhoneNumber(dto.getRecipientPhoneNumber());
        bill.setAddressMethod(dto.getAddressMethod());
        bill.setStatusPayment(dto.getStatusPayment());
        bill.setSubTotal(subTotal); // ✅ dùng số liệu server tính
        bill.setDiscountAmount(discount);
        bill.setShippingFee(shipping);
        bill.setGrandTotal(grandTotal);
        bill.setCreatedDate(LocalDate.now());

        // 4) Lưu Bill để có ID
        Bill savedBill = billRepository.save(bill);

        // 5) Map và lưu BillDetail
        List<BillDetail> details = dto.getBillDetails().stream().map(detailDto -> {
            BillDetail detail = new BillDetail();
            detail.setBill(savedBill); // Gán quan hệ
            detail.setProductDetailId(detailDto.getProductDetailId());
            detail.setQuantity(detailDto.getQuantity());
            detail.setPrice(detailDto.getPrice());
            detail.setProductName(detailDto.getProductName());
            detail.setColor(detailDto.getColor());
            detail.setSize(detailDto.getSize());
            return detail;
        }).collect(Collectors.toList());

        billDetailRepository.saveAll(details);

        return savedBill;
    }

    private String convertStatusToString(int status) {
        switch (status) {
            case 1: // Giả sử 0 là "Chờ xác nhận"
                return "Chờ xác nhận";
            case 2:
                return "Đã xác nhận";
            case 3:
                return "Đang giao";
            case 4:
                return "Hoàn Thành";
            case 5:
                return "Đã hủy";
            case 6:
                return "Trả hàng/Hoàn tiền";
            default:
                return "Tất cả"; // Hoặc "Không xác định"
        }
    }

    public List<InvoiceCustomerDTO> getInvoicesByCustomerId(Integer customerId) {
        List<InvoiceCustomerDTO> result = new ArrayList<>();

        List<Bill> bills = billRepository.findByCustomerId(customerId);
        for (Bill bill : bills) {
            List<InvoiceItemCustomerDTO> items = billRepository.findInvoiceItemsByCustomerIdAndBillId(customerId,
                    bill.getId());
            InvoiceCustomerDTO dto = new InvoiceCustomerDTO(
                    bill.getId(),
                    bill.getCode(),
                    bill.getRecipientName(),
                    bill.getRecipientPhoneNumber(),
                    bill.getReceiverAddress(),
                    bill.getCreatedDate(),
                    convertStatusToString(bill.getStatus()),
                    bill.getGrandTotal(),
                    items);
            result.add(dto);
        }
        return result;
    }

    // Phương thức tìm hóa đơn theo ID
    public Optional<Bill> findById(int billId) {
        return billRepository.findById(billId); // Trả về Optional để kiểm tra sự tồn tại
    }

    // Phương thức để cập nhật sub_total sau khi thay đổi số lượng
    public void updateSubTotal(int billId, BigDecimal subTotal) {
        Optional<Bill> billOptional = billRepository.findById(billId);

        if (billOptional.isPresent()) {
            Bill bill = billOptional.get();
            bill.setSubTotal(subTotal); // Cập nhật subTotal

            BigDecimal shippingFee = bill.getShippingFee();
            BigDecimal discountAmount = bill.getDiscountAmount();
            BigDecimal updatedGrandTotal = subTotal.add(shippingFee).subtract(discountAmount);

            bill.setGrandTotal(updatedGrandTotal);

            billRepository.save(bill); // Lưu hóa đơn với subTotal đã được cập nhật
        } else {
            throw new RuntimeException("Không tìm thấy hóa đơn có id: " + billId);
        }
    }
    public Bill updateStatusNote(int id, Bill b) {
        return billRepository.findById(id).map(bUpdate -> {
            bUpdate.setStatus(b.getStatus());
            bUpdate.setNote(b.getNote());
            return billRepository.save(bUpdate);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));
    }

        public Bill updateStatusNote4(int id, Bill b) {
        return billRepository.findById(id).map(bUpdate -> {
            bUpdate.setStatus(b.getStatus());
            bUpdate.setStatusPayment(b.getStatusPayment());
            return billRepository.save(bUpdate);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));
        }
        public Bill updateAddressByBill(int id, Bill b) {
        return billRepository.findById(id).map(bUpdate -> {
            bUpdate.setRecipientName(b.getRecipientName());
            bUpdate.setRecipientPhoneNumber(b.getRecipientPhoneNumber());
            bUpdate.setReceiverAddress(b.getReceiverAddress());
            return billRepository.save(bUpdate);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));
    }

            public Bill updateBill(int id, Bill b) {
        return billRepository.findById(id).map(bUpdate -> {
            bUpdate.setNote(b.getNote());
            bUpdate.setSubTotal(b.getSubTotal());
            bUpdate.setGrandTotal(b.getGrandTotal());
            return billRepository.save(bUpdate);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));
    }

}
