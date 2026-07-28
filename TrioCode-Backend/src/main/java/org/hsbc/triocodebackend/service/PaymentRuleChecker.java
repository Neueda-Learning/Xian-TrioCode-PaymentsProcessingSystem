package org.hsbc.triocodebackend.service;

import lombok.RequiredArgsConstructor;
import org.hsbc.triocodebackend.common.constants.Constants;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.mapper.AccountMapper;
import org.hsbc.triocodebackend.mapper.CurrencyDictMapper;
import org.hsbc.triocodebackend.model.Account;
import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.model.dto.PaymentCreateReqDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付业务规则校验器：账户/币种/余额校验，可被多个 Service 复用。
 */
@Component
@RequiredArgsConstructor
public class PaymentRuleChecker {

    private final AccountMapper accountMapper;
    private final CurrencyDictMapper currencyDictMapper;

    /**
     * 执行全部业务规则校验，校验失败则抛出对应 BizException。
     */
    public void check(PaymentCreateReqDTO req) {
        // 0. 金额规则：>0、<MAX_AMOUNT、最多2位小数
        BigDecimal amount = req.getAmount();
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.compareTo(Constants.Payment.MAX_AMOUNT_DECIMAL) >= 0
                || amount.scale() > Constants.Payment.CURRENCY_SCALE) {
            throw new BizException(ErrorCodeEnum.INVALID_AMOUNT,
                    "The amount must be greater than 0 and less than " + Constants.Payment.MAX_AMOUNT
                            + ", with up to " + Constants.Payment.CURRENCY_SCALE + " decimal places.");
        }

        // 1. 付款账户与收款账户不能相同
        if (req.getSourceAccountId().equals(req.getDestinationAccountId())) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT,
                    "The source and destination accounts cannot be the same.");
        }

        // 2. 校验币种是否支持
        CurrencyDict currency = currencyDictMapper.selectByCode(req.getCurrency());
        if (currency == null) {
            throw new BizException(ErrorCodeEnum.INVALID_CURRENCY, "Unsupported currency: " + req.getCurrency());
        }

        // 3. 校验付款账户
        Account source = accountMapper.selectById(req.getSourceAccountId());
        if (source == null || source.getStatus() != 1) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT,
                    "The source account does not exist or is disabled.");
        }

        // 4. 校验收款账户
        Account destination = accountMapper.selectById(req.getDestinationAccountId());
        if (destination == null || destination.getStatus() != 1) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT,
                    "The destination account does not exist or is disabled.");
        }

        // 5. 校验余额是否充足
        if (source.getBalance().compareTo(req.getAmount()) < 0) {
            throw new BizException(ErrorCodeEnum.INSUFFICIENT_FUNDS,
                    "Insufficient balance. Current balance: " + source.getBalance()
                            + ", required amount: " + req.getAmount());
        }
    }
}
