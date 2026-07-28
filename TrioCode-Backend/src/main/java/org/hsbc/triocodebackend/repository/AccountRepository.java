package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.Account;

import java.math.BigDecimal;

@Mapper
public interface AccountRepository {

    Account selectById(@Param("id") Long id);

    int deductBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
