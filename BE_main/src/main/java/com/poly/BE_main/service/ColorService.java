package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Color;

public interface ColorService {
    List<Color> findAll();
    Color findById(Long id);
    Color save(Color color);
    void deleteById(Long id);
}
