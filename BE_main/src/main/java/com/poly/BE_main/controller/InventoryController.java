package com.poly.BE_main.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.service.InventoryService;

@RestController
@RequestMapping("/inventory")

public class InventoryController {
    @Autowired
    InventoryService inventoryService;

    @GetMapping("/show")
    public List<Inventory> findAll() {
        return inventoryService.findAll();
    }
}