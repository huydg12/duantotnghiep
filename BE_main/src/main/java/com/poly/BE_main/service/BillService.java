package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Bill;

public interface BillService {
    List<Bill> findAll();
    Bill findById(Long id);
    Bill save(Bill bill);
    void deleteById(Long id);
}
