package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Integer> {

}