package com.poly.BE_main.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.PromotionDetailDTO;
import com.poly.BE_main.model.ProductDetail;
import com.poly.BE_main.model.PromotionDetail;
import com.poly.BE_main.repository.ProductDetailRepository;
import com.poly.BE_main.repository.PromotionDetailRepository;

@Service
public class PromotionDetailService {

    @Autowired
    PromotionDetailRepository repository;

    @Autowired
    ProductDetailRepository productDetailRepo;

    public List<PromotionDetail> findAll() {
        return repository.findAll();
    }

    public List<PromotionDetail> findByPromotionId(Integer promotionId) {
        return repository.findByPromotionId(promotionId);
    }

    public PromotionDetail create(PromotionDetail detail) {
        return repository.save(detail);
    }

    public PromotionDetail update(Integer id, PromotionDetail detailUpdate) {
        return repository.findById(id).map(detail -> {
            detail.setProductDetailId(detailUpdate.getProductDetailId());
            detail.setPromotionValue(detailUpdate.getPromotionValue());
            detail.setStatus(detailUpdate.getStatus());
            return repository.save(detail);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết khuyến mãi với ID: " + id));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    // ✅ Trả về PromotionDetailDTO để FE hiển thị chi tiết sản phẩm
    public List<PromotionDetailDTO> getDetailsByPromotionId(Integer promotionId) {
        List<PromotionDetail> details = repository.findByPromotionId(promotionId);

        return details.stream().map(detail -> {
            ProductDetail pd = productDetailRepo.findById(detail.getProductDetailId()).orElse(null);
            if (pd != null && pd.getProduct() != null && pd.getSize() != null && pd.getColor() != null) {
                return new PromotionDetailDTO(
                        detail.getId(),
                        pd.getId(),
                        pd.getProduct().getProductName(),
                        pd.getSize().getEu(),
                        pd.getColor().getName(),
                        detail.getPromotionValue(),
                        detail.getStatus());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
