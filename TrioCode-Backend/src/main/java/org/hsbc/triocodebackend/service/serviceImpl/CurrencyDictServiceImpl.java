package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.repository.CurrencyDictRepository;
import org.hsbc.triocodebackend.service.CurrencyDictService;
import org.hsbc.triocodebackend.vo.CurrencyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyDictServiceImpl implements CurrencyDictService {

    @Autowired
    private CurrencyDictRepository currencyDictRepository;

    @Override
    public List<CurrencyDict> getAllCurrency() {
        return currencyDictRepository.getAllCurrency();
    }

    @Override
    public List<CurrencyVO> getCurrencies(Integer enabled) {
        return currencyDictRepository.getCurrencies(enabled).stream()
                .map(c -> CurrencyVO.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .codeName(c.getCodeName())
                        .countryName(c.getCountryName())
                        .enabled(c.getEnabled())
                        .scale(c.getScale())
                        .build())
                .collect(Collectors.toList());
    }
}
