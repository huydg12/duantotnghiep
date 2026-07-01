package com.poly.BE_main.repository;

import com.poly.BE_main.model.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Integer> {
    long countByPromotionId(Integer promotionId);

    long countByPromotionIdAndCustomerId(Integer promotionId, Integer customerId);
}
