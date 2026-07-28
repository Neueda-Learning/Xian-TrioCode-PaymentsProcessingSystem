package org.hsbc.triocodebackend.model.dto;

import org.hsbc.triocodebackend.common.constants.Constants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateReqDTO {

    @NotBlank(message = "订单号不能为空")
    @Size(max = 32, message = "订单号长度不能超过32位")
    private String paymentNo;

    @NotNull(message = "付款账户ID不能为空")
    @Positive(message = "付款账户ID必须为正数")
    private Long sourceAccountId;

    @NotNull(message = "收款账户ID不能为空")
    @Positive(message = "收款账户ID必须为正数")
    private Long destinationAccountId;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "金额必须大于0")
    @DecimalMax(value = "1000000", inclusive = false, message = "金额必须小于1000000")
    @Digits(integer = 7, fraction = Constants.Payment.CURRENCY_SCALE, message = "金额最多保留2位小数")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    @Pattern(regexp = Constants.Regex.CURRENCY, message = "币种格式不正确，须为3位大写字母")
    private String currency;

    @Size(max = 128, message = "备注长度不能超过128位")
    private String reference;
}

