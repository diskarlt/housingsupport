package com.kakaopay.housing.HousingSupport;

import com.kakaopay.housing.dto.BankDto;
import com.kakaopay.housing.dto.HighestSupportDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class HousingControllerTests {
    @Autowired
    private WebTestClient client;

    @Before
    public void test_upload() {
        client = client
                .mutate()
                .responseTimeout(Duration.ofMillis(36000))
                .build();

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        String path = "src/test/resources/HousingSupportAmount.csv";
        bodyMap.add("file", new FileSystemResource(path));
        client.post()
                .uri("/housing/supports")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .syncBody(bodyMap)
                .exchange()
                .expectStatus().isOk()
        ;
    }

    @After
    public void test_clear() {
        client.delete()
                .uri("/housing/supports")
                .exchange()
                .expectStatus().isOk()
        ;
    }

    @Test
    public void test_bank_list() {
        client.get()
                .uri("/housing/banks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BankDto.class)
                .hasSize(9)
        ;
    }

    @Test
    public void test_summary() {
        client.get()
                .uri("/housing/supports/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
        ;
    }

    @Test
    public void test_highest_support() {
        client.get()
                .uri("/housing/supports/highest_support")
                .exchange()
                .expectStatus().isOk()
                .expectBody(HighestSupportDto.class)
        ;
    }

    @Test
    public void test_min_max() {
        List<BankDto> bankDtoList = new ArrayList<>();
        client.get()
                .uri("/housing/banks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BankDto.class)
                .hasSize(9)
                .consumeWith(response -> bankDtoList.addAll(Objects.requireNonNull(response.getResponseBody())))
        ;

        String id = bankDtoList.stream()
                .filter(b -> b.getName().equals("외환은행"))
                .findFirst()
                .orElseThrow(NoSuchElementException::new)
                .getCode();
        client.get()
                .uri("/housing/banks/"+id+"/supports/min_max")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> log.info(response.toString()))
        ;
    }

    @Test
    public void test_prediction() {
        List<BankDto> bankDtoList = new ArrayList<>();
        client.get()
                .uri("/housing/banks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BankDto.class)
                .hasSize(9)
                .consumeWith(response -> bankDtoList.addAll(Objects.requireNonNull(response.getResponseBody())))
        ;

        String id = bankDtoList.stream()
                .filter(b -> b.getName().equals("국민은행"))
                .findFirst()
                .orElseThrow(NoSuchElementException::new)
                .getCode();

        client.get()
                .uri("/housing/supports/prediction/banks/"+id+"?year=2018&month=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
        ;
    }
}
