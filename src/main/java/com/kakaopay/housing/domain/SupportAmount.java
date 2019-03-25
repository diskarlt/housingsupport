package com.kakaopay.housing.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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

    private int year;
    private int month;
    private int amount;

    @ManyToOne
    private Bank bank;

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
