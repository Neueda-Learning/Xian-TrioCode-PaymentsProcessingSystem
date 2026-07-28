package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.AccountBalanceHistory;

@Mapper
public interface AccountBalanceHistoryMapper {

    int insert(AccountBalanceHistory history);
}

