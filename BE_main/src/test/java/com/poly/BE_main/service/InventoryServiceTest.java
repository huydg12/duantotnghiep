package com.poly.BE_main.service;

import com.poly.BE_main.model.Inventory;
import com.poly.BE_main.repository.InventoryMovementRepository;
import com.poly.BE_main.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {
    private InventoryRepository inventoryRepository;
    private InventoryMovementRepository movementRepository;
    private InventoryService service;

    @BeforeEach
    void setup() {
        inventoryRepository = mock(InventoryRepository.class);
        movementRepository = mock(InventoryMovementRepository.class);
        service = new InventoryService();
        service.inventoryRepository = inventoryRepository;
        service.movementRepository = movementRepository;
    }

    @Test
    void refusesNegativeStock() {
        Inventory stock = new Inventory();
        stock.setProductDetailId(10);
        stock.setQuantity(2);
        when(inventoryRepository.findByProductDetailId(10)).thenReturn(Optional.of(stock));
        assertThrows(IllegalArgumentException.class, () -> service.updateQuantityTru(10, 3, "staff"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void recordsIncomingMovement() {
        Inventory stock = new Inventory();
        stock.setProductDetailId(10);
        stock.setQuantity(2);
        when(inventoryRepository.findByProductDetailId(10)).thenReturn(Optional.of(stock));
        assertTrue(service.updateQuantityCong(10, 5, "staff"));
        assertEquals(7, stock.getQuantity());
        verify(movementRepository).save(argThat(m -> m.getQuantityChange() == 5 && m.getBalanceAfter() == 7));
    }
}
