package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticBillDTO {
    private String createDate;
    private Long billCount;
    private BigDecimal totalStatistic;

}
