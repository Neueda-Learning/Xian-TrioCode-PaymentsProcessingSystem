package org.hsbc.triocodebackend.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class PaymentListQueryDTO {

    private String status;
    private String paymentNo;
    private String reference;
    private String currency;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;

    @Min(value = 1, message = "pageNum must be greater than or equal to 1.")
    private Integer pageNum;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1.")
    @Max(value = 100, message = "pageSize cannot exceed 100.")
    private Integer pageSize;
}
