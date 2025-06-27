package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Size;

public interface SizeService {
    List<Size> findAll();
    Size findById(Long id);
    Size save(Size size);
    void deleteById(Long id);
}
