package com.poly.BE_main.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.InventoryDTO;
import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.service.InventoryService;

@RestController
@RequestMapping("/inventory")

public class InventoryController {
    @Autowired
    InventoryService inventoryService;

    @GetMapping("/show")
    public List<InventoryDTO> showInventory() {
        return inventoryService.getAllWithDetails();
    }

    @PostMapping("/create")
        public Inventory create(@RequestBody Inventory inventory) {
        return inventoryService.create(inventory);
    }

    @GetMapping("/check/{productDetailId}")
    public ResponseEntity<Map<String, Boolean>> checkInventory(@PathVariable int productDetailId) {
        boolean exists = inventoryService.checkInventoryExists(productDetailId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/updateQuantity/{productDetailId}")
    public ResponseEntity<?> updateQuantity(@PathVariable("productDetailId") Integer productDetailId,
                                            @RequestBody Map<String, Integer> body) {
        int quantity = body.get("quantity");
        inventoryService.updateQuantity(productDetailId, quantity);
        return ResponseEntity.ok().build();
    }
}