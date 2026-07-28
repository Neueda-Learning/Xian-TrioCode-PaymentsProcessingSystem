package org.hsbc.triocodebackend.controller;

import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.model.CurrencyDict;
import org.hsbc.triocodebackend.service.CurrencyDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CurrencyDictController {

    @Autowired
    private CurrencyDictService currencyDictService;

    @GetMapping("/getAllCurrency")
    public Result<List<CurrencyDict>> getAllCurrency(){
        return Result.ok(currencyDictService.getAllCurrency());
    }

}
