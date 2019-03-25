package com.kakaopay.housing.HousingSupport;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Slf4j
public class SupportAmountRepositoryTests {
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SupportAmountRepository supportAmountRepository;

    @Before
    public void before() {
        List<String> bankNameList = new ArrayList<>();
        List<Bank> bankList = new ArrayList<>();
        bankNameList.add("주택도시기금");
        bankNameList.add("국민은행");
        bankNameList.add("우리은행");
        for(String bankName : bankNameList) {
            bankList.add(bankRepository.save(Bank.builder()
                    .name(bankName)
                    .build()));
        }

        // 아래와 같은 금액으로 데이터 생성
        // 주택도시기금 : 2005~2007년 매달 100억원
        // 국민은행 : 2005~2007년 매달 100억원
        // 우리은행 : 2005년 매달 100억원, 2006년 매달 200억원, 2007년 매달 300억원
        for(int year=2005; year<=2007; ++year) {
            for(int month=1; month<=12; ++month) {
                for(Bank bank : bankList) {
                    int amount = 100;
                    if(bank.getName().equals("국민은행")) {
                        if(year == 2006) {
                            amount = 200;
                        } else if(year == 2007) {
                            amount = 300;
                        }
                    }
                    supportAmountRepository.save(SupportAmount.builder()
                            .year(year)
                            .month(month)
                            .amount(amount)
                            .bank(bank)
                            .build());
                }
            }
        }
    }

    @After
    public void after() {
        supportAmountRepository.deleteAll();
        bankRepository.deleteAll();
    }

    @Test
    public void test_summary() {
        List<SupportAmount> supportAmountList = supportAmountRepository.summary();
        for (SupportAmount supportAmount : supportAmountList) {
            if(supportAmount.getBank() == bankRepository.findByName("국민은행").get()) {
                // 연도별 금액 합계가 맞는지 확인한다.
                if(supportAmount.getYear() == 2005) {
                    assertEquals(1200, supportAmount.getAmount());
                } else if (supportAmount.getYear() == 2006) {
                    assertEquals(2400, supportAmount.getAmount());
                } else {
                    assertEquals(3600, supportAmount.getAmount());
                }
            } else {
                assertEquals(1200, supportAmount.getAmount());
            }
        }
    }

    @Test
    public void test_highest_amount() {
        List<SupportAmount> supportAmountList = supportAmountRepository.findHighestSupport(PageRequest.of(0, 1));
        assertEquals(1, supportAmountList.size());

        SupportAmount supportAmount = supportAmountList.get(0);
        assertEquals(2007, supportAmount.getYear());
        assertEquals(3600, supportAmount.getAmount());
        assertEquals("국민은행", supportAmount.getBank().getName());
    }

    @Test
    public void test_min_max_amount() {
        Bank bank = bankRepository.findByName("국민은행").get();
        List<SupportAmount> minSupportAmountList = supportAmountRepository.findAvgMinSupport(bank.getId(), PageRequest.of(0, 1));
        List<SupportAmount> maxSupportAmountList = supportAmountRepository.findAvgMaxSupport(bank.getId(), PageRequest.of(0, 1));
        assertEquals(1, minSupportAmountList.size());
        assertEquals(1, maxSupportAmountList.size());

        SupportAmount minSupportAmount = minSupportAmountList.get(0);
        assertEquals(2005, minSupportAmount.getYear());
        assertEquals(100, minSupportAmount.getAmount());
        assertEquals("국민은행", minSupportAmount.getBank().getName());

        SupportAmount maxSupportAmount = maxSupportAmountList.get(0);
        assertEquals(2007, maxSupportAmount.getYear());
        assertEquals(300, maxSupportAmount.getAmount());
        assertEquals("국민은행", maxSupportAmount.getBank().getName());
    }
}
