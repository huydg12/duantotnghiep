package com.poly.BE_main.repository;

import com.poly.BE_main.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByIdAndIsActiveTrue(Integer id);

    boolean existsByPromotionCodeIgnoreCase(String promotionCode);

    boolean existsByPromotionCodeIgnoreCaseAndIdNot(String promotionCode, Integer id);
}
