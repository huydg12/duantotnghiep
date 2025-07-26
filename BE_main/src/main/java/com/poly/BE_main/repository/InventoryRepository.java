package com.poly.BE_main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.BE_main.dto.InventoryDTO;
import com.poly.BE_main.model.Inventory;


public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
@Query("""
    SELECT new com.poly.BE_main.dto.InventoryDTO(
        i.id,
        p.product.productName,
        p.color.name,
        p.size.eu,
        i.quantity,
        i.modifiedDate,
        d.unitPrice
    )
    FROM Inventory i
    JOIN ProductDetail p ON i.productDetailId = p.id
    LEFT JOIN ImportReceiptDetail d ON d.id = (
        SELECT MAX(sub.id)
        FROM ImportReceiptDetail sub
        WHERE sub.productDetailId = i.productDetailId
    )
""")
List<InventoryDTO> findAllWithProductDetails();

    boolean existsByProductDetailId(int productDetailId);

    Optional<Inventory> findByProductDetailId(int productDetailId);
}
