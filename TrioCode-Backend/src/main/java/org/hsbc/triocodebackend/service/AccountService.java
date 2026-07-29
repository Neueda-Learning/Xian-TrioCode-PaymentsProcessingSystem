package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.model.vo.AccountVO;

public interface AccountService {

    /**
     * Look up an account by ID.
     *
     * @param id account ID
     * @return account info
     * @throws org.hsbc.triocodebackend.common.exception.BizException ACCOUNT_NOT_FOUND if no account exists with that ID
     */
    AccountVO getAccountById(Long id);
}
