package com.kakaopay.housing.web;

import com.kakaopay.housing.dto.*;
import com.kakaopay.housing.service.HousingSupportService;
import com.kakaopay.housing.service.PredictService;
import com.kakaopay.housing.service.UploadFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class HousingController {
    @Autowired
    HousingSupportService housingSupportService;
    @Autowired
    PredictService predictService;
    @Autowired
    UploadFileService uploadFileService;

    /**
     * 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API
     */
    @PostMapping(value = "/supports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile multipartFile) {
        try {
            uploadFileService.uploadFile(multipartFile);
        } catch (IOException | NoSuchFieldException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 데이터베이스에 저장된 금융기관 정보 및 지원 금액 정보를 삭제하는 API
     */
    @DeleteMapping(value = "/supports")
    public void delete() {
        uploadFileService.deleteAll();
    }

    /**
     * 주택 금융 공급 금융기관(은행) 목록을 출력하는 API
     */
    @GetMapping(value = "/banks", produces = "application/json")
    public List<BankDto> findBankList() {
        return housingSupportService.findBankList();
    }

    /**
     * 연도별 각 금융기관의 지원금액 합계를 출력하는 API
     */
    @GetMapping(value = "/supports/summary", produces = "application/json")
    public SummaryDto summary() {
        return housingSupportService.summary();
    }

    /**
     * 각 연도별 각 기관의 전체 지원금액 중 연도별 지원금액 합계가 가장 컸던 해와 해당 금액을 지원한 기관명을 출력하는 API
     */
    @GetMapping(value = "/supports/highest_support", produces = "application/json")
    public HighestSupportDto findHighestSupport() {
        return housingSupportService.findHighestSupport();
    }

    /**
     * 전체 년도에서 특정 은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API
     */
    @GetMapping(value = "/banks/{id}/supports/min_max", produces = "application/json")
    public MinMaxDto findMinMax(@PathVariable long id) {
        return housingSupportService.findMinMax(id);
    }

    /**
     * 특정 은행의 특정 달에 대해서 금융지원 금액을 예측하는 API
     */
    @GetMapping(value = "/supports/prediction/banks/{id}", produces = "application/json")
    public PredictionDto predict(@PathVariable long id, @RequestParam Map<String, String> parameters) throws NoSuchFieldException {
        if(parameters.isEmpty() || parameters.get("year").isEmpty() || parameters.get("month").isEmpty())
            throw new NoSuchFieldException();
        int year = Integer.parseInt(parameters.get("year"));
        int month = Integer.parseInt(parameters.get("month"));
        return predictService.predict(id, year, month);
    }
}
