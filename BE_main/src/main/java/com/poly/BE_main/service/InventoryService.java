package com.poly.BE_main.service;

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
        inventoryRepository.save(inventory);
        return true;
    }

        return false; // Không tồn tại productDetailId
    }


}
