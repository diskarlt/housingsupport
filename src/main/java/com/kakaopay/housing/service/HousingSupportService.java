package com.kakaopay.housing.service;

import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.dto.BankDto;
import com.kakaopay.housing.dto.HighestSupportDto;
import com.kakaopay.housing.dto.MinMaxDto;
import com.kakaopay.housing.dto.SummaryDto;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class HousingSupportService {
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SupportAmountRepository supportAmountRepository;

    /**
     * 금융기관 목록 조회
     * @return 금융기관 목록
     */
    public List<BankDto> findBankList() {
        return bankRepository.findAll()
                .stream()
                .map(BankDto::new)
                .collect(toList());
    }

    /**
     * 연도별 각 금융기관의 지원금액 합계 조회
     * @return 연도별 각 금융기관의 지원금액 합계
     */
    @Transactional
    public SummaryDto summary() {
        SummaryDto summaryDto = new SummaryDto();
        try (Stream<SupportAmount> supportAmountStream = supportAmountRepository.summary()) {
            summaryDto.setSummaryPerYear(supportAmountStream.collect((Collectors.groupingBy(SupportAmount::getYear))));
        }
        log.info(summaryDto.toString());
        return summaryDto;
    }

    /**
     * 각 연도별 각 기관의 전체 지원금액 중 연도별 지원금액 합계가 가장 컸던 해와 해당 금액을 지원한 기관명 조회
     * @return 연도별 지원금액 합계가 가장 컸던 해와 해당 금액을 지원한 기관명
     */
    public HighestSupportDto findHighestSupport() {
        return new HighestSupportDto(supportAmountRepository.findHighestSupport(PageRequest.of(0, 1)).get(0));
    }

    /**
     * 전체 년도에서 특정 금융기관의 지원금액 평균 중에서 가장 작은 금액과 큰 금액 조회
     * @param bankId 조회할 금융기관 Id
     * @return 금융기관명과 가장 작았던/컸던 지원금액 평균
     */
    public MinMaxDto findMinMax(Long bankId) {
        return new MinMaxDto(bankRepository.findById(bankId).orElseThrow(EntityNotFoundException::new),
                supportAmountRepository.findAvgMinSupport(bankId, PageRequest.of(0, 1)).get(0),
                supportAmountRepository.findAvgMaxSupport(bankId, PageRequest.of(0, 1)).get(0));
    }
}
