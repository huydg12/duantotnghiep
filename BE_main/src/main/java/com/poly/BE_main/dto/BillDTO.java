package com.poly.BE_main.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private Integer customerId;
    private Integer employeeId;
    private Integer ptttId;
    private Integer promotionId;
    private String billCode;
    private String billType;
    private Integer status;
    private String recipientName;
    private String recipientPhoneNumber;
    private String receiverAddress;
    private String addressMethod;
    private String note;
    private String statusPayment;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
    @NotEmpty
    private List<@Valid BillDetailDTO> billDetails;
}
