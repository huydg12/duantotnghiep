package com.poly.BE_main.repository;

import com.poly.BE_main.model.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, Integer> {
    List<ImportReceiptDetail> findByImportReceiptId(Integer importReceiptId);
}
