package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.PaymentStatusHistory;

@Mapper
public interface PaymentStatusHistoryMapper {

    int insert(PaymentStatusHistory history);
}

