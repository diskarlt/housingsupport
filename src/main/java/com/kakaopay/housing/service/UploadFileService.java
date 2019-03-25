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
import java.util.Optional;

@Service
@Slf4j
public class UploadFileService {
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private SupportAmountRepository supportAmountRepository;

    @Transactional
    public void deleteAll() {
        supportAmountRepository.deleteAll();
        bankRepository.deleteAll();
    }

    @Transactional
    private void saveBankFromCSVHeader(@NotNull List<String> list) {
        List<Bank> bankList = new ArrayList<>();
        list.stream()
                .filter(header -> !header.equals("연도")&&!header.equals("월"))
                .map(name -> Bank.builder()
                        .name(name)
                        .build())
                .forEach(bankList::add);
        bankRepository.saveAll(bankList);
    }

    @Transactional
    private void saveSupportAmounts(@NotNull List<String> headerList, @NotNull List<String> recordList) {
        List<Optional<Bank>> bankList = new ArrayList<>();
        List<SupportAmount> supportAmountList = new ArrayList<>();
        int yearIdx = headerList.indexOf("연도");
        int monthIdx = headerList.indexOf("월");
        int year = Integer.parseInt(recordList.get(yearIdx));
        int month = Integer.parseInt(recordList.get(monthIdx));

        for (String s : headerList) {
            bankList.add(bankRepository.findByName(s));
        }

        for (int i = 0; i < recordList.size(); i++) {
            if (bankList.get(i).isPresent()) {
                SupportAmount build = SupportAmount.builder()
                        .year(year)
                        .month(month)
                        .amount(Integer.parseInt(recordList.get(i)))
                        .bank(bankList.get(i).get())
                        .build();
                supportAmountList.add(build);
            }
        }
        supportAmountRepository.saveAll(supportAmountList);
    }

    public void uploadFile(MultipartFile multipartFile) throws IOException {
        InputStream is = multipartFile.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "EUC-KR"));
        CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT);
        List<String> headerList = new ArrayList<>();

        for (CSVRecord csvRecord : csvParser) {
            if(headerList.isEmpty()) {
                for (int i = 0; i < csvRecord.size(); i++) {
                    if (!csvRecord.get(i).isEmpty()) {
                        headerList.add(csvRecord.get(i)
                                .replace("(억원)", "")
                                .replace("주택도시기금1)", "주택도시기금"));
                    }
                }
                saveBankFromCSVHeader(headerList);
            } else {
                List<String> recordList = new ArrayList<>();
                for (int i = 0; i < csvRecord.size(); i++) {
                    if (!csvRecord.get(i).isEmpty()) {
                        recordList.add(csvRecord.get(i)
                                .replace(",", ""));
                    }
                }
                saveSupportAmounts(headerList, recordList);
            }
        }
    }
}
