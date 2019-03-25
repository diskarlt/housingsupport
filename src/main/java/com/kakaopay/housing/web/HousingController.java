package com.kakaopay.housing.web;

import com.kakaopay.housing.dto.*;
import com.kakaopay.housing.service.HousingSupportService;
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
@RequestMapping(value = "/housing")
@Slf4j
public class HousingController {
    @Autowired
    HousingSupportService housingSupportService;
    @Autowired
    UploadFileService uploadFileService;

    @PostMapping(value = "/supports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile multipartFile) {
        try {
            uploadFileService.uploadFile(multipartFile);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/supports")
    public void delete() {
        uploadFileService.deleteAll();
    }

    @GetMapping(value = "/banks", produces = "application/json")
    public List<BankDto> findBankList() {
        return housingSupportService.findBankList();
    }

    @GetMapping(value = "/supports/summary", produces = "application/json")
    public SummaryDto summary() {
        return housingSupportService.summary();
    }

    @GetMapping(value = "/supports/highest_support", produces = "application/json")
    public HighestSupportDto findHighestSupport() {
        return housingSupportService.findHighestSupport();
    }

    @GetMapping(value = "/banks/{id}/supports/min_max", produces = "application/json")
    public MinMaxDto findMinMax(@PathVariable long id) {
        return housingSupportService.findMinMax(id);
    }

    @GetMapping(value = "/supports/prediction/banks/{id}", produces = "application/json")
    public PredictionDto predict(@PathVariable long id, @RequestParam Map<String, String> parameters) throws NoSuchFieldException {
        if(parameters.isEmpty() || parameters.get("year").isEmpty() || parameters.get("month").isEmpty())
            throw new NoSuchFieldException();
        int year = Integer.parseInt(parameters.get("year"));
        int month = Integer.parseInt(parameters.get("month"));
        return housingSupportService.predict(id, year, month);
    }
}
