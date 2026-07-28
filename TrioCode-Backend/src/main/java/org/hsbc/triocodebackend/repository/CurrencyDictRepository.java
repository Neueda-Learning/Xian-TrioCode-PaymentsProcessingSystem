package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.CurrencyDict;

import java.util.List;

@Mapper
public interface CurrencyDictRepository {

    List<CurrencyDict> getAllCurrency();

    List<CurrencyDict> getCurrencies(@Param("enabled") Integer enabled);

    CurrencyDict findEnabledByCode(@Param("code") String code);
}
