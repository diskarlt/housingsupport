package com.kakaopay.housing.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * 년,월 기준으로 금융기관이 지원한 금액
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class SupportAmount {
    @Id
    @GeneratedValue
    private Long id;

    private int year;   // 연도
    private int month;  // 월
    private int amount; // 지원금액

    @ManyToOne
    private Bank bank;  // 금융기관

    public SupportAmount(int year, long amount, Bank bank) {
        this.year = year;
        this.amount = (int) amount;
        this.bank = bank;
    }

    public SupportAmount(int year, double amount, Bank bank) {
        this.year = year;
        this.amount = (int) amount;
        this.bank = bank;
    }
}
