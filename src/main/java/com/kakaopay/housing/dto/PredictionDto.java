package com.kakaopay.housing.dto;

import com.kakaopay.housing.domain.Bank;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PredictionDto {
    private String bank;
    private String year;
    private String month;
    private String amount;

    public PredictionDto(@NotNull Bank bank, Integer year, Integer month, Integer amount) {
        this.bank = bank.getName();
        this.year = String.valueOf(year);
        this.month = String.valueOf(month);
        this.amount = String.valueOf(amount);
    }
}
