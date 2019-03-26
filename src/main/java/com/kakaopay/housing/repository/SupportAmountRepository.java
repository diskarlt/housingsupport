package com.kakaopay.housing.repository;

import com.kakaopay.housing.domain.SupportAmount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface SupportAmountRepository extends JpaRepository<SupportAmount, Long> {
    @Query("SELECT new com.kakaopay.housing.domain.SupportAmount(s.year, SUM(s.amount), b)" +
            " FROM SupportAmount s LEFT JOIN s.bank b" +
            " GROUP BY s.year, b.name" +
            " ORDER BY s.year")
    Stream<SupportAmount> summary();
    @Query("SELECT new com.kakaopay.housing.domain.SupportAmount(s.year, SUM(s.amount), b)" +
            " FROM SupportAmount s LEFT JOIN s.bank b" +
            " GROUP BY s.year, b.name" +
            " ORDER BY SUM(s.amount) desc")
    List<SupportAmount> findHighestSupport(Pageable pageable);
    @Query("SELECT new com.kakaopay.housing.domain.SupportAmount(s.year, AVG(s.amount), b)" +
            " FROM SupportAmount s LEFT JOIN s.bank b" +
            " where b.id = :bankId " +
            " GROUP BY s.year, b.name" +
            " ORDER BY AVG(s.amount) asc")
    List<SupportAmount> findAvgMinSupport(@Param("bankId") long bankId, Pageable pageable);
    @Query("SELECT new com.kakaopay.housing.domain.SupportAmount(s.year, AVG(s.amount), b)" +
            " from SupportAmount s LEFT JOIN s.bank b" +
            " where b.id = :bankId " +
            " GROUP BY s.year, b.name" +
            " ORDER BY AVG(s.amount) desc")
    List<SupportAmount> findAvgMaxSupport(@Param("bankId") long bankId, Pageable pageable);
    Stream<SupportAmount> findAllByBank_Id(long bankId);
}
