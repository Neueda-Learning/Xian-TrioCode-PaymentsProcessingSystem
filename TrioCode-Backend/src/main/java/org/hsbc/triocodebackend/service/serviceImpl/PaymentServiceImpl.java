package org.hsbc.triocodebackend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsbc.triocodebackend.common.constants.Constants;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.enums.PaymentStatusEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.mapper.AccountBalanceHistoryMapper;
import org.hsbc.triocodebackend.mapper.AccountMapper;
import org.hsbc.triocodebackend.mapper.PaymentMapper;
import org.hsbc.triocodebackend.mapper.PaymentStatusHistoryMapper;
import org.hsbc.triocodebackend.model.Account;
import org.hsbc.triocodebackend.model.AccountBalanceHistory;
import org.hsbc.triocodebackend.model.Payment;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;
import org.hsbc.triocodebackend.model.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.model.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.service.PaymentRuleChecker;
import org.hsbc.triocodebackend.service.PaymentService;
import org.hsbc.triocodebackend.service.PaymentStateMachine;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 支付核心业务实现。
 *
 * <p>前端只需调用一次 submitPayment，后端在同一事务内自动完成：
 * <ol>
 *   <li>幂等判断</li>
 *   <li>创建支付记录（CREATED）</li>
 *   <li>业务规则校验（→ VALIDATED 或 FAILED）</li>
 *   <li>模拟发送（→ SENT 或 FAILED，含最多3次重试）</li>
 *   <li>模拟清算（→ COMPLETED 或 FAILED）</li>
 *   <li>完成时：更新双方余额 + 记录余额流水</li>
 * </ol>
 * 每一次状态流转均通过状态机白名单校验，并写入 payment_status_history。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final AccountMapper accountMapper;
    private final PaymentStatusHistoryMapper historyMapper;
    private final AccountBalanceHistoryMapper balanceHistoryMapper;
    private final PaymentRuleChecker ruleChecker;
    private final PaymentStateMachine stateMachine;

    private static final int MAX_SEND_RETRIES = Constants.Payment.MAX_SEND_RETRIES;

    // ----------------------------------------------------------------
    // 公开接口
    // ----------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public PaymentDetailVO submitPayment(PaymentCreateReqDTO req) {
        // 统一金额精度到系统约定的 2 位小数。
        req.setAmount(req.getAmount().setScale(Constants.Payment.CURRENCY_SCALE, RoundingMode.HALF_UP));

        // ① 幂等判断：同一 paymentNo 直接返回已有记录
        Payment existing = paymentMapper.selectByPaymentNo(req.getPaymentNo());
        if (existing != null) {
            log.info("[Payment] 幂等命中，paymentNo={}, status={}", req.getPaymentNo(), existing.getStatus());
            return toVO(existing);
        }

        // ② 创建支付记录（CREATED）
        Payment payment = Payment.builder()
                .paymentNo(req.getPaymentNo())
                .sourceAccountId(req.getSourceAccountId())
                .destinationAccountId(req.getDestinationAccountId())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .reference(req.getReference())
                .status(PaymentStatusEnum.CREATED.name())
                .version(0)
                .build();
        try {
            paymentMapper.insert(payment);
        } catch (DataIntegrityViolationException e) {
            // 极端并发：唯一索引冲突，返回已有记录
            Payment dup = paymentMapper.selectByPaymentNo(req.getPaymentNo());
            return toVO(dup);
        }
        insertHistory(payment.getId(), null, PaymentStatusEnum.CREATED.name(), "支付创建", null, null);
        log.info("[Payment] 创建成功, id={}, paymentNo={}", payment.getId(), payment.getPaymentNo());

        // ③ 业务规则校验 → VALIDATED 或 FAILED
        try {
            ruleChecker.check(req);
        } catch (BizException e) {
            log.warn("[Payment] 业务校验失败, id={}, reason={}", payment.getId(), e.getMessage());
            failPayment(payment, PaymentStatusEnum.CREATED.name(),
                    e.getErrorCode().getCode(), e.getMessage());
            throw e;
        }
        transitionTo(payment, PaymentStatusEnum.CREATED.name(), PaymentStatusEnum.VALIDATED.name(),
                p -> p.setValidatedAt(LocalDateTime.now()), "业务校验通过");

        // ④ 模拟发送（含重试）→ SENT 或 FAILED
        boolean sendOk = false;
        for (int attempt = 1; attempt <= MAX_SEND_RETRIES; attempt++) {
            try {
                simulateSend();
                sendOk = true;
                break;
            } catch (BizException e) {
                log.warn("[Payment] 发送尝试 {}/{} 失败, id={}, reason={}",
                        attempt, MAX_SEND_RETRIES, payment.getId(), e.getMessage());
                if (attempt == MAX_SEND_RETRIES) {
                    failPayment(payment, PaymentStatusEnum.VALIDATED.name(),
                            e.getErrorCode().getCode(), e.getMessage());
                    throw e;
                }
            }
        }
        if (sendOk) {
            transitionTo(payment, PaymentStatusEnum.VALIDATED.name(), PaymentStatusEnum.SENT.name(),
                    p -> p.setSentAt(LocalDateTime.now()), "发送成功");
        }

        // ⑤ 模拟清算 → COMPLETED 或 FAILED
        try {
            simulateComplete();
        } catch (BizException e) {
            log.warn("[Payment] 清算失败, id={}, reason={}", payment.getId(), e.getMessage());
            failPayment(payment, PaymentStatusEnum.SENT.name(),
                    e.getErrorCode().getCode(), e.getMessage());
            throw e;
        }
        transitionTo(payment, PaymentStatusEnum.SENT.name(), PaymentStatusEnum.COMPLETED.name(),
                p -> p.setCompletedAt(LocalDateTime.now()), "清算完成");

        // ⑥ 完成：更新双方余额 + 写余额流水
        recordBalanceHistory(payment);

        log.info("[Payment] 支付完成, id={}, paymentNo={}", payment.getId(), payment.getPaymentNo());
        return toVO(paymentMapper.selectById(payment.getId()));
    }

    // ----------------------------------------------------------------
    // 私有：状态流转
    // ----------------------------------------------------------------

    /**
     * 通用状态流转：状态机校验 → 更新数据库（乐观锁）→ 写历史。
     */
    private void transitionTo(Payment payment, String fromStatus, String toStatus,
                               Consumer<Payment> timestampSetter, String reference) {
        stateMachine.checkTransition(fromStatus, toStatus);
        payment.setStatus(toStatus);
        timestampSetter.accept(payment);

        int rows = paymentMapper.updateStatusWithLock(payment, fromStatus);
        if (rows == 0) {
            throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突，状态更新失败");
        }
        payment.setVersion(payment.getVersion() + 1); // 本地版本号同步

        insertHistory(payment.getId(), fromStatus, toStatus, reference, null, null);
        log.info("[Payment] 状态流转 {} -> {}, id={}", fromStatus, toStatus, payment.getId());
    }

    /**
     * 流转到 FAILED：记录失败码和失败时间。
     */
    private void failPayment(Payment payment, String fromStatus,
                             String failureCode, String failureMessage) {
        stateMachine.checkTransition(fromStatus, PaymentStatusEnum.FAILED.name());
        payment.setStatus(PaymentStatusEnum.FAILED.name());
        payment.setFailureCode(failureCode);
        payment.setFailureMessage(failureMessage);
        payment.setFailedAt(LocalDateTime.now());

        int rows = paymentMapper.updateStatusWithLock(payment, fromStatus);
        if (rows == 0) {
            throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "并发冲突，FAILED状态更新失败");
        }
        payment.setVersion(payment.getVersion() + 1);

        insertHistory(payment.getId(), fromStatus, PaymentStatusEnum.FAILED.name(),
                null, failureCode, failureMessage);
        log.info("[Payment] 流转到 FAILED, id={}, code={}", payment.getId(), failureCode);
    }

    private void insertHistory(Long paymentId, String fromStatus, String toStatus,
                                String reference, String errorCode, String errorMessage) {
        historyMapper.insert(PaymentStatusHistory.builder()
                .paymentId(paymentId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reference(reference)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build());
    }

    // ----------------------------------------------------------------
    // 私有：余额流水
    // ----------------------------------------------------------------

    /**
     * 支付完成时：FOR UPDATE 加锁双方账户 → 扣/入余额 → 写余额流水（2条）。
     * 锁定顺序按 ID 从小到大，防止死锁。
     */
    private void recordBalanceHistory(Payment payment) {
        Long srcId = payment.getSourceAccountId();
        Long dstId = payment.getDestinationAccountId();
        BigDecimal amount = payment.getAmount();

        // 按 ID 顺序加行锁，避免死锁
        Account source, destination;
        if (srcId < dstId) {
            source      = accountMapper.selectByIdForUpdate(srcId);
            destination = accountMapper.selectByIdForUpdate(dstId);
        } else {
            destination = accountMapper.selectByIdForUpdate(dstId);
            source      = accountMapper.selectByIdForUpdate(srcId);
        }

        BigDecimal srcBefore = source.getBalance();
        BigDecimal dstBefore = destination.getBalance();
        BigDecimal srcAfter  = srcBefore.subtract(amount).setScale(Constants.Payment.CURRENCY_SCALE, RoundingMode.HALF_UP);
        BigDecimal dstAfter  = dstBefore.add(amount).setScale(Constants.Payment.CURRENCY_SCALE, RoundingMode.HALF_UP);

        // 更新余额
        accountMapper.updateBalance(srcId, srcAfter);
        accountMapper.updateBalance(dstId, dstAfter);

        // 付款方：DEBIT
        balanceHistoryMapper.insert(AccountBalanceHistory.builder()
                .accountId(srcId)
                .paymentId(payment.getId())
                .operationType(Constants.Payment.OPERATION_DEBIT)
                .balanceBefore(srcBefore)
                .balanceAfter(srcAfter)
                .amount(amount)
                .description("Payment " + payment.getPaymentNo())
                .build());

        // 收款方：CREDIT
        balanceHistoryMapper.insert(AccountBalanceHistory.builder()
                .accountId(dstId)
                .paymentId(payment.getId())
                .operationType(Constants.Payment.OPERATION_CREDIT)
                .balanceBefore(dstBefore)
                .balanceAfter(dstAfter)
                .amount(amount)
                .description("Payment " + payment.getPaymentNo())
                .build());
    }

    // ----------------------------------------------------------------
    // 私有：模拟发送/清算（可修改概率演示失败路径）
    // ----------------------------------------------------------------

    /**
     * 模拟发送步骤。
     * 当前设置为必定成功（0% 失败率），可将下行注释打开演示失败：
     *   if (RANDOM.nextInt(10) == 0) throw new BizException(ErrorCodeEnum.NETWORK_ERROR);
     */
    private void simulateSend() {
        // if (RANDOM.nextInt(10) == 0) throw new BizException(ErrorCodeEnum.NETWORK_ERROR);
    }

    /**
     * 模拟清算步骤。
     * 当前设置为必定成功，可将下行注释打开演示失败：
     *   if (RANDOM.nextInt(20) == 0) throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "清算系统暂时不可用");
     */
    private void simulateComplete() {
        // if (RANDOM.nextInt(20) == 0) throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "清算系统暂时不可用");
    }

    // ----------------------------------------------------------------
    // 私有：VO 转换
    // ----------------------------------------------------------------

    private PaymentDetailVO toVO(Payment p) {
        PaymentDetailVO vo = new PaymentDetailVO();
        vo.setPaymentId(p.getId());
        vo.setPaymentNo(p.getPaymentNo());
        vo.setSourceAccountId(p.getSourceAccountId());
        vo.setDestinationAccountId(p.getDestinationAccountId());
        vo.setAmount(p.getAmount());
        vo.setCurrency(p.getCurrency());
        vo.setReference(p.getReference());
        vo.setStatus(p.getStatus());
        vo.setFailureCode(p.getFailureCode());
        vo.setFailureMessage(p.getFailureMessage());
        vo.setValidatedAt(p.getValidatedAt());
        vo.setSentAt(p.getSentAt());
        vo.setCompletedAt(p.getCompletedAt());
        vo.setFailedAt(p.getFailedAt());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());
        return vo;
    }
}

