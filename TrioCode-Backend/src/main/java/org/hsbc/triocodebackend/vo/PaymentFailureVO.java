package org.hsbc.triocodebackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailureVO {
    private Long paymentId;
    private String status;
    private String failureCode;
    private String failureMessage;
    private String failedAt;
}
