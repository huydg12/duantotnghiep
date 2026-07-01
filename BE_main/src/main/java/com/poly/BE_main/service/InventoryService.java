package com.poly.BE_main.service;

import com.poly.BE_main.dto.InventoryDTO;
import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.model.InventoryMovement;
import com.poly.BE_main.repository.InventoryMovementRepository;
import com.poly.BE_main.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service

public class InventoryService {
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    InventoryMovementRepository movementRepository;

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<InventoryDTO> getAllWithDetails() {
        return inventoryRepository.findAllWithProductDetails();
    }

    public Inventory create(Inventory inventory) {

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

    @Transactional
    public boolean updateQuantityCong(int productDetailId, int addedQuantity, String username) {
        if (addedQuantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductDetailId(productDetailId);

        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            inventory.setQuantity(inventory.getQuantity() + addedQuantity);
            inventory.setModifiedDate(LocalDateTime.now());
            inventoryRepository.save(inventory);
            record(productDetailId, addedQuantity, inventory.getQuantity(), "IMPORT", username);
            return true;
        }

        return false; // Không tồn tại productDetailId
    }

    @Transactional
    public boolean updateQuantityTru(int productDetailId, int addedQuantity, String username) {
        if (addedQuantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductDetailId(productDetailId);

        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            if (inventory.getQuantity() < addedQuantity) throw new IllegalArgumentException("Insufficient inventory");
            inventory.setQuantity(inventory.getQuantity() - addedQuantity);
            inventory.setModifiedDate(LocalDateTime.now());
            inventoryRepository.save(inventory);
            record(productDetailId, -addedQuantity, inventory.getQuantity(), "MANUAL_OUT", username);
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

        if (optionalInventory.isEmpty())
            return false;

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

    public List<InventoryMovement> getMovements(Integer productDetailId) {
        return movementRepository.findByProductDetailIdOrderByCreatedAtDesc(productDetailId);
    }

    private void record(Integer pdId, int change, int balance, String type, String username) {
        InventoryMovement m = new InventoryMovement();
        m.setProductDetailId(pdId);
        m.setQuantityChange(change);
        m.setBalanceAfter(balance);
        m.setMovementType(type);
        m.setReferenceType("MANUAL");
        m.setCreatedBy(username);
        m.setCreatedAt(LocalDateTime.now());
        movementRepository.save(m);
    }

}
