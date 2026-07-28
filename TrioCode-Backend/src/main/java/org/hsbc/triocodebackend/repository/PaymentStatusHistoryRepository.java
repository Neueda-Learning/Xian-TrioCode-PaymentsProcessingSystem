package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;

import java.util.List;

@Mapper
public interface PaymentStatusHistoryRepository {

    List<PaymentStatusHistory> selectByPaymentId(@Param("paymentId") Long paymentId);

    int insert(PaymentStatusHistory history);
}
