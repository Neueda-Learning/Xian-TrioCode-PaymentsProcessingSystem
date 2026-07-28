package org.hsbc.triocodebackend.dto;

import lombok.Data;

@Data
public class PaymentListQueryDTO {
    private String status;
    private String paymentNo;
    private String reference;
    private String currency;
    /** 创建时间起（ISO8601），如 2024-01-01T00:00:00 */
    private String createdFrom;
    /** 创建时间止（ISO8601），如 2024-12-31T23:59:59 */
    private String createdTo;
    private int pageNum = 1;
    private int pageSize = 10;
}
