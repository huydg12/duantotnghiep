package com.poly.BE_main.controller;

import java.util.HashMap;
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

import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.ProductDetailDTO;
import com.poly.BE_main.dto.ProductReceiptDTO;
import com.poly.BE_main.model.ProductDetail;
import com.poly.BE_main.service.ProductDetailService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/productDetail")
public class ProductDetailController {
    @Autowired
    ProductDetailService productDetailService;

    @GetMapping("/show")
    public List<ProductDetail> findAll() {
        return productDetailService.findAll();
    }

    @GetMapping("/showProductReceipt")
    public List<ProductReceiptDTO> getProductReceipt() {
        return productDetailService.getAllProductReceiptDTO();
    }

    @GetMapping("/show/{productId}")
    public ResponseEntity<List<ProductDetailDTO>> showProductDetail(@PathVariable Integer productId) {
        List<ProductDetailDTO> list = productDetailService.findAllProductDetailDTOByID(productId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/findProductId/{productDetailId}")
    public ResponseEntity<Map<String, Integer>> getProductIdFromDetail(@PathVariable Integer productDetailId) {
        Integer productId = productDetailService.findProductID(productDetailId);
        Map<String, Integer> response = new HashMap<>();
        response.put("productId", productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ProductDetail create(@RequestBody ProductDetail p) {
        return productDetailService.create(p);
    }

    @PutMapping("/update/{id}")
    public ProductDetail update(@PathVariable int id, @RequestBody ProductDetail p) {
        return productDetailService.update(id, p);
    }

    @PutMapping("/updateStatus/{id}")
    public ProductDetail updateStatus(@PathVariable int id, @RequestBody ProductDetail p) {
        return productDetailService.updateStatus(id, p);
    }


    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        productDetailService.deleteById(id);
    }
}