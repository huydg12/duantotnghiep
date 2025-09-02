package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Sole;
import com.poly.BE_main.repository.SoleRepository;

@Service
public class SoleService {
    @Autowired
    SoleRepository soleRepository;

    public List<Sole> findAll() {
        return soleRepository.findAll();
    }

    public Sole create(Sole s) {
        return soleRepository.save(s);
    }

    public void delete(Integer id) {
        soleRepository.deleteById(id);
    }

    public Sole update(int id, Sole sUpdate) {
        return soleRepository.findById(id).map(s -> {
            s.setName(sUpdate.getName());
            s.setDescription(sUpdate.getDescription());
            return soleRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy Sole có id: " + id));
    }

    public Sole updateStatus(int id, Sole sUpdate) {
        return soleRepository.findById(id).map(s -> {
            if (s.isActive()) {
                s.setActive(false);
            } else {
                s.setActive(true);
            }
            return soleRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy Sole có id: " + id));
    }
}
