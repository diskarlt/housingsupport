package com.kakaopay.housing.service;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.dto.PredictionDto;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class PredictService {
    @Autowired
    BankRepository bankRepository;
    @Autowired
    SupportAmountRepository supportAmountRepository;

    /**
     * 선형회귀 알고리즘을 통한 값 예측
     */
    @NotNull
    private Double linearRegression(@NotNull List<Integer> x, @NotNull List<Integer> y, Integer predictForValue) {
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

        double interceptNominator = ySummed - slope * xSummed;
        double interceptDenominator = Double.valueOf(numberOfDataValues);
        double intercept = interceptNominator / interceptDenominator;

        return slope * predictForValue + intercept;
    }

    /**
     * 특정 은행의 특정 달에 대해서 금융지원 금액을 예측
     * @param id 예측할 금융기관 id
     * @param year 예측할 연도
     * @param month 예측할 달
     * @return 예측되는 금융지원 금액
     */
    @Transactional
    public PredictionDto predict(long id, int year, int month) {
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();

        Bank bank = bankRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        try (Stream<SupportAmount> supportAmountStream = supportAmountRepository.findAllByBank_Id(bank.getId())) {
            supportAmountStream.forEach(supportAmount -> {
                x.add(supportAmount.getYear()*12 + supportAmount.getMonth());
                y.add(supportAmount.getAmount());
            });
        }

        Integer amount = Math.max(linearRegression(x, y, year*12 + month).intValue(), 0);
        return new PredictionDto(bank, year, month, amount);
    }
}
