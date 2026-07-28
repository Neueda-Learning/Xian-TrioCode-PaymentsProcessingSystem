package org.hsbc.triocodebackend.controller;

import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.model.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.model.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.model.vo.PaymentHistoryVO;
import org.hsbc.triocodebackend.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付接口 —— 单一入口。
 *
 * <p>前端只需调用 POST /api/v1/payments 即可。
 * 后端自动完成创建、校验、发送、清算全流程，并返回最终状态。
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 提交支付。
     *
     * <ul>
     *   <li>幂等：相同 paymentNo 重复提交直接返回已有记录。</li>
     *   <li>业务规则校验失败 → status=FAILED，返回 failureCode/failureMessage。</li>
     *   <li>成功 → status=COMPLETED，双方余额已更新。</li>
     * </ul>
     */
    @PostMapping("/payments")
    public Result<PaymentDetailVO> submitPayment(@Validated @RequestBody PaymentCreateReqDTO req) {
        PaymentDetailVO vo = paymentService.submitPayment(req);
        return Result.ok(vo);
    }

    /**
     * 查询单笔支付的完整状态变更历史（时间线）。
     * 不分页，按 created_at 升序返回全量记录。
     */
    @GetMapping("/payments/{paymentId}/histories")
    public Result<List<PaymentHistoryVO>> getHistories(@PathVariable Long paymentId) {
        List<PaymentHistoryVO> list = paymentService.getHistories(paymentId);
        return Result.ok(list);
    }

    /**
     * 按 paymentNo 查询单笔支付的完整状态变更历史（时间线）。
     * 先通过 paymentNo 找到对应的 paymentId，再复用同样的历史查询逻辑。
     * 不分页，按 created_at 升序返回全量记录。
     */
    @GetMapping("/payments/no/{paymentNo}/histories")
    public Result<List<PaymentHistoryVO>> getHistoriesByPaymentNo(@PathVariable String paymentNo) {
        List<PaymentHistoryVO> list = paymentService.getHistoriesByPaymentNo(paymentNo);
        return Result.ok(list);
    }
}



