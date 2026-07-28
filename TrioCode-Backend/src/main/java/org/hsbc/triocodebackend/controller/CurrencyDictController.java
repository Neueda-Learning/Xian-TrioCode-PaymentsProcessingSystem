package org.hsbc.triocodebackend.controller;

import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.service.CurrencyDictService;
import org.hsbc.triocodebackend.vo.CurrencyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CurrencyDictController {

    @Autowired
    private CurrencyDictService currencyDictService;

    /**
     * 接口10：查询币种字典
     * GET /api/v1/dicts/currencies?enabled=1（默认仅启用）/ enabled=0（全部）
     */
    @GetMapping("/dicts/currencies")
    public Result<List<CurrencyVO>> getCurrencies(@RequestParam(defaultValue = "1") Integer enabled) {
        return Result.ok(currencyDictService.getCurrencies(enabled));
    }
}
