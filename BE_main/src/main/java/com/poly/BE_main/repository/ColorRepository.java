package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.BE_main.model.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer>{

}