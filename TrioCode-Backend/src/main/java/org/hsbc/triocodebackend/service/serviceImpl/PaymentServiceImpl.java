package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.enums.PaymentStatusEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.common.result.PageResult;
import org.hsbc.triocodebackend.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.dto.PaymentListQueryDTO;
import org.hsbc.triocodebackend.model.Payment;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;
import org.hsbc.triocodebackend.repository.PaymentRepository;
import org.hsbc.triocodebackend.repository.PaymentStatusHistoryRepository;
import org.hsbc.triocodebackend.service.PaymentService;
import org.hsbc.triocodebackend.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.vo.PaymentFailureVO;
import org.hsbc.triocodebackend.vo.PaymentHistoryVO;
import org.hsbc.triocodebackend.vo.PaymentListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Autowired
    private PaymentRuleChecker ruleChecker;

    // ----------------------------------------------------------------
    // 接口1：创建支付
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public PaymentDetailVO createPayment(PaymentCreateReqDTO req) {
        // 幂等检查：同一 paymentNo 重复提交直接返回已有记录
        Payment existing = paymentRepository.selectByPaymentNo(req.getPaymentNo());
        if (existing != null) {
            return toDetailVO(existing);
        }

        // 精度校验
        ruleChecker.checkAmountScale(req.getAmount());
        // 业务规则校验
        ruleChecker.checkNotSameAccount(req.getSourceAccountId(), req.getDestinationAccountId());
        ruleChecker.checkSourceAccount(req.getSourceAccountId());
        ruleChecker.checkDestinationAccount(req.getDestinationAccountId());
        ruleChecker.checkCurrency(req.getCurrency());

        // 构建并插入支付记录
        Payment payment = Payment.builder()
                .paymentNo(req.getPaymentNo())
                .sourceAccountId(req.getSourceAccountId())
                .destinationAccountId(req.getDestinationAccountId())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .reference(req.getReference())
                .status(PaymentStatusEnum.CREATED.name())
                .build();

        try {
            paymentRepository.insert(payment);
        } catch (Exception e) {
            // 处理唯一键冲突（极端并发场景）
            Payment race = paymentRepository.selectByPaymentNo(req.getPaymentNo());
            if (race != null) {
                return toDetailVO(race);
            }
            throw new BizException(ErrorCodeEnum.PROCESSING_ERROR, "创建支付失败，请重试");
        }

        // 写状态历史
        paymentStatusHistoryRepository.insert(PaymentStatusHistory.builder()
                .paymentId(payment.getId())
                .fromStatus(null)
                .toStatus(PaymentStatusEnum.CREATED.name())
                .reference("支付创建")
                .build());

        return toDetailVO(paymentRepository.selectById(payment.getId()));
    }

    // ----------------------------------------------------------------
    // 接口2：查询支付详情
    // ----------------------------------------------------------------

    @Override
    public PaymentDetailVO getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            throw new BizException(ErrorCodeEnum.PAYMENT_NOT_FOUND);
        }
        return toDetailVO(payment);
    }

    // ----------------------------------------------------------------
    // 接口3：分页查询支付列表
    // ----------------------------------------------------------------

    @Override
    public PageResult<PaymentListItemVO> listPayments(PaymentListQueryDTO query) {
        if (query.getPageSize() > 100) {
            query.setPageSize(100);
        }
        if (query.getPageNum() < 1) {
            query.setPageNum(1);
        }

        long total = paymentRepository.countByQuery(query);
        if (total == 0) {
            return PageResult.empty(query.getPageNum(), query.getPageSize());
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        List<Payment> payments = paymentRepository.selectByQuery(query, offset, query.getPageSize());

        List<PaymentListItemVO> records = payments.stream()
                .map(this::toListItemVO)
                .collect(Collectors.toList());

        return PageResult.of(records, total, query.getPageNum(), query.getPageSize());
    }

    // ----------------------------------------------------------------
    // 接口4：查询状态历史
    // ----------------------------------------------------------------

    @Override
    public List<PaymentHistoryVO> getPaymentHistories(Long paymentId) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            throw new BizException(ErrorCodeEnum.PAYMENT_NOT_FOUND);
        }

        List<PaymentStatusHistory> histories =
                paymentStatusHistoryRepository.selectByPaymentId(paymentId);

        return histories.stream()
                .map(this::toHistoryVO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // 接口9：查询支付失败详情
    // ----------------------------------------------------------------

    @Override
    public PaymentFailureVO getPaymentFailure(Long paymentId) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            throw new BizException(ErrorCodeEnum.PAYMENT_NOT_FOUND);
        }
        if (!PaymentStatusEnum.FAILED.name().equals(payment.getStatus())) {
            throw new BizException(ErrorCodeEnum.VALIDATION_FAILED, "该支付状态不是 FAILED，无失败详情");
        }
        return PaymentFailureVO.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .failureCode(payment.getFailureCode())
                .failureMessage(payment.getFailureMessage())
                .failedAt(format(payment.getFailedAt()))
                .build();
    }

    // ----------------------------------------------------------------
    // Private mapping helpers
    // ----------------------------------------------------------------

    private PaymentDetailVO toDetailVO(Payment p) {
        return PaymentDetailVO.builder()
                .paymentId(p.getId())
                .paymentNo(p.getPaymentNo())
                .sourceAccountId(p.getSourceAccountId())
                .destinationAccountId(p.getDestinationAccountId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .reference(p.getReference())
                .status(p.getStatus())
                .failureCode(p.getFailureCode())
                .failureMessage(p.getFailureMessage())
                .validatedAt(format(p.getValidatedAt()))
                .sentAt(format(p.getSentAt()))
                .completedAt(format(p.getCompletedAt()))
                .failedAt(format(p.getFailedAt()))
                .createdAt(format(p.getCreatedAt()))
                .updatedAt(format(p.getUpdatedAt()))
                .build();
    }

    private PaymentListItemVO toListItemVO(Payment p) {
        return PaymentListItemVO.builder()
                .paymentId(p.getId())
                .paymentNo(p.getPaymentNo())
                .sourceAccountId(p.getSourceAccountId())
                .destinationAccountId(p.getDestinationAccountId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .createdAt(format(p.getCreatedAt()))
                .build();
    }

    private PaymentHistoryVO toHistoryVO(PaymentStatusHistory h) {
        return PaymentHistoryVO.builder()
                .historyId(h.getId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .reference(h.getReference())
                .errorCode(h.getErrorCode())
                .errorMessage(h.getErrorMessage())
                .createdAt(format(h.getCreatedAt()))
                .build();
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : dt.format(FORMATTER);
    }
}
