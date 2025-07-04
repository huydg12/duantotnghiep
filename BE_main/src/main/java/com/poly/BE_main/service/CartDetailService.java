package com.poly.BE_main.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.CartDetailDTO;
import com.poly.BE_main.model.CartDetail;
import com.poly.BE_main.repository.CartDetailRepository;

@Service
public class CartDetailService {
    @Autowired
    private final CartDetailRepository cartDetailRepository;

    public CartDetailService(CartDetailRepository cartDetailRepository) {
        this.cartDetailRepository = cartDetailRepository;
    }

    public List<CartDetailDTO> findAllCartDetailByCustomer(Integer customerId) {

        List<Object[]> result = cartDetailRepository.findAllCartDetailByCustomer(customerId);
        return result.stream().map(obj -> {
            Integer cartDetailId = (Integer) obj[0];
            Integer cartId = (Integer) obj[1];
            Integer customerIdVal = (Integer) obj[2];
            Integer productId = (Integer) obj[3];
            String productName = (String) obj[4];
            Integer productDetailId = (Integer) obj[5];
            String size = (String) obj[6];
            String color = (String) obj[7];
            Integer quantity = (Integer) obj[8];
            String images = (String) obj[9];

            return new CartDetailDTO(
                    cartDetailId,
                    cartId,
                    customerIdVal,
                    productId,
                    productName,
                    productDetailId,
                    size,
                    color,
                    quantity,
                    images);
        }).collect(Collectors.toList());
    }

    public void add(CartDetail cartDetail) {
        cartDetailRepository.save(cartDetail);
    }

}
