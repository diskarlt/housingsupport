package com.kakaopay.housing.HousingSupport;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.dto.*;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import com.kakaopay.housing.service.HousingSupportService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class HousingSupportServiceTests {
    @InjectMocks
    private HousingSupportService housingSupportService;

    @Mock
    private BankRepository bankRepository;
    @Mock
    private SupportAmountRepository supportAmountRepository;

    @Test
    public void test_bank_list() {
        List<Bank> bankList = new ArrayList<>();
        bankList.add(new Bank(0L, "주택도시기금"));
        bankList.add(new Bank(1L, "국민은행"));
        bankList.add(new Bank(2L, "우리은행"));
        bankList.add(new Bank(3L, "신한은행"));
        bankList.add(new Bank(4L, "한국시티은행"));
        bankList.add(new Bank(5L, "하나은행"));
        bankList.add(new Bank(6L, "농협은행/수협은행"));
        bankList.add(new Bank(7L, "외환은행"));
        bankList.add(new Bank(8L, "기타은행"));
        when(bankRepository.findAll()).thenReturn(bankList);

        List<BankDto> bankDtoList = housingSupportService.findBankList();
        assertEquals(bankList.size(), bankDtoList.size());
        for(Bank bank : bankList) {
            assertTrue(bankDtoList.contains(new BankDto(bank)));
        }
        log.info(bankList.toString());
        log.info(bankDtoList.toString());

        verify(bankRepository).findAll();
    }

    @Test
    public void test_summary() {
        List<SupportAmount> supportAmountList = new ArrayList<>();
        Bank bank1 = new Bank(1L, "주택도시기금");
        Bank bank2 = new Bank(2L, "국민은행");
        long id = 1L;
        for(int year=2005; year<=2017; ++year) {
            supportAmountList.add(SupportAmount.builder()
                    .id(++id)
                    .year(year)
                    .amount(year)
                    .bank(bank1)
                    .build());
            supportAmountList.add(SupportAmount.builder()
                    .id(++id)
                    .year(year)
                    .amount(year*2)
                    .bank(bank2)
                    .build());
        }
        when(supportAmountRepository.summary()).thenReturn(supportAmountList);

        SummaryDto summaryDto = housingSupportService.summary();
        assertEquals("주택금융 공급현황", summaryDto.getName());
        assertEquals(13, summaryDto.getSummary().size());
        for(int i=0; i<summaryDto.getSummary().size(); ++i) {
            int year, amount;
            year = amount = 2005 + i;
            assertEquals(year+" 년", summaryDto.getSummary().get(i).getYear());
            assertEquals(String.valueOf(amount*3), summaryDto.getSummary().get(i).getTotal_amount());
            assertEquals(String.valueOf(amount), summaryDto.getSummary().get(i).getDetail_amount().get("주택도시기금"));
            assertEquals(String.valueOf(amount*2), summaryDto.getSummary().get(i).getDetail_amount().get("국민은행"));
        }
        log.info(summaryDto.toString());

        verify(supportAmountRepository).summary();
    }

    @Test
    public void test_highest_support() {
        List<SupportAmount> supportAmountList = new ArrayList<>();
        supportAmountList.add(SupportAmount.builder()
                .year(2010)
                .amount(1200)
                .bank(Bank.builder()
                        .id(1L)
                        .name("국민은행")
                        .build())
                .build());
        when(supportAmountRepository.findHighestSupport(PageRequest.of(0, 1))).thenReturn(supportAmountList);

        HighestSupportDto highestSupportDto = housingSupportService.findHighestSupport();
        assertEquals("2010", highestSupportDto.getYear());
        assertEquals("국민은행", highestSupportDto.getBank());

        verify(supportAmountRepository).findHighestSupport(PageRequest.of(0, 1));
    }

    @Test
    public void test_min_max() {
        List<SupportAmount> minList = new ArrayList<>();
        List<SupportAmount> maxList = new ArrayList<>();
        minList.add(SupportAmount.builder()
                .year(2010)
                .amount(67)
                .build());
        maxList.add(SupportAmount.builder()
                .year(2010)
                .amount(546)
                .build());
        Bank bank = Bank.builder()
                .id(1L)
                .name("외환은행")
                .build();
        when(bankRepository.findById(anyLong())).thenReturn(Optional.of(bank));
        when(supportAmountRepository.findAvgMinSupport(anyLong(), any())).thenReturn(minList);
        when(supportAmountRepository.findAvgMaxSupport(anyLong(), any())).thenReturn(maxList);

        MinMaxDto minMaxDto = housingSupportService.findMinMax(1L);
        assertEquals("외환은행", minMaxDto.getBank());
        assertEquals("2010", minMaxDto.getSupportAmountList().get(0).getYear());
        assertEquals("67", minMaxDto.getSupportAmountList().get(0).getAmount());
        assertEquals("2010", minMaxDto.getSupportAmountList().get(1).getYear());
        assertEquals("546", minMaxDto.getSupportAmountList().get(1).getAmount());

        verify(supportAmountRepository).findAvgMinSupport(anyLong(), any());
        verify(supportAmountRepository).findAvgMaxSupport(anyLong(), any());
    }

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
                .amount(100)
                .build());
        supportAmountList.add(SupportAmount.builder()
                .id(3L)
                .year(2017)
                .month(12)
                .amount(200)
                .build());
        when(supportAmountRepository.findAllByBank_Id(anyLong())).thenReturn(supportAmountList);

        PredictionDto predictionDto = housingSupportService.predict(1L, 2018, 2);
        assertEquals("국민은행", predictionDto.getBank());
        assertEquals("2018", predictionDto.getYear());
        assertEquals("2", predictionDto.getMonth());
        assertEquals("400", predictionDto.getAmount());

        verify(bankRepository).findById(anyLong());
        verify(supportAmountRepository).findAllByBank_Id(anyLong());
    }
}
