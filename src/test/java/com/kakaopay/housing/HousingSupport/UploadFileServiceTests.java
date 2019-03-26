package com.kakaopay.housing.HousingSupport;

import com.kakaopay.housing.domain.Bank;
import com.kakaopay.housing.domain.SupportAmount;
import com.kakaopay.housing.repository.BankRepository;
import com.kakaopay.housing.repository.SupportAmountRepository;
import com.kakaopay.housing.service.UploadFileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class UploadFileServiceTests {
    @InjectMocks
    private UploadFileService uploadFileService;

    @Mock
    private BankRepository bankRepository;
    @Mock
    private SupportAmountRepository supportAmountRepository;

    @Test
    public void test_upload_file() {
        File file = new File("src/test/resources/HousingSupportAmount.csv");
        try {
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), input);
            uploadFileService.uploadFile(multipartFile);

            verify(bankRepository, times(9)).save(any(Bank.class));
            verify(supportAmountRepository, times(1386)).save(any(SupportAmount.class));
        } catch (IOException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
