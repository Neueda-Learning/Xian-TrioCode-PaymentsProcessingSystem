package org.hsbc.triocodebackend.model.dto;

import org.hsbc.triocodebackend.common.constants.Constants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateReqDTO {

    @NotBlank(message = "The order number is required.")
    @Size(max = 32, message = "The order number cannot exceed 32 characters.")
    private String paymentNo;

    @NotNull(message = "The source account ID is required.")
    @Positive(message = "The source account ID must be a positive number.")
    private Long sourceAccountId;

    @NotNull(message = "The destination account ID is required.")
    @Positive(message = "The destination account ID must be a positive number.")
    private Long destinationAccountId;

    @NotNull(message = "The payment amount is required.")
    @DecimalMin(value = "0", inclusive = false, message = "The amount must be greater than 0.")
    @DecimalMax(value = "1000000", inclusive = false, message = "The amount must be less than 1000000.")
    @Digits(integer = 7, fraction = Constants.Payment.CURRENCY_SCALE, message = "The amount can have at most 2 decimal places.")
    private BigDecimal amount;

    @NotBlank(message = "The currency is required.")
    @Pattern(regexp = Constants.Regex.CURRENCY, message = "The currency format is invalid. Use a 3-letter uppercase code.")
    private String currency;

    @Size(max = 128, message = "The reference cannot exceed 128 characters.")
    private String reference;
}

