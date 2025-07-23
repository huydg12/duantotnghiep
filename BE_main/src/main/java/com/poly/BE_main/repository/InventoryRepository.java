package com.poly.BE_main.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.BE_main.dto.InventoryDTO;
import com.poly.BE_main.model.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query("SELECT new com.poly.BE_main.dto.InventoryDTO(i.id, p.product.productName, p.size.eu, i.quantity, i.modifiedDate) " +
       "FROM Inventory i JOIN ProductDetail p ON i.productDetailId = p.id")
    List<InventoryDTO> findAllWithProductDetails();
}
