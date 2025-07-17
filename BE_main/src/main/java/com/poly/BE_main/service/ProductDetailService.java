package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.ProductDetailDTO;
import com.poly.BE_main.model.ProductDetail;
import com.poly.BE_main.repository.ProductDetailRepository;

@Service
public class ProductDetailService {
    @Autowired
    ProductDetailRepository productDetailRepository;

     public List<ProductDetail> findAll() {
        return productDetailRepository.findAll();
    }

    public List<ProductDetailDTO> findAllProductDetailDTOByID(Integer productId) {
        List<Object[]> results = productDetailRepository.findAllProductDetailDTOByID(productId);

        return results.stream().map(obj -> {
            Integer productIdVal = (Integer) obj[0];
            Integer productDetailId = (Integer) obj[1];
            String productName = (String) obj[2];
            String brandName = (String) obj[3];
            String color = (String) obj[4];
            String collar = (String) obj[5]; // Assuming collar is at index 5
            String description = (String) obj[6];
            String size = (String) obj[7];
            BigDecimal price = (BigDecimal) obj[8];
            String imageString = (String) obj[9];

            List<String> images = (imageString != null && !imageString.isEmpty())
                    ? Arrays.asList(imageString.split(","))
                    : Collections.emptyList();

            return new ProductDetailDTO(
                    productIdVal,
                    productDetailId,
                    productName,
                    brandName,
                    color,
                    collar,
                    description,
                    size,
                    price,
                    images);
        }).collect(Collectors.toList());
    }
    public Integer findProductID(Integer id){
        return productDetailRepository.findProductId(id);
    }
}
