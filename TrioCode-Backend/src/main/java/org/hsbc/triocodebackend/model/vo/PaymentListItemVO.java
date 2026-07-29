package org.hsbc.triocodebackend.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentListItemVO {

    private Long paymentId;
    private String paymentNo;
    private Long sourceAccountId;
    private String sourceAccountName;
    private Long destinationAccountId;
    private String destinationAccountName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
