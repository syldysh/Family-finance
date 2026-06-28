package com.familyfinance.finance.service;

import com.familyfinance.common.exception.AccessDeniedException;
import com.familyfinance.common.exception.ConflictException;
import com.familyfinance.common.exception.NotFoundException;
import com.familyfinance.finance.client.AuthServiceClient;
import com.familyfinance.finance.dto.AddMemberRequest;
import com.familyfinance.finance.dto.CreateFamilyRequest;
import com.familyfinance.finance.dto.FamilyResponse;
import com.familyfinance.finance.dto.MemberResponse;
import com.familyfinance.finance.entity.Family;
import com.familyfinance.finance.entity.FamilyMember;
import com.familyfinance.finance.entity.FamilyRole;
import com.familyfinance.finance.repository.FamilyMemberRepository;
import com.familyfinance.finance.repository.FamilyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilyMemberRepository memberRepository;

    @Mock
    private AccessService accessService;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private FamilyService familyService;

    @Test
    void createFamily_shouldCreateFamilyWithOwner() {
        CreateFamilyRequest request = new CreateFamilyRequest("Семья Ивановых");
        when(familyRepository.save(any(Family.class))).thenAnswer(inv -> {
            Family f = inv.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });
        when(memberRepository.save(any(FamilyMember.class))).thenAnswer(inv -> inv.getArgument(0));

        FamilyResponse response = familyService.createFamily(request, 10L, "ivan@test.com");

        assertEquals("Семья Ивановых", response.getName());
        assertEquals(1, response.getMembers().size());
        assertEquals("OWNER", response.getMembers().get(0).getRole());
        assertEquals("ivan@test.com", response.getMembers().get(0).getUserEmail());
        verify(familyRepository).save(any(Family.class));
        verify(memberRepository).save(any(FamilyMember.class));
    }

    @Test
    void addMember_shouldAddMemberWhenOwner() {
        Family family = new Family();
        family.setId(1L);
        family.setName("Тест");

        AddMemberRequest request = new AddMemberRequest("new@test.com");
        when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
        when(authServiceClient.getUserByEmail("new@test.com", "token"))
                .thenReturn(Map.of("id", 20, "email", "new@test.com"));
        when(memberRepository.existsByFamilyIdAndUserId(1L, 20L)).thenReturn(false);
        when(memberRepository.save(any(FamilyMember.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberResponse response = familyService.addMember(1L, request, 10L, "token");

        assertEquals(20L, response.getUserId());
        assertEquals("new@test.com", response.getUserEmail());
        assertEquals("MEMBER", response.getRole());
        verify(accessService).checkFamilyOwner(1L, 10L);
    }

    @Test
    void addMember_shouldThrowIfAlreadyMember() {
        Family family = new Family();
        family.setId(1L);

        AddMemberRequest request = new AddMemberRequest("existing@test.com");
        when(familyRepository.findById(1L)).thenReturn(Optional.of(family));
        when(authServiceClient.getUserByEmail("existing@test.com", "token"))
                .thenReturn(Map.of("id", 20, "email", "existing@test.com"));
        when(memberRepository.existsByFamilyIdAndUserId(1L, 20L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> familyService.addMember(1L, request, 10L, "token"));
    }

    @Test
    void addMember_shouldThrowIfNotOwner() {
        AddMemberRequest request = new AddMemberRequest("new@test.com");
        doThrow(new AccessDeniedException("Только владелец может выполнять это действие"))
                .when(accessService).checkFamilyOwner(1L, 99L);

        assertThrows(AccessDeniedException.class,
                () -> familyService.addMember(1L, request, 99L, "token"));
    }

    @Test
    void removeMember_shouldRemoveNonOwner() {
        FamilyMember member = new FamilyMember();
        member.setUserId(20L);
        member.setRole(FamilyRole.MEMBER);
        when(memberRepository.findByFamilyIdAndUserId(1L, 20L)).thenReturn(Optional.of(member));

        familyService.removeMember(1L, 20L, 10L);

        verify(accessService).checkFamilyOwner(1L, 10L);
        verify(memberRepository).delete(member);
    }

    @Test
    void removeMember_shouldThrowForOwner() {
        FamilyMember owner = new FamilyMember();
        owner.setUserId(10L);
        owner.setRole(FamilyRole.OWNER);
        when(memberRepository.findByFamilyIdAndUserId(1L, 10L)).thenReturn(Optional.of(owner));

        assertThrows(ConflictException.class,
                () -> familyService.removeMember(1L, 10L, 10L));
    }

    @Test
    void removeMember_shouldThrowIfNotFound() {
        when(memberRepository.findByFamilyIdAndUserId(1L, 999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> familyService.removeMember(1L, 999L, 10L));
    }

    @Test
    void getFamilyById_shouldReturnFamilyForMember() {
        Family family = new Family();
        family.setId(1L);
        family.setName("Тест");
        family.setCreatedAt(LocalDateTime.now());

        FamilyMember member = new FamilyMember();
        member.setUserId(10L);
        member.setUserEmail("user@test.com");
        member.setRole(FamilyRole.OWNER);
        family.getMembers().add(member);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(family));

        FamilyResponse response = familyService.getFamilyById(1L, 10L);

        assertEquals("Тест", response.getName());
        assertEquals(1, response.getMembers().size());
        verify(accessService).checkFamilyMember(1L, 10L);
    }

    @Test
    void getUserFamilies_shouldReturnList() {
        Family f1 = new Family();
        f1.setId(1L);
        f1.setName("Семья 1");
        f1.setCreatedAt(LocalDateTime.now());

        Family f2 = new Family();
        f2.setId(2L);
        f2.setName("Семья 2");
        f2.setCreatedAt(LocalDateTime.now());

        when(familyRepository.findAllByUserId(10L)).thenReturn(List.of(f1, f2));

        List<FamilyResponse> families = familyService.getUserFamilies(10L);

        assertEquals(2, families.size());
        assertEquals("Семья 1", families.get(0).getName());
        assertEquals("Семья 2", families.get(1).getName());
    }
}
