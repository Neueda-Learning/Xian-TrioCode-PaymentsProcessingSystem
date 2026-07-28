package org.hsbc.triocodebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long id;
    private String accountNo;
    private String name;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


