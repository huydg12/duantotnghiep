package com.poly.BE_main.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="PROMOTION_USAGE", schema="dbo", uniqueConstraints=@UniqueConstraint(columnNames="BILL_ID"))
@Data @NoArgsConstructor @AllArgsConstructor
public class PromotionUsage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ID") private Integer id;
    @Column(name="PROMOTION_ID", nullable=false) private Integer promotionId;
    @Column(name="CUSTOMER_ID", nullable=false) private Integer customerId;
    @Column(name="BILL_ID", nullable=false) private Integer billId;
    @Column(name="USED_AT", nullable=false) private LocalDateTime usedAt;
}
