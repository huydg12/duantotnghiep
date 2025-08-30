package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Color;
import com.poly.BE_main.repository.ColorRepository;

@Service
public class ColorService {
    @Autowired
    ColorRepository colorRepository;

    public List<Color> findAll() {
        return colorRepository.findAll();
    }

    public Color create(Color c) {
        colorRepository.save(c);
        return c;
    }

    public void deleteById(Integer id) {
        colorRepository.deleteById(id);
    }

    public Color update(int id, Color cUpdate) {
        return colorRepository.findById(id).map(c -> {
            c.setName(cUpdate.getName());
            c.setDescription(cUpdate.getDescription());
            return colorRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy màu có id: " + id));
    }

    public Color updateStatus(int id, Color cUpdate) {
        return colorRepository.findById(id).map(c -> {
            if (c.isActive() == true) {
                c.setActive(false);
            }else{
                c.setActive(true);
            }
            return colorRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy màu có id: " + id));
    }
}
