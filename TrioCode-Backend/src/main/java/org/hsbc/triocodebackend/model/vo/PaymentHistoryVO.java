package org.hsbc.triocodebackend.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentHistoryVO {

    private Long historyId;
    private String fromStatus;
    private String toStatus;
    private String reference;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime createdAt;
}
