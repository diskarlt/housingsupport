package com.kakaopay.housing.service;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
public class UploadFileService {
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SupportAmountRepository supportAmountRepository;

    /**
     * 지원 금액 정보 삭제
     */
    @Transactional
    public void deleteAll() {
        supportAmountRepository.deleteAll();
        bankRepository.deleteAll();
    }

    /**
     * CSV의 헤더에서 연도 및 월을 제외한 레코드 정보를 기반으로 금융기관 정보를 Bank에 저장한다.
     * 저장 시 (억원) 또는 샘플코드의 주택도시기금1) 과 같은 형태의 금융기관 명을 보정하여 저장한다.
     * @param csvHeader 데이터 파일의 Header Line
     * @return 금융기관 정보 리스트
     */
    private List<Bank> saveBankFromCSVHeader(@NotNull CSVRecord csvHeader) {
        List<Bank> list = new ArrayList<>();
        IntStream.range(0, csvHeader.size())
                .filter(i -> !csvHeader.get(i).isEmpty())
                .forEachOrdered(i -> {
                    String header = csvHeader.get(i);
                    if (header.equals("연도") || header.equals("월")) {
                        list.add(Bank.builder().build());
                    } else {
                        list.add(bankRepository.save(Bank.builder()
                                .name(header
                                        .replace("(억원)", "")    // 국민은행(억원) -> 국민은행
                                        .replace("주택도시기금1)", "주택도시기금"))
                                .build()));
                    }
                });
        return list;
    }

    /**
     * 연도, 월에 해당하는 지원 금액을 SupportAmount에 저장한다
     * @param bankList Bank 리스트
     * @param csvRecord 데이터 파일의 body line
     */
    private void saveSupportAmounts(List<Bank> bankList, @NotNull CSVRecord csvRecord) {
        int year = Integer.parseInt(csvRecord.get(0));
        int month = Integer.parseInt(csvRecord.get(1));
        IntStream.range(2, csvRecord.size()).filter(i -> !csvRecord.get(i).isEmpty())
                .mapToObj(i -> SupportAmount.builder()
                        .year(year)
                        .month(month)
                        .amount(Integer.parseInt(csvRecord.get(i).replace(",", ""))) // 1,234와 같은 포맷 보정
                        .bank(bankList.get(i))
                        .build())
                .forEach(supportAmount -> supportAmountRepository.save(supportAmount));
    }

    /**
     * 업로드한 데이터 파일을 Parsing하여 Repository에 저장한다
     * @param multipartFile 업로드한 데이터 파일
     * @throws IOException IOException 발생
     * @throws NoSuchFieldException 연도, 월 필드 없음
     */
    @Transactional
    public void uploadFile(MultipartFile multipartFile) throws IOException, NoSuchFieldException {
        InputStream is = multipartFile.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
        try(CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT)) {
            List<Bank> bankList = null;
            for (CSVRecord csvRecord : csvParser) {
                if(bankList == null) {
                    // CSV Header Parsing
                    // 연도, 월, 순으로 시작하지 않을 경우 Exception 발생
                    if(!csvRecord.get(0).equals("연도") || !csvRecord.get(1).equals("월"))
                        throw new NoSuchFieldException();

                    bankList = saveBankFromCSVHeader(csvRecord);
                } else {
                    // CSV Body Parsing
                    saveSupportAmounts(bankList, csvRecord);
                }
            }
        }
    }
}
