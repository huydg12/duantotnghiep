package com.poly.BE_main.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.poly.BE_main.model.Image;
import com.poly.BE_main.service.ImageService;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    ImageService imageService;

    @GetMapping("/show")
    public List<Image> findAll() {
        return imageService.FinAll();
    }

    @PostMapping("/add")
    public Image create(@RequestBody Image image) {
        return imageService.create(image);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        imageService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Image update(@PathVariable int id, @RequestBody Image i) {
        return imageService.update(id, i);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam("mainImageIndex") int mainImageIndex) { // 🔥 Thêm dòng này

        // 🔎 Kiểm tra số lượng ảnh hiện tại
        long currentCount = imageService.countByProductDetailId(productDetailId);
        if (currentCount + files.size() > 6) {
            return ResponseEntity.badRequest().body("Một sản phẩm chỉ được tối đa 6 ảnh.");
        }

        List<Image> savedImages = new ArrayList<>();
        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

                    Path path = Paths.get(System.getProperty("user.dir"), "uploads", fileName);
                    Files.createDirectories(path.getParent());
                    Files.write(path, file.getBytes());

                    String fileUrl = "http://localhost:8080/uploads/" + fileName;

                    Image image = new Image();
                    image.setProductDetailId(productDetailId);
                    image.setUrl(fileUrl);

                    // ✅ Đúng ảnh được chọn là ảnh chính
                    image.setMain(i == mainImageIndex);

                    Image saved = imageService.create(image);
                    savedImages.add(saved);
                }
            }
            return ResponseEntity.ok(savedImages);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lưu ảnh.");
        }
    }

}
