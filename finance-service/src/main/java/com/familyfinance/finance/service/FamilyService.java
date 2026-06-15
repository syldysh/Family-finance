package com.familyfinance.finance.service;

import com.familyfinance.common.exception.ConflictException;
import com.familyfinance.common.exception.NotFoundException;
import com.familyfinance.finance.client.AuthServiceClient;
import com.familyfinance.finance.dto.*;
import com.familyfinance.finance.entity.Family;
import com.familyfinance.finance.entity.FamilyMember;
import com.familyfinance.finance.entity.FamilyRole;
import com.familyfinance.finance.repository.FamilyMemberRepository;
import com.familyfinance.finance.repository.FamilyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository memberRepository;
    private final AccessService accessService;
    private final AuthServiceClient authServiceClient;

    public FamilyService(FamilyRepository familyRepository,
                         FamilyMemberRepository memberRepository,
                         AccessService accessService,
                         AuthServiceClient authServiceClient) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
        this.accessService = accessService;
        this.authServiceClient = authServiceClient;
    }

    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request, Long userId, String userEmail) {
        Family family = new Family();
        family.setName(request.getName());
        familyRepository.save(family);

        FamilyMember owner = new FamilyMember();
        owner.setFamily(family);
        owner.setUserId(userId);
        owner.setUserEmail(userEmail);
        owner.setRole(FamilyRole.OWNER);
        memberRepository.save(owner);

        family.getMembers().add(owner);
        return toResponse(family);
    }

    @Transactional(readOnly = true)
    public List<FamilyResponse> getUserFamilies(Long userId) {
        return familyRepository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FamilyResponse getFamilyById(Long familyId, Long userId) {
        accessService.checkFamilyMember(familyId, userId);
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new NotFoundException("Группа не найдена"));
        return toResponse(family);
    }

    @Transactional
    public MemberResponse addMember(Long familyId, AddMemberRequest request, Long userId, String token) {
        accessService.checkFamilyOwner(familyId, userId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new NotFoundException("Группа не найдена"));

        Map<String, Object> userInfo = authServiceClient.getUserByEmail(request.getEmail(), token);
        Long newUserId = ((Number) userInfo.get("id")).longValue();
        String email = (String) userInfo.get("email");

        if (memberRepository.existsByFamilyIdAndUserId(familyId, newUserId)) {
            throw new ConflictException("Пользователь уже является участником группы");
        }

        FamilyMember member = new FamilyMember();
        member.setFamily(family);
        member.setUserId(newUserId);
        member.setUserEmail(email);
        member.setRole(FamilyRole.MEMBER);
        memberRepository.save(member);

        return new MemberResponse(newUserId, email, FamilyRole.MEMBER.name());
    }

    @Transactional
    public void removeMember(Long familyId, Long memberUserId, Long currentUserId) {
        accessService.checkFamilyOwner(familyId, currentUserId);

        FamilyMember member = memberRepository.findByFamilyIdAndUserId(familyId, memberUserId)
                .orElseThrow(() -> new NotFoundException("Участник не найден в группе"));

        if (member.getRole() == FamilyRole.OWNER) {
            throw new ConflictException("Нельзя удалить владельца группы");
        }

        memberRepository.delete(member);
    }

    private FamilyResponse toResponse(Family family) {
        List<MemberResponse> members = family.getMembers().stream()
                .map(m -> new MemberResponse(m.getUserId(), m.getUserEmail(), m.getRole().name()))
                .toList();
        return new FamilyResponse(family.getId(), family.getName(), family.getCreatedAt(), members);
    }
}
