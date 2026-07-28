package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.model.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.model.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.model.vo.PaymentHistoryVO;

import java.util.List;

public interface PaymentService {

    /**
     * 提交支付请求。
     * 后端自动完成：CREATED → VALIDATED → SENT → COMPLETED（或 FAILED）。
     * 支持幂等：同一 paymentNo 重复提交返回已有记录。
     */
    PaymentDetailVO submitPayment(PaymentCreateReqDTO req);

    /**
     * 查询单笔支付的完整状态变更历史，按 created_at 升序，不分页。
     */
    List<PaymentHistoryVO> getHistories(Long paymentId);

    /**
     * 按 paymentNo 查询单笔支付的完整状态变更历史。
     * 先通过 paymentNo 定位 paymentId，再复用 getHistories 的逻辑。
     */
    List<PaymentHistoryVO> getHistoriesByPaymentNo(String paymentNo);
}

