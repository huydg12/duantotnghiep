package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Style;

public interface StyleService {
    List<Style> findAll();
    Style findById(Long id);
    Style save(Style style);
    void deleteById(Long id);
}
