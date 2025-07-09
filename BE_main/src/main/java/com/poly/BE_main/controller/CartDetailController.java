package com.poly.BE_main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.CartDetailDTO;
import com.poly.BE_main.dto.UpdateQuantityDTO;
import com.poly.BE_main.model.CartDetail;
import com.poly.BE_main.service.CartDetailService;
@CrossOrigin(origins = "http://localhost:5173")
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

    @PostMapping("/add")
    public void addCartDetail(@RequestBody CartDetail cartDetail) {
        cartDetailService.add(cartDetail);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCartDetail(@PathVariable Integer id) {
        cartDetailService.deleteById(id);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkProductExistsInCart(
        @RequestParam("cartId") Integer cartId,
        @RequestParam("productDetailId") Integer productDetailId
    ) {
    boolean exists = cartDetailService.existsProductDetailInCarDetail(cartId, productDetailId);
    return ResponseEntity.ok(exists);
    }


    @PutMapping("/updateQuantity")
    public ResponseEntity<String> updateQuantity(@RequestBody UpdateQuantityDTO request) {
        try {
            cartDetailService.updateQuantity(request.getCartId(), request.getProductDetailId(), request.getQuantity());
            return ResponseEntity.ok("Cập nhật số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    @PutMapping("/updateQuantityByCartDetailID/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
    Integer quantity = body.get("quantity");
    cartDetailService.upateQuantityByCartDetailID(id, quantity);
    return ResponseEntity.ok("Cập nhật thành công");
    }
}