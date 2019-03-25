package com.kakaopay.housing.service;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.dto.*;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class HousingSupportService {
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SupportAmountRepository supportAmountRepository;

    public List<BankDto> findBankList() {
        return bankRepository.findAll()
                .stream()
                .map(BankDto::new)
                .collect(toList());
    }

    public SummaryDto summary() {
        SummaryDto summaryDto = new SummaryDto();
        summaryDto.setSummaryPerYear(supportAmountRepository.summary()
                .stream()
                .collect(Collectors.groupingBy(SupportAmount::getYear))
        );
        log.info(summaryDto.toString());
        return summaryDto;
    }

    public HighestSupportDto findHighestSupport() {
        return new HighestSupportDto(supportAmountRepository.findHighestSupport(PageRequest.of(0, 1)).get(0));
    }

    public MinMaxDto findMinMax(Long bankId) {
        return new MinMaxDto(bankRepository.findById(bankId).orElseThrow(EntityNotFoundException::new),
                supportAmountRepository.findAvgMinSupport(bankId, PageRequest.of(0, 1)).get(0),
                supportAmountRepository.findAvgMaxSupport(bankId, PageRequest.of(0, 1)).get(0));
    }

    public PredictionDto predict(long id, int year, int month) {
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();

        Bank bank = bankRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        List<SupportAmount> supportAmountList = supportAmountRepository.findAllByBank_Id(bank.getId());
        supportAmountList.forEach(supportAmount -> {
            x.add(supportAmount.getYear() * 12 + supportAmount.getMonth());
            y.add(supportAmount.getAmount());
        });

        Integer numberOfDataValues = x.size();

        List<Double> xSquared = x.stream()
                .map(position -> Math.pow(position, 2))
                .collect(Collectors.toList());

        List<Integer> xMultipliedByY = IntStream.range(0, numberOfDataValues)
                .map(i -> x.get(i) * y.get(i))
                .boxed()
                .collect(Collectors.toList());

        Integer xSummed = x.stream()
                .reduce(0, (prev, next) -> prev + next);

        Integer ySummed = y.stream()
                .reduce(0, (prev, next) -> prev + next);

        Double sumOfXSquared = xSquared.stream()
                .reduce(0.0, (prev, next) -> prev + next);

        Integer sumOfXMultipliedByY = xMultipliedByY.stream()
                .reduce(0, (prev, next) -> prev + next);

        Integer slopeNominator = numberOfDataValues * sumOfXMultipliedByY - ySummed * xSummed;
        Double slopeDenominator = numberOfDataValues * sumOfXSquared - Math.pow(xSummed, 2);
        Double slope = slopeNominator / slopeDenominator;

        Double interceptNominator = ySummed - slope * xSummed;
        Double interceptDenominator = Double.valueOf(numberOfDataValues);
        Double intercept = interceptNominator / interceptDenominator;

        double amount = (slope * (year*12 + month)) + intercept;

        return new PredictionDto(bank, year, month, (int) amount);
    }
}
