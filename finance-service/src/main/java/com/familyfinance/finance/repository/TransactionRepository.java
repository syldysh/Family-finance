package com.familyfinance.finance.repository;

import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.family IS NULL " +
           "AND (:dateFrom IS NULL OR t.date >= :dateFrom) " +
           "AND (:dateTo IS NULL OR t.date <= :dateTo) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "ORDER BY t.date DESC")
    List<Transaction> findPersonal(@Param("userId") Long userId,
                                   @Param("dateFrom") LocalDate dateFrom,
                                   @Param("dateTo") LocalDate dateTo,
                                   @Param("type") OperationType type,
                                   @Param("categoryId") Long categoryId);

    @Query("SELECT t FROM Transaction t WHERE t.family.id = :familyId " +
           "AND (:dateFrom IS NULL OR t.date >= :dateFrom) " +
           "AND (:dateTo IS NULL OR t.date <= :dateTo) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:userId IS NULL OR t.userId = :userId) " +
           "ORDER BY t.date DESC")
    List<Transaction> findByFamily(@Param("familyId") Long familyId,
                                   @Param("dateFrom") LocalDate dateFrom,
                                   @Param("dateTo") LocalDate dateTo,
                                   @Param("type") OperationType type,
                                   @Param("categoryId") Long categoryId,
                                   @Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE " +
           "((t.userId = :userId AND t.family IS NULL) " +
           " OR t.family.id IN :familyIds) " +
           "AND (:dateFrom IS NULL OR t.date >= :dateFrom) " +
           "AND (:dateTo IS NULL OR t.date <= :dateTo) " +
           "ORDER BY t.date DESC")
    List<Transaction> findAllAccessible(@Param("userId") Long userId,
                                        @Param("familyIds") List<Long> familyIds,
                                        @Param("dateFrom") LocalDate dateFrom,
                                        @Param("dateTo") LocalDate dateTo);
}
