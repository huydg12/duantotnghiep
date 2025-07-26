package com.poly.BE_main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.InventoryDTO;
import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.repository.InventoryRepository;

@Service

public class InventoryService {
    @Autowired
    InventoryRepository inventoryRepository;

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<InventoryDTO> getAllWithDetails() {
        return inventoryRepository.findAllWithProductDetails();
    }

    public Inventory create(Inventory inventory){

        if (inventory.getModifiedDate() == null) {
        inventory.setModifiedDate(LocalDateTime.now());
    }
        return inventoryRepository.save(inventory);
    }
    
    public Inventory update(int id, Inventory iUpdate) {
        return inventoryRepository.findById(id).map(i -> {
            i.setQuantity(iUpdate.getQuantity());
            i.setModifiedDate(iUpdate.getModifiedDate());
            return inventoryRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy kho có id: " + id));
    }
    public boolean checkInventoryExists(int productDetailId) {
        return inventoryRepository.existsByProductDetailId(productDetailId);
    }
    public boolean updateQuantity(int productDetailId, int addedQuantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductDetailId(productDetailId);

    if (optionalInventory.isPresent()) {
        Inventory inventory = optionalInventory.get();
        inventory.setQuantity(inventory.getQuantity() + addedQuantity);
        inventory.setModifiedDate(LocalDateTime.now()); 
        inventoryRepository.save(inventory);
        return true;
    }

        return false; // Không tồn tại productDetailId
    }
    public boolean updateQuantityByPayment(Integer productDetailId, Integer purchasedQuantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductDetailId(productDetailId);

        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            int currentQuantity = inventory.getQuantity();

            if (currentQuantity < purchasedQuantity) {
                return false;
            }

            inventory.setQuantity(currentQuantity - purchasedQuantity);
            inventoryRepository.save(inventory);
            return true;
        }

        return false;
    }

    public boolean updateQuantityByBill(Integer productDetailId, Integer newQuantity, Integer oldQuantity) {
    Optional<Inventory> optionalInventory = inventoryRepository.findByProductDetailId(productDetailId);

    if (optionalInventory.isEmpty()) return false;

    Inventory inventory = optionalInventory.get();
    int currentInventory = inventory.getQuantity();

    // Chênh lệch giữa số lượng mới và cũ
    Integer delta = newQuantity - oldQuantity;

    // Nếu tăng số lượng -> phải kiểm tra tồn kho đủ không
    if (delta > 0 && currentInventory < delta) {
        return false; // Không đủ hàng
    }

    // Cập nhật tồn kho (trừ nếu delta > 0, cộng lại nếu delta < 0)
    inventory.setQuantity(currentInventory - delta);
    inventoryRepository.save(inventory);
    return true;
    }

        // ✅ Trả về số lượng tồn kho theo productDetailId
    public Integer getQuantityByProductDetailId(Integer productDetailId) {
        return inventoryRepository.findByProductDetailId(productDetailId)
                .map(Inventory::getQuantity)
                .orElse(0); // nếu không có -> trả về 0
    }


}
