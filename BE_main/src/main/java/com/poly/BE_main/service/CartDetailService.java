package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.CartDetail;

public interface CartDetailService {
    List<CartDetail> findAll();
    CartDetail findById(Long id);
    CartDetail save(CartDetail cartdetail);
    void deleteById(Long id);
}
