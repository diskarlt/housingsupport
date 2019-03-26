package com.kakaopay.housing.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 금융 기관 정보
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Bank {
    @Id
    @GeneratedValue
    private Long id;        // 금융 기관 id
    private String name;    // 금융 기관명
}
