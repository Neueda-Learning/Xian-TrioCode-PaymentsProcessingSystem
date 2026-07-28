package org.hsbc.triocodebackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateReqDTO {

    @NotBlank(message = "支付流水号不能为空")
    @Size(max = 32, message = "支付流水号长度不能超过32位")
    private String paymentNo;

    @NotNull(message = "付款账户ID不能为空")
    @Positive(message = "付款账户ID必须大于0")
    private Long sourceAccountId;

    @NotNull(message = "收款账户ID不能为空")
    @Positive(message = "收款账户ID必须大于0")
    private Long destinationAccountId;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额最小为0.01")
    @DecimalMax(value = "1000000", message = "支付金额最大为1000000")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    @Pattern(regexp = "[A-Z]{3}", message = "币种必须为3位大写字母（如 USD）")
    private String currency;

    @Size(max = 128, message = "支付备注长度不能超过128位")
    private String reference;
}
