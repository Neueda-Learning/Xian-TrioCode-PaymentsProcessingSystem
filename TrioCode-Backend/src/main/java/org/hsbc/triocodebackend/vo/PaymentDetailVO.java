package org.hsbc.triocodebackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailVO {
    private Long paymentId;
    private String paymentNo;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String status;
    private String failureCode;
    private String failureMessage;
    private String validatedAt;
    private String sentAt;
    private String completedAt;
    private String failedAt;
    private String createdAt;
    private String updatedAt;
}
