package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.FavoriteDTO;
import com.poly.BE_main.model.Favorite;
import com.poly.BE_main.service.FavoriteService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/show/{customerId}")
    public List<FavoriteDTO> findAllFavoriteDTOs(@PathVariable Integer customerId) {
        return favoriteService.getFavoritesByCustomerId(customerId);
    }

    @PostMapping("/add")
    public Favorite createFavorite(@RequestBody Favorite favorite) {
        return favoriteService.create(favorite);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteFavorite(@PathVariable Integer id) {
        favoriteService.delete(id);
    }

}
