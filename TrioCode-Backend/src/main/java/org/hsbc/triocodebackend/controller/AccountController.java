package org.hsbc.triocodebackend.controller;

import lombok.RequiredArgsConstructor;
import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.model.vo.AccountVO;
import org.hsbc.triocodebackend.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account lookup —— used by the frontend to resolve an account name from its ID
 * (e.g. Source/Destination Account ID on the Create Payment form).
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Get a single account by ID.
     * Returns ACCOUNT_NOT_FOUND (404) if no account exists with the given ID.
     */
    @GetMapping("/accounts/{id}")
    public Result<AccountVO> getAccountById(@PathVariable Long id) {
        return Result.ok(accountService.getAccountById(id));
    }
}
