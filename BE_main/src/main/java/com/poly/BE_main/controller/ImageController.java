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

    private final String uploadFolder = "D:/ManhDuAn/duantotnghiep/FE_main/my-project/public/images";

    @GetMapping("/show")
    public List<Image> findAll() {
        return imageService.FinAll();
    }

    @GetMapping("/show/{productDetailId}")
    public List<Image> findByProductDetailId(@PathVariable int productDetailId ) {
        return imageService.findByProductDetailId(productDetailId);
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
        Optional<Image> imageOpt = imageRepository.findById(id);

        if (!imageOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ảnh không tồn tại");
        }

        Image image = imageOpt.get();

        // Đường dẫn tuyệt đối đến file ảnh
        String imagePath = uploadFolder + "/" + image.getUrl().replace("./images/", "");

        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path); // Xoá file ảnh thật
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xoá file ảnh trên ổ đĩa");
        }

        // Xoá ảnh khỏi database
        imageRepository.deleteById(id);

        return ResponseEntity.ok("Đã xoá ảnh thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateImage(
            @PathVariable int id,
            @RequestParam("isMain") boolean isMain,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Image image = imageService.findById(id);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            image.setMain(isMain);
            image.setProductDetailId(productDetailId);

            if (file != null && !file.isEmpty()) {
                // Xoá ảnh cũ
                String oldFileName = image.getUrl().replace("./images/", "");
                Path oldPath = Paths.get("D:/ManhDuAn/duantotnghiep/FE_main/my-project/public/images", oldFileName);
                Files.deleteIfExists(oldPath);

                // Upload ảnh mới
                String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
                String newFileName = UUID.randomUUID().toString() + extension;
                Path newPath = Paths.get("D:/ManhDuAn/duantotnghiep/FE_main/my-project/public/images", newFileName);
                Files.write(newPath, file.getBytes());

                image.setUrl("./images/" + newFileName);
            }

            Image updated = imageService.update(id, image);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi cập nhật ảnh.");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam("mainImageIndex") int mainImageIndex,
            @RequestParam(value = "customFilename", required = false) String customFilename) {
        String feImageFolder = "D:/ManhDuAn/duantotnghiep/FE_main/my-project/public/images";
        String urlPrefix = "./images/";

        long currentCount = imageService.countByProductDetailId(productDetailId);
        if (currentCount + files.size() > 6) {
            return ResponseEntity.badRequest().body("Một sản phẩm chỉ được tối đa 6 ảnh.");
        }

        List<Image> savedImages = new ArrayList<>();
        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    String originalName = file.getOriginalFilename();
                    String extension = originalName.substring(originalName.lastIndexOf('.'));

                    // ✅ Sử dụng customFilename nếu có
                    String baseFileName;
                    if (customFilename != null && !customFilename.isBlank() && files.size() == 1) {
                        baseFileName = customFilename;
                    } else {
                        baseFileName = originalName.substring(0, originalName.lastIndexOf('.'));
                    }

                    // ✅ Tránh trùng tên
                    String finalFileName = baseFileName + extension;
                    Path path = Paths.get(feImageFolder, finalFileName);
                    int counter = 1;
                    while (Files.exists(path)) {
                        finalFileName = baseFileName + "_" + counter + extension;
                        path = Paths.get(feImageFolder, finalFileName);
                        counter++;
                    }

                    Files.createDirectories(path.getParent());
                    Files.write(path, file.getBytes());

                    String fileUrl = urlPrefix + finalFileName;

                    Image image = new Image();
                    image.setProductDetailId(productDetailId);
                    image.setUrl(fileUrl);
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

    @PostMapping("/update-file")
    public ResponseEntity<?> updateImageFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productDetailId") int productDetailId,
            @RequestParam("imageIndex") int imageIndex) {

        try {
            List<Image> images = imageService.findByProductDetailId(productDetailId);

            if (imageIndex < 0 || imageIndex >= images.size()) {
                return ResponseEntity.badRequest().body("Vị trí ảnh không hợp lệ.");
            }

            Image imageToUpdate = images.get(imageIndex);

            // Xoá file cũ
            String oldFileName = imageToUpdate.getUrl().replace("./images/", "");
            Path oldPath = Paths.get(uploadFolder, oldFileName);
            Files.deleteIfExists(oldPath);

            // Upload file mới
            String newFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path newPath = Paths.get(uploadFolder, newFileName);
            Files.createDirectories(newPath.getParent());
            Files.write(newPath, file.getBytes());

            imageToUpdate.setUrl("./images/" + newFileName);
            imageService.update(imageToUpdate.getId(), imageToUpdate); // Đổi create → update

            return ResponseEntity.ok("Cập nhật ảnh thành công.");
        } catch (IOException e) {
            e.printStackTrace();
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
