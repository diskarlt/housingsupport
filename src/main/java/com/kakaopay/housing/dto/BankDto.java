package com.kakaopay.housing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kakaopay.housing.domain.Bank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class BankDto {
    @JsonProperty("institute_code")
    private String code;
    @JsonProperty("institute_name")
    private String name;

    public BankDto(@NotNull Bank bank) {
        this.code = bank.getId().toString();
        this.name = bank.getName();
    }
}
