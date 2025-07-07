package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Image;
import com.poly.BE_main.service.ImageService;

@RestController
@RequestMapping("/image")
public class ImageController {
    @Autowired
    ImageService imageService;

    @GetMapping("/show")
    public List<Image>findAll() {
        return imageService.FinAll();
    }

    @PostMapping("/add")
    public Image create (@RequestBody Image image){
        return imageService.create(image);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id){
        imageService.delete(id);
    }

    @PutMapping("/update/{id}")
        public  Image update(@PathVariable int id, @RequestBody Image i){
            return imageService.update(id, i);
        }
}
