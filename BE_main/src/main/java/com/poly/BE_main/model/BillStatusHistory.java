package com.poly.BE_main.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "BILL_STATUS_HISTORY", schema = "dbo")
@Data @NoArgsConstructor @AllArgsConstructor
public class BillStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ID") private Integer id;
    @Column(name = "BILL_ID", nullable = false) private Integer billId;
    @Column(name = "FROM_STATUS") private Integer fromStatus;
    @Column(name = "TO_STATUS", nullable = false) private Integer toStatus;
    @Column(name = "CHANGED_BY", nullable = false, length = 255) private String changedBy;
    @Column(name = "NOTE", length = 500) private String note;
    @Column(name = "CHANGED_AT", nullable = false) private LocalDateTime changedAt;
}
