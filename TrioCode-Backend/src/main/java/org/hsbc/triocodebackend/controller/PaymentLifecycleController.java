package org.hsbc.triocodebackend.controller;

import jakarta.validation.Valid;
import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.dto.PaymentActionReqDTO;
import org.hsbc.triocodebackend.dto.PaymentFailReqDTO;
import org.hsbc.triocodebackend.service.PaymentLifecycleService;
import org.hsbc.triocodebackend.vo.PaymentDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentLifecycleController {

    @Autowired
    private PaymentLifecycleService lifecycleService;

    /**
     * 接口5：执行校验 CREATED → VALIDATED
     * POST /api/v1/payments/{paymentId}/validate
     */
    @PostMapping("/{paymentId}/validate")
    public Result<PaymentDetailVO> validatePayment(@PathVariable Long paymentId,
                                                   @RequestBody(required = false) PaymentActionReqDTO req) {
        String reference = req != null ? req.getReference() : null;
        return Result.ok(lifecycleService.validatePayment(paymentId, reference));
    }

    /**
     * 接口6：执行发送 VALIDATED → SENT
     * POST /api/v1/payments/{paymentId}/send
     */
    @PostMapping("/{paymentId}/send")
    public Result<PaymentDetailVO> sendPayment(@PathVariable Long paymentId,
                                               @RequestBody(required = false) PaymentActionReqDTO req) {
        String reference = req != null ? req.getReference() : null;
        return Result.ok(lifecycleService.sendPayment(paymentId, reference));
    }

    /**
     * 接口7：执行完成 SENT → COMPLETED
     * POST /api/v1/payments/{paymentId}/complete
     */
    @PostMapping("/{paymentId}/complete")
    public Result<PaymentDetailVO> completePayment(@PathVariable Long paymentId,
                                                   @RequestBody(required = false) PaymentActionReqDTO req) {
        String reference = req != null ? req.getReference() : null;
        return Result.ok(lifecycleService.completePayment(paymentId, reference));
    }

    /**
     * 接口8：手工置失败
     * POST /api/v1/payments/{paymentId}/fail
     */
    @PostMapping("/{paymentId}/fail")
    public Result<PaymentDetailVO> failPayment(@PathVariable Long paymentId,
                                               @Valid @RequestBody PaymentFailReqDTO req) {
        return Result.ok(lifecycleService.failPayment(paymentId,
                req.getErrorCode(), req.getErrorMessage(), req.getReference()));
    }
}
