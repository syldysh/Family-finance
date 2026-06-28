package com.familyfinance.finance.repository;

import com.familyfinance.finance.entity.FamilyMember;
import com.familyfinance.finance.entity.FamilyRole;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, Long userId);

    List<FamilyMember> findAllByFamilyId(Long familyId);

    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    boolean existsByFamilyIdAndUserIdAndRole(Long familyId, Long userId, FamilyRole role);

    @Query("SELECT m FROM FamilyMember m WHERE m.family.id IN :familyIds")
    List<FamilyMember> findAllByFamilyIds(@Param("familyIds") Collection<Long> familyIds);
}
