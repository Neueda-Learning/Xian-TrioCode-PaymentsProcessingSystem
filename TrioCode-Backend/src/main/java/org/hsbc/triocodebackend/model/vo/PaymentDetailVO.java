package org.hsbc.triocodebackend.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private LocalDateTime validatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

