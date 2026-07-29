package org.hsbc.triocodebackend.model.vo;

import lombok.Data;

/**
 * Lightweight account info used to resolve an account name from its ID
 * (e.g. shown while filling in Source/Destination Account ID on the Create Payment form).
 */
@Data
public class AccountVO {
    private Long id;
    private String accountNo;
    private String name;
    private Integer status;
}
