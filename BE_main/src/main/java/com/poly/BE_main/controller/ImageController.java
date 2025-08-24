package com.poly.BE_main.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.poly.BE_main.model.Image;
import com.poly.BE_main.repository.ImageRepository;
import com.poly.BE_main.service.ImageService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    ImageService imageService;
    @Autowired
    ImageRepository imageRepository;

    private static final Path UPLOAD_DIR = Paths.get("D:/Shoes/FE_main/my-project/public/images");
    private static final String URL_PREFIX = "./images/";

    public ImageController() {
        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException ignored) {
        }
    }

    private Path resolveFilePath(String url) {
        String fileName = url.replace(URL_PREFIX, "");
        return UPLOAD_DIR.resolve(fileName);
    }

    private String toUrl(String fileName) {
        return URL_PREFIX + fileName;
    }

    @GetMapping("/show")
    public List<Image> findAll() {
        return imageService.findAll();
    }

    @GetMapping("/show/{productDetailId}")
    public List<Image> findByProductDetailIdOrderByIdAsc(@PathVariable int productDetailId) {
        return imageService.findByProductDetailIdOrderByIdAsc(productDetailId);
    }

    @PostMapping("/add")
    public Image create(@RequestBody Image image) {
        return imageService.create(image);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        imageService.delete(id);
    }

    @DeleteMapping("/delete-image/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer id) {
        Optional<Image> imgOpt = imageRepository.findById(id);
        if (imgOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ảnh không tồn tại");

        Image img = imgOpt.get();
        try {
            Files.deleteIfExists(resolveFilePath(img.getUrl())); // xoá file thật
            imageRepository.deleteById(id); // xoá DB
            return ResponseEntity.ok("Đã xoá ảnh thành công");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xoá file ảnh");
        }
    }

    // Cập nhật thông tin ảnh
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable int id,
            @RequestParam("isMain") boolean isMain,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Image image = imageService.findById(id);
            image.setMain(isMain);
            image.setProductDetailId(productDetailId);

            if (file != null && !file.isEmpty()) {
                // xoá file cũ
                Files.deleteIfExists(resolveFilePath(image.getUrl()));

                // lưu file mới với tên random
                String orig = file.getOriginalFilename();
                String ext = (orig != null && orig.lastIndexOf('.') != -1) ? orig.substring(orig.lastIndexOf('.')) : "";
                String newFileName = UUID.randomUUID() + ext;

                Path newPath = UPLOAD_DIR.resolve(newFileName);
                Files.createDirectories(newPath.getParent());
                Files.write(newPath, file.getBytes());

                image.setUrl(toUrl(newFileName));
            }
            return ResponseEntity.ok(imageService.update(id, image));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi cập nhật ảnh.");
        }
    }

    // Thêm mới nhiều ảnh
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam("mainImageIndex") int mainImageIndex,
            @RequestParam(value = "customFilename", required = false) String customFilename) {

        long currentCount = imageService.countByProductDetailId(productDetailId);
        if (currentCount + files.size() > 6) {
            return ResponseEntity.badRequest().body("Một sản phẩm chỉ được tối đa 6 ảnh.");
        }

        List<Image> savedImages = new ArrayList<>();

        try {
            Files.createDirectories(UPLOAD_DIR);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty())
                    continue;

                String originalName = file.getOriginalFilename();
                String ext = (originalName != null && originalName.lastIndexOf('.') != -1)
                        ? originalName.substring(originalName.lastIndexOf('.'))
                        : "";

                String baseName;
                if (customFilename != null && !customFilename.isBlank() && files.size() == 1) {
                    baseName = customFilename;
                } else {
                    baseName = (originalName != null && originalName.lastIndexOf('.') != -1)
                            ? originalName.substring(0, originalName.lastIndexOf('.'))
                            : (originalName != null ? originalName : UUID.randomUUID().toString());
                }

                String candidate = baseName + ext;
                Path candidatePath = UPLOAD_DIR.resolve(candidate);

                // ==== DEDUPE THEO NỘI DUNG ====
                byte[] newBytes = file.getBytes();
                if (Files.exists(candidatePath)) {
                    // Nếu file cùng tên đã tồn tại: so sánh nội dung
                    byte[] oldBytes = Files.readAllBytes(candidatePath);
                    if (!java.util.Arrays.equals(oldBytes, newBytes)) {
                        // khác nội dung -> tìm tên rảnh: _1, _2, ...
                        int counter = 1;
                        while (Files.exists(candidatePath)) {
                            candidate = baseName + "_" + counter + ext;
                            candidatePath = UPLOAD_DIR.resolve(candidate);
                            counter++;
                        }
                        Files.write(candidatePath, newBytes);
                    }
                    // nếu cùng nội dung -> KHÔNG ghi thêm, tái sử dụng file cũ (giữ nguyên
                    // candidatePath)
                } else {
                    // tên chưa tồn tại -> ghi mới
                    Files.write(candidatePath, newBytes);
                }

                Image img = new Image();
                img.setProductDetailId(productDetailId);
                img.setUrl(URL_PREFIX + candidate);
                img.setMain(i == mainImageIndex); // đánh dấu tạm thời

                savedImages.add(imageService.create(img));
            }

            // Nếu có main index, đảm bảo chỉ 1 ảnh là main
            if (mainImageIndex >= 0 && mainImageIndex < savedImages.size()) {
                imageService.setMainImage(savedImages.get(mainImageIndex).getId(), productDetailId);
            }

            return ResponseEntity.ok(savedImages);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lưu ảnh.");
        }
    }

    @PostMapping("/update-file")
    public ResponseEntity<?> updateImageFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam("imageIndex") int imageIndex) {

        try {
            List<Image> images = imageService.findByProductDetailIdOrderByIdAsc(productDetailId);
            if (imageIndex < 0 || imageIndex >= images.size()) {
                return ResponseEntity.badRequest().body("Vị trí ảnh không hợp lệ.");
            }

            Image img = images.get(imageIndex);

            Path oldPath = resolveFilePath(img.getUrl());
            String oldName = oldPath.getFileName().toString();

            String orig = file.getOriginalFilename();
            String newExt = "";
            if (orig != null) {
                int dot = orig.lastIndexOf('.');
                if (dot != -1)
                    newExt = orig.substring(dot).toLowerCase();
            }

            String base = oldName.contains(".") ? oldName.substring(0, oldName.lastIndexOf('.')) : oldName;
            String newName = base + (newExt.isEmpty() ? "" : newExt);
            Path newPath = UPLOAD_DIR.resolve(newName);

            if (!newPath.equals(oldPath)) {
                Files.deleteIfExists(oldPath);
                img.setUrl(toUrl(newName));
            }

            Files.createDirectories(newPath.getParent());
            Files.copy(file.getInputStream(), newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            imageService.update(img.getId(), img);
            return ResponseEntity.ok(img);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật ảnh.");
        }
    }

    @PutMapping("/set-main/{imageId}")
    public ResponseEntity<String> setMainImage(
            @PathVariable Integer imageId,
            @RequestParam Integer productDetailId) {
        imageService.setMainImage(imageId, productDetailId);
        return ResponseEntity.ok("Đã đặt ảnh chính.");
    }
}
