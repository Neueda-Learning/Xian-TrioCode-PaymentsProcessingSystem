package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.AccountBalanceHistory;

@Mapper
public interface AccountBalanceHistoryRepository {

    int insert(AccountBalanceHistory history);
}
