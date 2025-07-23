package com.poly.BE_main.service;

import java.util.List;

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
    
    public Inventory update(int id, Inventory iUpdate) {
        return inventoryRepository.findById(id).map(i -> {
            i.setQuantity(iUpdate.getQuantity());
            i.setModifiedDate(iUpdate.getModifiedDate());
            return inventoryRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy kho có id: " + id));
    }

}
