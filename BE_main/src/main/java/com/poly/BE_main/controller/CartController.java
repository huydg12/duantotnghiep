package com.poly.BE_main.controller;

import com.poly.BE_main.model.Cart;
import com.poly.BE_main.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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