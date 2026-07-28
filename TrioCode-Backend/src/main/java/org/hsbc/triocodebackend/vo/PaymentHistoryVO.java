package org.hsbc.triocodebackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryVO {
    private Long historyId;
    private String fromStatus;
    private String toStatus;
    private String reference;
    private String errorCode;
    private String errorMessage;
    private String createdAt;
}
