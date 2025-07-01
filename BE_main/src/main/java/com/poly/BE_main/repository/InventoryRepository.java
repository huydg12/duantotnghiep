package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Inventory;

public interface InventoryRepository extends JpaRepository <Inventory, Integer> {
    
}