package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;

import java.util.List;

@Mapper
public interface PaymentStatusHistoryMapper {

    int insert(PaymentStatusHistory history);

    /**
     * 查询某笔支付的全量状态历史，按 created_at 升序，不分页。
     */
    List<PaymentStatusHistory> selectByPaymentId(Long paymentId);
}

