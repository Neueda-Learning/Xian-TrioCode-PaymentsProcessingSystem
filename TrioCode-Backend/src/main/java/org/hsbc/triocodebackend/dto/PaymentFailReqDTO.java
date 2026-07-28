package org.hsbc.triocodebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentFailReqDTO {

    @NotBlank(message = "错误码不能为空")
    private String errorCode;

    @NotBlank(message = "错误描述不能为空")
    private String errorMessage;

    private String reference;
}
