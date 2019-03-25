package com.kakaopay.housing.dto;

import com.kakaopay.housing.domain.SupportAmount;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SummaryDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public class SummaryPerYear {
        private String year;
        private String total_amount;
        private Map<String, String> detail_amount;

        SummaryPerYear(int year, @NotNull List<SupportAmount> supportAmountList) {
            this.year = year + " 년";
            this.total_amount = String.valueOf(
                    supportAmountList.stream()
                    .mapToLong(SupportAmount::getAmount)
                    .sum()
            );

            Map<String, String> map = new HashMap<>();
            for (SupportAmount s : supportAmountList) {
                if (map.put(s.getBank().getName(), String.valueOf(s.getAmount())) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            this.detail_amount = map;
        }
    }

    private final String name = "주택금융 공급현황";
    private List<SummaryPerYear> summary = new ArrayList<>();

    public void setSummaryPerYear(Map<Integer, List<SupportAmount>> map) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
            this.summary.add(new SummaryPerYear(entry.getKey(), entry.getValue()))
        );
    }
}
