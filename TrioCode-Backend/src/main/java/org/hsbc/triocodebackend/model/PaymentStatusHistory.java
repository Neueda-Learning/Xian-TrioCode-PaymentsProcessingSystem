package org.hsbc.triocodebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusHistory {
    private Long id;
    private Long paymentId;
    private String fromStatus;
    private String toStatus;
    private String reference;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime createdAt;
}


