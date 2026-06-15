package com.familyfinance.finance.repository;

import com.familyfinance.finance.entity.Category;
import com.familyfinance.finance.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.userId = :userId OR c.userId IS NULL")
    List<Category> findAvailableForUser(@Param("userId") Long userId);

    List<Category> findByType(OperationType type);

    @Query("SELECT c FROM Category c WHERE (c.userId = :userId OR c.userId IS NULL) AND c.type = :type")
    List<Category> findAvailableForUserByType(@Param("userId") Long userId, @Param("type") OperationType type);
}
