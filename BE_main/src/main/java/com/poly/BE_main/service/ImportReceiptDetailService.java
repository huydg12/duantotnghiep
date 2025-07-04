package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.poly.BE_main.model.ImportReceiptDetail;
import com.poly.BE_main.repository.ImportReceiptDetailRepository;

@Service
public class ImportReceiptDetailService {
    
    @Autowired
    ImportReceiptDetailRepository importReceiptDetailRepository;

    public List<ImportReceiptDetail> findAll(){
        return importReceiptDetailRepository.findAll();
    }

    public ImportReceiptDetail create(ImportReceiptDetail i){
        return importReceiptDetailRepository.save(i);
    }

    public void delete(Integer id){
         importReceiptDetailRepository.deleteById(id);
    }

    public ImportReceiptDetail update(int id,ImportReceiptDetail iUpdate){
        return importReceiptDetailRepository.findById(id).map(i ->{
            i.setImportReceiptId(iUpdate.getImportReceiptId());
            i.setProductDetailId(iUpdate.getProductDetailId());
            i.setQuantity(iUpdate.getQuantity());
            i.setUnitPrice(iUpdate.getUnitPrice());
            i.setTotalPrice(iUpdate.getTotalPrice());
            i.setStatus(iUpdate.getStatus());
            return importReceiptDetailRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập chi tiết có id:" + id));
    }
}
