package com.poly.BE_main.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BILL")
public class Bill {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="ID")
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

    public Bill() {
    }

    public Bill(int id, Integer customerId, Integer employeeId, Integer ptttId, String code, String billType,
            int status, String createdBy, LocalDate createdDate, LocalDate shippingDate, LocalDate dateOfPayment,
            String recipientName, String recipientPhoneNumber, String receiverAddress, String addressMethod,
            LocalDate estimatedDeliveryDate, String modifiedBy, LocalDate modifiedDate, String note,
            String statusPayment, BigDecimal subTotal, BigDecimal discountAmount, BigDecimal shippingFee,
            BigDecimal grandTotal) {
        this.id = id;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.ptttId = ptttId;
        this.code = code;
        this.billType = billType;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.shippingDate = shippingDate;
        this.dateOfPayment = dateOfPayment;
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.receiverAddress = receiverAddress;
        this.addressMethod = addressMethod;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.note = note;
        this.statusPayment = statusPayment;
        this.subTotal = subTotal;
        this.discountAmount = discountAmount;
        this.shippingFee = shippingFee;
        this.grandTotal = grandTotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getPtttId() {
        return ptttId;
    }

    public void setPtttId(Integer ptttId) {
        this.ptttId = ptttId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }

    public LocalDate getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(LocalDate dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getAddressMethod() {
        return addressMethod;
    }

    public void setAddressMethod(String addressMethod) {
        this.addressMethod = addressMethod;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatusPayment() {
        return statusPayment;
    }

    public void setStatusPayment(String statusPayment) {
        this.statusPayment = statusPayment;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

}