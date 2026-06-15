package com.familyfinance.finance.repository;

import com.familyfinance.finance.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    @Query("SELECT f FROM Family f JOIN f.members m WHERE m.userId = :userId")
    List<Family> findAllByUserId(@Param("userId") Long userId);
}
