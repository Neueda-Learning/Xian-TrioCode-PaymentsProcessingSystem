package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.common.result.PageResult;
import org.hsbc.triocodebackend.dto.PaymentCreateReqDTO;
import org.hsbc.triocodebackend.dto.PaymentListQueryDTO;
import org.hsbc.triocodebackend.vo.PaymentDetailVO;
import org.hsbc.triocodebackend.vo.PaymentFailureVO;
import org.hsbc.triocodebackend.vo.PaymentHistoryVO;
import org.hsbc.triocodebackend.vo.PaymentListItemVO;

import java.util.List;

public interface PaymentService {

    /** 接口1：创建支付（含幂等处理） */
    PaymentDetailVO createPayment(PaymentCreateReqDTO req);

    /** 接口2：根据 paymentId 查询支付详情 */
    PaymentDetailVO getPaymentDetail(Long paymentId);

    /** 接口3：分页查询支付列表 */
    PageResult<PaymentListItemVO> listPayments(PaymentListQueryDTO query);

    /** 接口4：查询支付状态历史（按 created_at 升序，不分页） */
    List<PaymentHistoryVO> getPaymentHistories(Long paymentId);

    /** 接口9：查询支付失败详情 */
    PaymentFailureVO getPaymentFailure(Long paymentId);
}
