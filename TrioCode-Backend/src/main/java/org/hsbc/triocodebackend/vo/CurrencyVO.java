package org.hsbc.triocodebackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyVO {
    private Long id;
    private String code;
    private String codeName;
    private String countryName;
    private Integer enabled;
    private Integer scale;
}
