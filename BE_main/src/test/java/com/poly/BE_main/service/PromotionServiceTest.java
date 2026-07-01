package com.poly.BE_main.service;

import com.poly.BE_main.model.Promotion;
import com.poly.BE_main.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PromotionServiceTest {
    private PromotionRepository repository;
    private PromotionService service;

    @BeforeEach
    void setup() {
        repository = mock(PromotionRepository.class);
        service = new PromotionService();
        service.promotionRepository = repository;
    }

    @Test
    void rejectsPercentageOverOneHundred() {
        Promotion p = valid();
        p.setValue(BigDecimal.valueOf(101));
        assertThrows(IllegalArgumentException.class, () -> service.create(p));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDuplicateCodeCaseInsensitively() {
        Promotion p = valid();
        when(repository.existsByPromotionCodeIgnoreCase("SALE10")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(p));
    }

    private Promotion valid() {
        Promotion p = new Promotion();
        p.setPromotionCode("SALE10");
        p.setType(1);
        p.setValue(BigDecimal.TEN);
        p.setStartDate(LocalDateTime.now());
        p.setEndDate(LocalDateTime.now().plusDays(1));
        return p;
    }
}
