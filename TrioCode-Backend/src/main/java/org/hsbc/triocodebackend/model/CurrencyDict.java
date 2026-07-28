package org.hsbc.triocodebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDict {
    private Long id;
    private String code;
    private String codeName;
    private String countryName;
}


