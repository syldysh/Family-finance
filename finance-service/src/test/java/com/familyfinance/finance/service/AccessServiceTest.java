package com.familyfinance.finance.service;

import com.familyfinance.common.exception.AccessDeniedException;
import com.familyfinance.finance.entity.FamilyRole;
import com.familyfinance.finance.repository.FamilyMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private FamilyMemberRepository memberRepository;

    @InjectMocks
    private AccessService accessService;

    @Test
    void checkFamilyMember_shouldPass() {
        when(memberRepository.existsByFamilyIdAndUserId(1L, 1L)).thenReturn(true);
        assertDoesNotThrow(() -> accessService.checkFamilyMember(1L, 1L));
    }

    @Test
    void checkFamilyMember_shouldThrow() {
        when(memberRepository.existsByFamilyIdAndUserId(1L, 2L)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> accessService.checkFamilyMember(1L, 2L));
    }

    @Test
    void checkFamilyOwner_shouldPass() {
        when(memberRepository.existsByFamilyIdAndUserIdAndRole(1L, 1L, FamilyRole.OWNER)).thenReturn(true);
        assertDoesNotThrow(() -> accessService.checkFamilyOwner(1L, 1L));
    }

    @Test
    void checkFamilyOwner_shouldThrowForMember() {
        when(memberRepository.existsByFamilyIdAndUserIdAndRole(1L, 2L, FamilyRole.OWNER)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> accessService.checkFamilyOwner(1L, 2L));
    }
}
