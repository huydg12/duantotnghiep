package com.poly.BE_main.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.poly.BE_main.model.ImportReceipt;
import com.poly.BE_main.repository.ImportReceiptRepository;

@Service
public class ImportReceiptService {
    @Autowired
    ImportReceiptRepository importReceiptRepository;

    public List<ImportReceipt> findALL(){
        return importReceiptRepository.findAll();
    }

    public ImportReceipt create(ImportReceipt i){
        return importReceiptRepository.save(i);
    }

    public void delete(Integer id){
        importReceiptRepository.deleteById(id);
    }

    public ImportReceipt update(int id, ImportReceipt iUpdate){
        return importReceiptRepository.findById(id).map(i ->{
            i.setImportReceiptCode(iUpdate.getImportReceiptCode());
            i.setEmployeeId(iUpdate.getEmployeeId());
            i.setImportDate(iUpdate.getImportDate());
            i.setTotalAmount(iUpdate.getTotalAmount());
            i.setNote(iUpdate.getNote());
            i.setStatus(iUpdate.getStatus());
            i.setCreatedDate(iUpdate.getCreatedDate());
            return importReceiptRepository.save(i);
        }).orElseThrow(()-> new RuntimeException("Không tìm thấy phiếu nhập có id:" + id));
    }
}
