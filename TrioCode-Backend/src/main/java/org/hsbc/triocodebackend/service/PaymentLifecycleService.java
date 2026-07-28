package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.vo.PaymentDetailVO;

public interface PaymentLifecycleService {

    /** 接口5：执行校验 CREATED → VALIDATED（失败则 → FAILED） */
    PaymentDetailVO validatePayment(Long paymentId, String reference);

    /** 接口6：执行发送 VALIDATED → SENT（模拟失败则 → FAILED） */
    PaymentDetailVO sendPayment(Long paymentId, String reference);

    /** 接口7：执行完成 SENT → COMPLETED（模拟失败则 → FAILED） */
    PaymentDetailVO completePayment(Long paymentId, String reference);

    /** 接口8：手工置失败 CREATED/VALIDATED/SENT → FAILED */
    PaymentDetailVO failPayment(Long paymentId, String errorCode, String errorMessage, String reference);
}
