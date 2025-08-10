package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Size;
import com.poly.BE_main.repository.SizeRepository;

@Service
public class SizeService {
    @Autowired
    SizeRepository sizeRepository;

    public List<Size> findAll() {
        return sizeRepository.findAll();
    }

    public Size create(Size s) {
        return sizeRepository.save(s);
    }

    public void delete(Integer id) {
        sizeRepository.deleteById(id);
    }

    public Size update(int id, Size sUpdate) {
        return sizeRepository.findById(id).map(s -> {
            s.setEu(sUpdate.getEu());
            s.setDescription(sUpdate.getDescription());
            return sizeRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy Size có id: " + id));
    }
}
