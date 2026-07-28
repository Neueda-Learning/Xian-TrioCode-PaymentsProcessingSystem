package org.hsbc.triocodebackend.dto;

import lombok.Data;

@Data
public class PaymentActionReqDTO {
    /** 状态变更备注，保存到 payment_status_history.reference */
    private String reference;
}
