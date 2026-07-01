package com.poly.BE_main.controller;

import com.poly.BE_main.dto.FavoriteDTO;
import com.poly.BE_main.model.Favorite;
import com.poly.BE_main.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
