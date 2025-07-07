package com.poly.BE_main.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Cart;
import com.poly.BE_main.service.CartService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public void addCart(@RequestBody Cart cart) {
        System.out.println("Thêm ok");
        cartService.add(cart);

    }


    @GetMapping("/getCartId/{customerId}")
    public ResponseEntity<Integer> getCartIdByCustomerId(@PathVariable Integer customerId) {
        Integer cartId = cartService.getCartIdByCustomerId(customerId);
        if (cartId != null) {
            return ResponseEntity.ok(cartId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}