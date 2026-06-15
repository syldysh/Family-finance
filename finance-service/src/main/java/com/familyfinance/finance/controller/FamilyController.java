package com.familyfinance.finance.controller;

import com.familyfinance.common.security.UserPrincipal;
import com.familyfinance.finance.dto.*;
import com.familyfinance.finance.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families")
@Tag(name = "Семьи / группы", description = "Управление финансовыми группами")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping
    @Operation(summary = "Создать группу")
    public ResponseEntity<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        FamilyResponse response = familyService.createFamily(request, principal.getUserId(), principal.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Получить список групп пользователя")
    public ResponseEntity<List<FamilyResponse>> getUserFamilies(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(familyService.getUserFamilies(principal.getUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить группу по ID")
    public ResponseEntity<FamilyResponse> getFamily(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(familyService.getFamilyById(id, principal.getUserId()));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Добавить участника в группу")
    public ResponseEntity<MemberResponse> addMember(@PathVariable Long id,
                                                    @Valid @RequestBody AddMemberRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        MemberResponse response = familyService.addMember(id, request, principal.getUserId(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{familyId}/members/{userId}")
    @Operation(summary = "Удалить участника из группы")
    public ResponseEntity<Void> removeMember(@PathVariable Long familyId,
                                             @PathVariable Long userId,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        familyService.removeMember(familyId, userId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
