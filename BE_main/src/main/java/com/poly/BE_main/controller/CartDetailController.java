package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.CartDetailDTO;
import com.poly.BE_main.service.CartDetailService;

@RestController
@RequestMapping("/cartDetail")
public class CartDetailController {

    @Autowired
    private final CartDetailService cartDetailService;
    public CartDetailController(CartDetailService cartDetailService) {
        this.cartDetailService = cartDetailService;
    }

    @GetMapping("/showCartDetail/{customerId}")
    public List<CartDetailDTO> showCartDetail(@PathVariable Integer customerId) {
        return cartDetailService.findAllCartDetailByCustomer(customerId);
    }
}