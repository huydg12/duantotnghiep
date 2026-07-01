package com.poly.BE_main.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.poly.BE_main.model.InventoryMovement;
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement,Integer> {
    List<InventoryMovement> findByProductDetailIdOrderByCreatedAtDesc(Integer productDetailId);
}
