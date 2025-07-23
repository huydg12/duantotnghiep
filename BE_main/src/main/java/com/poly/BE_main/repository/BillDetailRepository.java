package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.BillDetail;

public interface BillDetailRepository extends JpaRepository<BillDetail, Integer>{
    List<BillDetail> findByBillId(Integer billId);
}