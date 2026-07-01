package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCustomerDTO {
    int id;
    String code;
    String recipientName;
    String recipientNumberPhone;
    String receiverAddress;
    LocalDate date;
    String status;
    BigDecimal total;
    List<InvoiceItemCustomerDTO> items;
}
