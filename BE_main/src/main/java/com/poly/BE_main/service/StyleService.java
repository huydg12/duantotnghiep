package com.poly.BE_main.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Style;
import com.poly.BE_main.repository.StyleRepository;

@Service
public class StyleService {

    @Autowired
    StyleRepository styleRepository;

    public List<Style> finall() {
        return styleRepository.findAll();
    }

    public Style create(Style s) {
        return styleRepository.save(s);
    }

    public void delete(Integer id) {
        styleRepository.deleteById(id);
    }

    public Style update(int id, Style sUpdate) {
        return styleRepository.findById(id).map(s -> {
            s.setName(sUpdate.getName());
            s.setDescription(sUpdate.getDescription());
            return styleRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy style có id: " + id));
    }
}
