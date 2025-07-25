package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Image;
import com.poly.BE_main.repository.ImageRepository;

@Service
public class ImageService {

    @Autowired
    ImageRepository imageRepository;

    public List<Image> FinAll() {
        return imageRepository.findAll();
    }

    public Image create(Image i) {
        return imageRepository.save(i);
    }

    public void delete(int id) {
        imageRepository.deleteById(id);
    }

    public Image update(int id, Image iupdate) {
        return imageRepository.findById(id).map(i -> {
            i.setUrl(iupdate.getUrl());
            i.setMain(iupdate.isMain());
            return imageRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh có id: " + id));
    }

    public long countByProductDetailId(int productDetailId) {
        return imageRepository.countByProductDetailId(productDetailId);
    }

    public List<Image> findByProductDetailId(int productDetailId) {
        return imageRepository.findByProductDetailId(productDetailId);
    }

    // ✅ Hàm tìm ảnh theo ID (dùng cho update ảnh có file mới)
    public Image findById(int id) {
        return imageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh có id: " + id));
    }
}
