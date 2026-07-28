package org.hsbc.triocodebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
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
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


