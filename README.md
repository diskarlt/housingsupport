# 주택 금융 서비스 API

주택 금융 공급 현황 분석 서비스

## Framework
 - Spring Boot
 - Spring-Data-JPA
 - H2 Database
 - Lombok
 - Apache Commons CSV

## Strategy
 - 	POST /housing/supports
    * 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API
    * CSV 파일의 헤더 라인을 분석하여 금융기관 정보 저장
    * CSV 파일의 각 레코드를 분석하여 연도 및 월을 기준으로 지원 금액 저장

 - DELETE /housing/supports
    * 데이터베이스에 저장된 금융기관 정보 및 지원 금액 정보를 삭제하는 API

 -  GET /housing/banks
    * 주택 금융 공급 금융기관(은행) 목록을 출력하는 API
    * 데이터베이스에 저장되어있는 금융기관 정보 기준으로 목록 조회

 -  GET /housing/supports/summary
    * 연도별 각 금융기관의 지원금액 합계를 출력하는 API
    * (출력 예제가 JSON 규격에 맞지 않아 각 금융기관의 지원금액 합계 리스트에 대한 key(summary)를 추가함)
    * JPQL을 사용하여 연도별, 금융기관별 기준으로 지원 금액 조회

 -  GET /housing/supports/highest_support
    * 각 연도별 각 기관의 전체 지원금액 중 연도별 지원금액 합계가 가장 컸던 해와 해당 금액을 지원한 기관명을 출력하는 API
    * JPQL을 사용하여 연도별, 금융기관별 기준으로 그룹핑하여 지원 금액의 합계를 조회
    * 조회한 지원 금액 합계를 페이징하여 가장 큰 금액 정보만 조회

 -  GET /housing/banks/{id}/supports/min_max
    * 전체 년도에서 특정 은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API
    * 요청한 기능 명세는 특정한 기관(외환은행)에만 적용되는 API이나 효율성을 위해 모든 기관에 대해 동작할 수 있도록 구현
    * JPQL을 사용하여 연도별, 금융기관별 기준으로 그룹핑하여 지원 금액 평균을 조회
    * 조회한 지원 금액 합계를 페이징하여 가장 큰/작은 금액 정보만 조회

 -  GET /housing/supports/prediction/banks/{id}?year={year}&month={month}
    * 특정 은행의 특정 달에 대해서 금융지원 금액을 예측하는 API
    * 경제학에서 경험적인 데이터를 통해 미래를 예측하고자 할 때 주로 사용하는 선형 회귀(linear regression) 알고리즘을 사용

## Build
``` bash
mvnw package
```

## Run
``` bash
java -jar target/housing-support-1.0-SNAPSHOT.jar
```
