package com.poly.BE_main.repository;

import com.poly.BE_main.model.Sole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoleRepository extends JpaRepository<Sole, Integer> {

}