package com.kakaopay.housing.dto;

import com.kakaopay.housing.domain.SupportAmount;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class HighestSupportDto {
    private String year;
    private String bank;

    public HighestSupportDto(@NotNull SupportAmount supportAmount) {
        this.year = String.valueOf(supportAmount.getYear());
        this.bank = supportAmount.getBank().getName();
    }
}
