package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.vo.CurrencyVO;

import java.util.List;

public interface CurrencyDictService {

    List<CurrencyDict> getAllCurrency();

    List<CurrencyVO> getCurrencies(Integer enabled);
}
