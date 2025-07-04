package com.poly.BE_main.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BILL", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "PTTT_ID")
    private Integer ptttId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "BILL_TYPE")
    private String billType;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "SHIPPING_DATE")
    private LocalDate shippingDate;

    @Column(name = "DATE_OF_PAYMENT")
    private LocalDate dateOfPayment;

    @Column(name = "RECIPIENT_NAME")
    private String recipientName;

    @Column(name = "RECIPIENT_PHONE_NUMBER")
    private String recipientPhoneNumber;

    @Column(name = "RECEIVER_ADDRESS")
    private String receiverAddress;

    @Column(name = "ADDRESS_METHOD")
    private String addressMethod;

    @Column(name = "ESTIMATED_DELIVERY_DATE")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @Column(name = "MODIFIED_DATE")
    private LocalDate modifiedDate;

    @Column(name = "NOTE", columnDefinition = "text")
    private String note;

    @Column(name = "STATUS_PAYMENT")
    private String statusPayment;

    @Column(name = "SUB_TOTAL")
    private BigDecimal subTotal;

    @Column(name = "DISCOUNT_AMOUNT")
    private BigDecimal discountAmount;

    @Column(name = "SHIPPING_FEE")
    private BigDecimal shippingFee;

    @Column(name = "GRAND_TOTAL")
    private BigDecimal grandTotal;

}