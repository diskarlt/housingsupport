package com.kakaopay.housing.HousingSupport;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.dto.PredictionDto;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import com.kakaopay.housing.service.PredictService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class PredictServiceTests {
    @InjectMocks
    private PredictService predictService;

    @Mock
    private BankRepository bankRepository;
    @Mock
    private SupportAmountRepository supportAmountRepository;

    /**
     * 금융 지원 금액 예측
     */
    @Test
    public void test_prediction() {
        Bank bank = Bank.builder()
                .id(1L)
                .name("국민은행")
                .build();
        when(bankRepository.findById(anyLong())).thenReturn(Optional.of(bank));
        List<SupportAmount> supportAmountList = new ArrayList<>();
        supportAmountList.add(SupportAmount.builder()
                .id(2L)
                .year(2017)
                .month(11)
                .amount(200)
                .build());
        supportAmountList.add(SupportAmount.builder()
                .id(3L)
                .year(2017)
                .month(12)
                .amount(400)
                .build());
        when(supportAmountRepository.findAllByBank_Id(anyLong())).thenReturn(supportAmountList.stream());

        PredictionDto predictionDto = predictService.predict(bank.getId(), 2018, 2);
        assertEquals(bank.getName(), predictionDto.getBank());
        assertEquals("2018", predictionDto.getYear());
        assertEquals("2", predictionDto.getMonth());
        assertEquals("800", predictionDto.getAmount());

        verify(bankRepository).findById(bank.getId());
        verify(supportAmountRepository).findAllByBank_Id(bank.getId());
    }

    /**
     * 지원금액의 지속적인 하락이 예측되어 0보다 작을 것 같은 경우
     */
    @Test
    public void test_minus_prediction() {
        Bank bank = Bank.builder()
                .id(1L)
                .name("국민은행")
                .build();
        when(bankRepository.findById(anyLong())).thenReturn(Optional.of(bank));
        List<SupportAmount> supportAmountList = new ArrayList<>();
        supportAmountList.add(SupportAmount.builder()
                .id(2L)
                .year(2017)
                .month(11)
                .amount(200)
                .build());
        supportAmountList.add(SupportAmount.builder()
                .id(3L)
                .year(2017)
                .month(12)
                .amount(100)
                .build());
        when(supportAmountRepository.findAllByBank_Id(anyLong())).thenReturn(supportAmountList.stream());

        PredictionDto predictionDto = predictService.predict(bank.getId(), 2018, 2);
        assertEquals(bank.getName(), predictionDto.getBank());
        assertEquals("2018", predictionDto.getYear());
        assertEquals("2", predictionDto.getMonth());
        assertEquals("0", predictionDto.getAmount());

        verify(bankRepository).findById(bank.getId());
        verify(supportAmountRepository).findAllByBank_Id(bank.getId());
    }
}
