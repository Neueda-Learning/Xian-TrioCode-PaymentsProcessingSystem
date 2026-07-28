package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.mapper.CurrencyDictMapper;
import org.hsbc.triocodebackend.service.CurrencyDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyDictServiceImpl implements CurrencyDictService {

    @Autowired
    private CurrencyDictMapper currencyDictMapper;

    @Override
    public List<CurrencyDict> getAllCurrency() {
        return currencyDictMapper.getAllCurrency();
    }
}
