package com.poly.BE_main.service;

import java.math.BigDecimal;
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
            BigDecimal price = (BigDecimal) obj[8];
            Integer quantity = (Integer) obj[9];
            String images = (String) obj[10];

            return new CartDetailDTO(
                    cartDetailId,
                    cartId,
                    customerIdVal,
                    productId,
                    productName,
                    productDetailId,
                    size,
                    color,
                    price,
                    quantity,
                    images);
        }).collect(Collectors.toList());
    }

    public void add(CartDetail cartDetail) {
        cartDetailRepository.save(cartDetail);
    }
          public CartDetail update(int id, CartDetail cartDetail) {
        return cartDetailRepository.findById(id).map(c -> {
            c.setId(cartDetail.getId());
            c.setProductDetailId(cartDetail.getProductDetailId());
            c.setQuantity(cartDetail.getQuantity());
            return cartDetailRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id: " + id));
    }

         
    public void deleteById(Integer id) {
        cartDetailRepository.deleteById(id);
    }

}
