package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.poly.BE_main.model.BillDetail;
import com.poly.BE_main.repository.BillDetailRepository;

@Service
public class BillDetailService {
    @Autowired
    BillDetailRepository billDetailRepository;

    public List<BillDetail> findAll() {
        return billDetailRepository.findAll();
    }

    public List<BillDetail> findByBillId(Integer billId) {
        return billDetailRepository.findByBillId(billId);
    }

    public BillDetail create(BillDetail i) {
        return billDetailRepository.save(i);
    }

    public void delete(Integer i) {
        billDetailRepository.deleteById(i);
    }

    public BillDetail update(int id, BillDetail bUpdate) {
        return billDetailRepository.findById(id).map(i -> {
            i.setBill(bUpdate.getBill());
            i.setProductDetailId(bUpdate.getProductDetailId());
            i.setQuantity(bUpdate.getQuantity());
            i.setPrice(bUpdate.getPrice());
            i.setStatus(bUpdate.getStatus());
            i.setProductImage(bUpdate.getProductImage());
            i.setSize(bUpdate.getSize());
            i.setProductName(bUpdate.getProductName());
            return billDetailRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn chi tiết có id:" + id));
    }

}
