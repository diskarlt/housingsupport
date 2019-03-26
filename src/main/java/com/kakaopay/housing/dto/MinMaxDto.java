package com.kakaopay.housing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class MinMaxDto {
    @Getter
    @Setter
    public class MinMaxAmount {
        private String year;
        private String amount;

        private MinMaxAmount(@NotNull SupportAmount supportAmount) {
            this.year = String.valueOf(supportAmount.getYear());
            this.amount = String.valueOf(supportAmount.getAmount());
        }
    }

    private String bank;
    @JsonProperty("support_amount")
    private List<MinMaxAmount> supportAmountList = new ArrayList<>();

    public MinMaxDto(@NotNull Bank bank, @NotNull SupportAmount minAmount, @NotNull SupportAmount maxAmount) {
        this.bank = bank.getName();
        this.supportAmountList.add(new MinMaxAmount(minAmount));    // 최소값
        this.supportAmountList.add(new MinMaxAmount(maxAmount));    // 최대값
    }
}
