package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.model.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.model.vo.PaymentDetailVO;

public interface PaymentService {

    /**
     * 提交支付请求。
     * 后端自动完成：CREATED → VALIDATED → SENT → COMPLETED（或 FAILED）。
     * 支持幂等：同一 paymentNo 重复提交返回已有记录。
     */
    PaymentDetailVO submitPayment(PaymentCreateReqDTO req);
}

