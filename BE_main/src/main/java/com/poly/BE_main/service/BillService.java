package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.poly.BE_main.model.Bill;
import com.poly.BE_main.repository.BillRepository;


@Service
public class BillService {
    
    @Autowired
    BillRepository billRepository;

    public List<Bill> findAll(){
        return billRepository.findAll();
    }

    public Bill create(Bill i){
        return billRepository.save(i);
    }

    public void delete(Integer id){
        billRepository.deleteById(id);
    }

    public Bill update(int id, Bill bUpdate){
        return billRepository.findById(id).map(i ->{
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
}
