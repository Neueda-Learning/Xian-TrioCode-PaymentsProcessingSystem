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
public class PaymentListItemVO {
    private Long paymentId;
    private String paymentNo;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String createdAt;
}
