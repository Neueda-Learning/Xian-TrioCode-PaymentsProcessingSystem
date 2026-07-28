package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.model.Account;
import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.repository.AccountRepository;
import org.hsbc.triocodebackend.repository.CurrencyDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validates business rules for payments.
 * Can be reused in createPayment and validatePayment flows.
 */
@Component
public class PaymentRuleChecker {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrencyDictRepository currencyDictRepository;

    public Account checkSourceAccount(Long sourceAccountId) {
        Account account = accountRepository.selectById(sourceAccountId);
        if (account == null || account.getStatus() != 1) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT, "付款账户不存在或已禁用");
        }
        return account;
    }

    public Account checkDestinationAccount(Long destinationAccountId) {
        Account account = accountRepository.selectById(destinationAccountId);
        if (account == null || account.getStatus() != 1) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT, "收款账户不存在或已禁用");
        }
        return account;
    }

    public void checkNotSameAccount(Long sourceAccountId, Long destinationAccountId) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BizException(ErrorCodeEnum.INVALID_ACCOUNT, "付款账户与收款账户不能相同");
        }
    }

    public void checkCurrency(String currency) {
        CurrencyDict dict = currencyDictRepository.findEnabledByCode(currency);
        if (dict == null) {
            throw new BizException(ErrorCodeEnum.INVALID_CURRENCY, "不支持的币种: " + currency);
        }
    }

    public void checkBalance(Account sourceAccount, BigDecimal amount) {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new BizException(ErrorCodeEnum.INSUFFICIENT_FUNDS,
                    "账户余额不足，当前余额: " + sourceAccount.getBalance() + "，需要: " + amount);
        }
    }

    public void checkAmountScale(BigDecimal amount) {
        if (amount.scale() > 2) {
            throw new BizException(ErrorCodeEnum.INVALID_AMOUNT, "支付金额最多保留2位小数");
        }
    }
}
