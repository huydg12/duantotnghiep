package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.PaymentMethod;
import com.poly.BE_main.repository.PaymentMethodRepository;

@Service
public class PaymentMethodService {
    @Autowired
    PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll();
    }

    public PaymentMethod create(PaymentMethod p) {
        return paymentMethodRepository.save(p);
    }

    public void delete(Integer id) {
        paymentMethodRepository.deleteById(id);
    }

    public PaymentMethod update(int id, PaymentMethod pUpdate) {
        return paymentMethodRepository.findById(id).map(p -> {
            p.setName(pUpdate.getName());
            p.setDescription(pUpdate.getDescription());
            p.setCreatedDate(pUpdate.getCreatedDate());
            p.setModifiedDate(pUpdate.getModifiedDate());
            return paymentMethodRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy Phương thức thanh toán có id: " + id));
    }
}
