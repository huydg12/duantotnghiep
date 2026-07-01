package com.poly.BE_main.repository;

import com.poly.BE_main.model.BillStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillStatusHistoryRepository extends JpaRepository<BillStatusHistory, Integer> {
    List<BillStatusHistory> findByBillIdOrderByChangedAtAsc(Integer billId);
}
