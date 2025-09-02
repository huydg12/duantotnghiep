package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.StatisticBillDTO;
import com.poly.BE_main.dto.StatisticTop5SPDTO;
import com.poly.BE_main.repository.BillDetailRepository;
import com.poly.BE_main.repository.BillRepository;

@Service
public class StatisticService {
        @Autowired
        private BillRepository billRepository;

        @Autowired
        private BillDetailRepository billDetailRepository;

        public List<StatisticBillDTO> getDailyRevenue() {
                return billRepository.revenueByDayPaid()
                                .stream()
                                .map(row -> new StatisticBillDTO(
                                                (String) row[0],
                                                row[1] == null ? 0L : ((Number) row[1]).longValue(), // COUNT(*) ->
                                                                                                     // Number -> long
                                                (java.math.BigDecimal) row[2]))
                                .toList();
        }

        public List<StatisticTop5SPDTO> getTop5BestSellingProductsPaid() {
                List<Object[]> results = billDetailRepository.findTop5BestSellingProductsPaid();

                return results.stream()
                                .map(row -> new StatisticTop5SPDTO(
                                                (String) row[0], // productName
                                                ((Number) row[1]).longValue() // totalSold
                                ))
                                .toList();
        }

}