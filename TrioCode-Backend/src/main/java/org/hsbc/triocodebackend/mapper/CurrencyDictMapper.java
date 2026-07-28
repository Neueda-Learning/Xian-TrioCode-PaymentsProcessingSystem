package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.CurrencyDict;

import java.util.List;

@Mapper
public interface CurrencyDictMapper {

    List<CurrencyDict> getAllCurrency();

    CurrencyDict selectByCode(String code);
}

