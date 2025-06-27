package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Sole;

public interface SoleService {
    List<Sole> findAll();
    Sole findById(Long id);
    Sole save(Sole sole);
    void deleteById(Long id);
}
