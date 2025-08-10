package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Collar;
import com.poly.BE_main.repository.CollarRepository;

@Service
public class CollarService {
    @Autowired
    CollarRepository collarRepository;

    public List<Collar> findAll() {
        return collarRepository.findAll();
    }

    public Collar create(Collar c) {
        return collarRepository.save(c);
    }

    public void delete(int id) {
        collarRepository.deleteById(id);
    }

    public Collar update(int id, Collar cUpdate) {
        return collarRepository.findById(id).map(c -> {
            c.setName(cUpdate.getName());
            c.setDescription(cUpdate.getDescription());
            return collarRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy cổ giày có id: " + id));
    }
}
