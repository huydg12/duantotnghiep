// package com.poly.BE_main.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.poly.BE_main.dto.ProductDetailDTO;
// import com.poly.BE_main.service.ProductDetailService;

// @CrossOrigin(origins = "http://localhost:5173")
// @RestController
// @RequestMapping("/productDetail")
// public class ProductDetailController {
//     @Autowired
//     ProductDetailService productDetailService;

// // @GetMapping("/show/{productId}")
// // public ResponseEntity<List<ProductDetailDTO>> showProductDetail(@PathVariable Integer productId) {
// //     List<ProductDetailDTO> list = productDetailService.findAllProductDetailDTOByID(productId);
// //     return ResponseEntity.ok(list);
// // }
// }