package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.hsbc.triocodebackend.model.CurrencyDict;

import java.util.List;

@Mapper
public interface CurrencyDictRepository {

    List<CurrencyDict> getAllCurrency();
}
