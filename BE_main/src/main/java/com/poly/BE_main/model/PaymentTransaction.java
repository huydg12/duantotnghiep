package com.poly.BE_main.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="PAYMENT_TRANSACTION", schema="dbo", uniqueConstraints={
    @UniqueConstraint(columnNames="PROVIDER_ORDER_ID"), @UniqueConstraint(columnNames="PROVIDER_TRANSACTION_ID")})
@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ID") private Integer id;
    @Column(name="BILL_ID", nullable=false) private Integer billId;
    @Column(name="PROVIDER", nullable=false, length=30) private String provider;
    @Column(name="PROVIDER_ORDER_ID", nullable=false, length=100) private String providerOrderId;
    @Column(name="PROVIDER_TRANSACTION_ID", length=100) private String providerTransactionId;
    @Column(name="AMOUNT", nullable=false, precision=18, scale=2) private BigDecimal amount;
    @Column(name="STATUS", nullable=false, length=30) private String status;
    @Column(name="CREATED_AT", nullable=false) private LocalDateTime createdAt;
    @Column(name="COMPLETED_AT") private LocalDateTime completedAt;
}
