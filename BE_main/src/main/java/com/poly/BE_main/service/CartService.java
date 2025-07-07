package com.poly.BE_main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Cart;
import com.poly.BE_main.repository.CartRepository;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void add(Cart cart) {
        cartRepository.save(cart);
    }

    public Integer getCartIdByCustomerId(Integer customerId) {
    return cartRepository.findCartIdByCustomerId(customerId);
    }
}
