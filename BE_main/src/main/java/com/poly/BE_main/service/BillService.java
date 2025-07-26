package com.poly.BE_main.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.BillDTO;
import com.poly.BE_main.model.Bill;
import com.poly.BE_main.model.BillDetail;
import com.poly.BE_main.repository.BillDetailRepository;
import com.poly.BE_main.repository.BillRepository;

@Service
public class BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    BillDetailRepository billDetailRepository;

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

    public Bill createBill(BillDTO dto) {
        // 1. Tạo Bill và gán đầy đủ các trường từ DTO
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
        bill.setSubTotal(dto.getSubTotal());
        bill.setDiscountAmount(dto.getDiscountAmount());
        bill.setShippingFee(dto.getShippingFee());
        bill.setGrandTotal(dto.getGrandTotal());
        bill.setCreatedDate(LocalDate.now());

        // 2. Lưu Bill trước để có ID
        Bill savedBill = billRepository.save(bill);

        // 3. Tạo danh sách BillDetail từ DTO
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

        // 4. Lưu danh sách chi tiết hóa đơn
        billDetailRepository.saveAll(details);

        // 5. Trả về Bill đã lưu (nếu cần trả cả chi tiết, có thể set thêm)
        return savedBill;
    }

}
