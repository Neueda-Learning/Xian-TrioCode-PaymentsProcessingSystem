package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.repository.CurrencyDictRepository;
import org.hsbc.triocodebackend.service.CurrencyDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyDictServiceImpl implements CurrencyDictService {

    @Autowired
    private CurrencyDictRepository currencyDictRepository;

    @Override
    public List<CurrencyDict> getAllCurrency() {
        return currencyDictRepository.getAllCurrency();
    }
}
