package org.hsbc.triocodebackend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.hsbc.triocodebackend.mapper.AccountMapper;
import org.hsbc.triocodebackend.model.Account;
import org.hsbc.triocodebackend.model.vo.AccountVO;
import org.hsbc.triocodebackend.service.AccountService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;

    @Override
    public AccountVO getAccountById(Long id) {
        Account account = accountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCodeEnum.ACCOUNT_NOT_FOUND);
        }
        AccountVO vo = new AccountVO();
        vo.setId(account.getId());
        vo.setAccountNo(account.getAccountNo());
        vo.setName(account.getName());
        vo.setStatus(account.getStatus());
        return vo;
    }
}
