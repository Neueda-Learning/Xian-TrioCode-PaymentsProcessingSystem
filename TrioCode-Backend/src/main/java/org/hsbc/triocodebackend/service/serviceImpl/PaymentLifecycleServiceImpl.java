package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.enums.PaymentStatusEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.model.Account;
import org.hsbc.triocodebackend.model.AccountBalanceHistory;
import org.hsbc.triocodebackend.model.Payment;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;
import org.hsbc.triocodebackend.repository.AccountBalanceHistoryRepository;
import org.hsbc.triocodebackend.repository.AccountRepository;
import org.hsbc.triocodebackend.repository.PaymentRepository;
import org.hsbc.triocodebackend.repository.PaymentStatusHistoryRepository;
import org.hsbc.triocodebackend.service.PaymentLifecycleService;
import org.hsbc.triocodebackend.service.PaymentService;
import org.hsbc.triocodebackend.vo.PaymentDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentLifecycleServiceImpl implements PaymentLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentLifecycleServiceImpl.class);
    private static final int MAX_RETRY = 3;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStatusHistoryRepository historyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountBalanceHistoryRepository balanceHistoryRepository;

    @Autowired
    private PaymentStateMachine stateMachine;

    @Autowired
    private PaymentRuleChecker ruleChecker;

    @Autowired
    private PaymentService paymentService;

    // ----------------------------------------------------------------
    // 接口5：执行校验 CREATED → VALIDATED
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentDetailVO validatePayment(Long paymentId, String reference) {
        Payment payment = requirePayment(paymentId);
        PaymentStatusEnum current = PaymentStatusEnum.valueOf(payment.getStatus());
        stateMachine.canTransit(current, PaymentStatusEnum.VALIDATED);

        try {
            // 完整业务规则校验（含余额检查）
            ruleChecker.checkNotSameAccount(payment.getSourceAccountId(), payment.getDestinationAccountId());
            Account srcAccount = ruleChecker.checkSourceAccount(payment.getSourceAccountId());
            ruleChecker.checkDestinationAccount(payment.getDestinationAccountId());
            ruleChecker.checkCurrency(payment.getCurrency());
            ruleChecker.checkBalance(srcAccount, payment.getAmount());

            // 状态更新
            LocalDateTime now = LocalDateTime.now();
            int rows = paymentRepository.updateToValidated(paymentId, payment.getVersion(), now);
            if (rows == 0) {
                throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突，请重试");
            }

            historyRepository.insert(buildHistory(paymentId,
                    PaymentStatusEnum.CREATED.name(),
                    PaymentStatusEnum.VALIDATED.name(),
                    reference, null, null));

        } catch (BizException e) {
            // 校验失败：自动流转到 FAILED
            LocalDateTime now = LocalDateTime.now();
            paymentRepository.updateToFailed(paymentId, payment.getVersion(),
                    PaymentStatusEnum.CREATED.name(), now, e.getErrorCode().getCode(), e.getMessage());
            historyRepository.insert(buildHistory(paymentId,
                    PaymentStatusEnum.CREATED.name(),
                    PaymentStatusEnum.FAILED.name(),
                    reference, e.getErrorCode().getCode(), e.getMessage()));
        }

        return paymentService.getPaymentDetail(paymentId);
    }

    // ----------------------------------------------------------------
    // 接口6：执行发送 VALIDATED → SENT（模拟网络失败重试）
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentDetailVO sendPayment(Long paymentId, String reference) {
        Payment payment = requirePayment(paymentId);
        PaymentStatusEnum current = PaymentStatusEnum.valueOf(payment.getStatus());
        stateMachine.canTransit(current, PaymentStatusEnum.SENT);

        boolean success = false;
        String failReason = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            if (simulateNetworkSuccess()) {
                success = true;
                break;
            }
            failReason = "网络通信失败（第" + attempt + "次重试）";
            log.warn("支付 {} 发送失败，第 {} 次重试", paymentId, attempt);
        }

        LocalDateTime now = LocalDateTime.now();
        if (success) {
            int rows = paymentRepository.updateToSent(paymentId, payment.getVersion(), now);
            if (rows == 0) throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突，请重试");
            historyRepository.insert(buildHistory(paymentId,
                    PaymentStatusEnum.VALIDATED.name(),
                    PaymentStatusEnum.SENT.name(),
                    reference, null, null));
        } else {
            paymentRepository.updateToFailed(paymentId, payment.getVersion(),
                    PaymentStatusEnum.VALIDATED.name(), now,
                    ErrorCodeEnum.NETWORK_ERROR.getCode(), failReason);
            historyRepository.insert(buildHistory(paymentId,
                    PaymentStatusEnum.VALIDATED.name(),
                    PaymentStatusEnum.FAILED.name(),
                    reference, ErrorCodeEnum.NETWORK_ERROR.getCode(), failReason));
        }

        return paymentService.getPaymentDetail(paymentId);
    }

    // ----------------------------------------------------------------
    // 接口7：执行完成 SENT → COMPLETED（含余额流水写入）
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentDetailVO completePayment(Long paymentId, String reference) {
        Payment payment = requirePayment(paymentId);
        PaymentStatusEnum current = PaymentStatusEnum.valueOf(payment.getStatus());
        stateMachine.canTransit(current, PaymentStatusEnum.COMPLETED);

        boolean success = false;
        String failReason = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            if (simulateClearingSuccess()) {
                success = true;
                break;
            }
            failReason = "清算失败（第" + attempt + "次重试）";
            log.warn("支付 {} 清算失败，第 {} 次重试", paymentId, attempt);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!success) {
            paymentRepository.updateToFailed(paymentId, payment.getVersion(),
                    PaymentStatusEnum.SENT.name(), now,
                    ErrorCodeEnum.NETWORK_ERROR.getCode(), failReason);
            historyRepository.insert(buildHistory(paymentId,
                    PaymentStatusEnum.SENT.name(),
                    PaymentStatusEnum.FAILED.name(),
                    reference, ErrorCodeEnum.NETWORK_ERROR.getCode(), failReason));
            return paymentService.getPaymentDetail(paymentId);
        }

        // 读取账户余额快照
        Account srcAccount = accountRepository.selectById(payment.getSourceAccountId());
        Account dstAccount = accountRepository.selectById(payment.getDestinationAccountId());
        BigDecimal amount = payment.getAmount();
        BigDecimal srcBefore = srcAccount.getBalance();
        BigDecimal dstBefore = dstAccount.getBalance();

        // 更新支付状态
        int rows = paymentRepository.updateToCompleted(paymentId, payment.getVersion(), now);
        if (rows == 0) throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突，请重试");

        // 更新账户余额
        accountRepository.deductBalance(payment.getSourceAccountId(), amount);
        accountRepository.addBalance(payment.getDestinationAccountId(), amount);

        // 写余额流水（双边）
        balanceHistoryRepository.insert(AccountBalanceHistory.builder()
                .accountId(payment.getSourceAccountId())
                .paymentId(paymentId)
                .operationType("DEBIT")
                .balanceBefore(srcBefore)
                .balanceAfter(srcBefore.subtract(amount))
                .amount(amount)
                .description("支付完成 - 扣款")
                .build());

        balanceHistoryRepository.insert(AccountBalanceHistory.builder()
                .accountId(payment.getDestinationAccountId())
                .paymentId(paymentId)
                .operationType("CREDIT")
                .balanceBefore(dstBefore)
                .balanceAfter(dstBefore.add(amount))
                .amount(amount)
                .description("支付完成 - 入账")
                .build());

        // 写状态历史
        historyRepository.insert(buildHistory(paymentId,
                PaymentStatusEnum.SENT.name(),
                PaymentStatusEnum.COMPLETED.name(),
                reference, null, null));

        return paymentService.getPaymentDetail(paymentId);
    }

    // ----------------------------------------------------------------
    // 接口8：手工置失败
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentDetailVO failPayment(Long paymentId, String errorCode, String errorMessage, String reference) {
        Payment payment = requirePayment(paymentId);
        PaymentStatusEnum current = PaymentStatusEnum.valueOf(payment.getStatus());
        stateMachine.canTransit(current, PaymentStatusEnum.FAILED);

        LocalDateTime now = LocalDateTime.now();
        int rows = paymentRepository.updateToFailedAny(paymentId, payment.getVersion(), now, errorCode, errorMessage);
        if (rows == 0) throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突或状态已变更，请重试");

        historyRepository.insert(buildHistory(paymentId,
                current.name(),
                PaymentStatusEnum.FAILED.name(),
                reference, errorCode, errorMessage));

        return paymentService.getPaymentDetail(paymentId);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Payment requirePayment(Long paymentId) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            throw new BizException(ErrorCodeEnum.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    private PaymentStatusHistory buildHistory(Long paymentId,
                                              String fromStatus,
                                              String toStatus,
                                              String reference,
                                              String errorCode,
                                              String errorMessage) {
        return PaymentStatusHistory.builder()
                .paymentId(paymentId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reference(reference)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /** 模拟网络发送成功概率（80%） */
    private boolean simulateNetworkSuccess() {
        return Math.random() >= 0.2;
    }

    /** 模拟清算成功概率（85%） */
    private boolean simulateClearingSuccess() {
        return Math.random() >= 0.15;
    }
}
