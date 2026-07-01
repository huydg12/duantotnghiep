package com.poly.BE_main.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.poly.BE_main.model.BillStatusHistory;
public interface BillStatusHistoryRepository extends JpaRepository<BillStatusHistory, Integer> {
    List<BillStatusHistory> findByBillIdOrderByChangedAtAsc(Integer billId);
}
