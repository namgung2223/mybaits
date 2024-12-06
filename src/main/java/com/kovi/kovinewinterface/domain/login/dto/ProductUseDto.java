package com.kovi.kovinewinterface.domain.login.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductUseDto {
    private String prdtCd;
    private String serviceStatus;
    private String priceType;
    private String offline;
    private LocalDate pauseDt;
    private LocalDate startDt;

    public ProductUseDto(String prdtCd){
        this.prdtCd = prdtCd;
    }
}
