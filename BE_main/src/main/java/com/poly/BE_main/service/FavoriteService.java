package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.FavoriteDTO;
import com.poly.BE_main.model.Favorite;
import com.poly.BE_main.repository.FavoriteRepository;
@Service
public class FavoriteService {
    @Autowired
    FavoriteRepository favoriteRepository;

    public List<FavoriteDTO> getFavoritesByCustomerId(Integer customerId) {
    List<Object[]> rows = favoriteRepository.findFavoritesByCustomerId(customerId);
    return rows.stream().map(row -> new FavoriteDTO(
        (Integer) row[0],               // favoriteId
        (Integer) row[1],               // customerId
        (Integer) row[2],               // productId
        (String)  row[3],               // productName
        (String)  row[4],               // brandName
        (BigDecimal) row[5],            // price
        (String)  row[6],               // image1
        (String)  row[7]                // image2
    )).toList();
    }
    public Favorite create(Favorite favorite){
        return favoriteRepository.save(favorite);
    }
    public void delete(Integer id){
        favoriteRepository.deleteById(id);
    }
}
