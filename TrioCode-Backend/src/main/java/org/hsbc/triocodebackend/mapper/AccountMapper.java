package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.Account;

import java.math.BigDecimal;

@Mapper
public interface AccountMapper {

    Account selectById(Long id);

    /** 带行锁查询，用于余额扣减前防止并发超扣 */
    Account selectByIdForUpdate(Long id);

    int updateBalance(@Param("id") Long id, @Param("newBalance") BigDecimal newBalance);
}

