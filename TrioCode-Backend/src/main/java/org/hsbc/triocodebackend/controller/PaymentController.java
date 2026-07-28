package org.hsbc.triocodebackend.controller;

import jakarta.validation.Valid;
import org.hsbc.triocodebackend.common.result.PageResult;
import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.dto.PaymentListQueryDTO;
import org.hsbc.triocodebackend.service.PaymentService;
import org.hsbc.triocodebackend.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.vo.PaymentFailureVO;
import org.hsbc.triocodebackend.vo.PaymentHistoryVO;
import org.hsbc.triocodebackend.vo.PaymentListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 接口1：创建支付
     * POST /api/v1/payments
     */
    @PostMapping
    public Result<PaymentDetailVO> createPayment(@Valid @RequestBody PaymentCreateReqDTO req) {
        return Result.ok(paymentService.createPayment(req));
    }

    /**
     * 接口2：查询支付详情
     * GET /api/v1/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public Result<PaymentDetailVO> getPaymentDetail(@PathVariable Long paymentId) {
        return Result.ok(paymentService.getPaymentDetail(paymentId));
    }

    /**
     * 接口3：分页查询支付列表
     * GET /api/v1/payments
     */
    @GetMapping
    public Result<PageResult<PaymentListItemVO>> listPayments(PaymentListQueryDTO query) {
        return Result.ok(paymentService.listPayments(query));
    }

    /**
     * 接口4：查询状态历史
     * GET /api/v1/payments/{paymentId}/histories
     */
    @GetMapping("/{paymentId}/histories")
    public Result<List<PaymentHistoryVO>> getPaymentHistories(@PathVariable Long paymentId) {
        return Result.ok(paymentService.getPaymentHistories(paymentId));
    }

    /**
     * 接口9：查询失败详情
     * GET /api/v1/payments/{paymentId}/failure
     */
    @GetMapping("/{paymentId}/failure")
    public Result<PaymentFailureVO> getPaymentFailure(@PathVariable Long paymentId) {
        return Result.ok(paymentService.getPaymentFailure(paymentId));
    }
}
