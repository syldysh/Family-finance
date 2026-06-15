package com.familyfinance.finance.service;

import com.familyfinance.common.exception.AccessDeniedException;
import com.familyfinance.finance.entity.FamilyRole;
import com.familyfinance.finance.repository.FamilyMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    private final FamilyMemberRepository familyMemberRepository;

    public AccessService(FamilyMemberRepository familyMemberRepository) {
        this.familyMemberRepository = familyMemberRepository;
    }

    public void checkFamilyMember(Long familyId, Long userId) {
        if (!familyMemberRepository.existsByFamilyIdAndUserId(familyId, userId)) {
            throw new AccessDeniedException("Вы не являетесь участником этой группы");
        }
    }

    public void checkFamilyOwner(Long familyId, Long userId) {
        if (!familyMemberRepository.existsByFamilyIdAndUserIdAndRole(familyId, userId, FamilyRole.OWNER)) {
            throw new AccessDeniedException("Только владелец может выполнять это действие");
        }
    }
}
