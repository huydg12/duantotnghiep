package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Cart;

public interface CartService {
    List<Cart> findAll();

    Cart findById(Long id);

    Cart save(Cart cart);

    void deleteById(Long id);
}
