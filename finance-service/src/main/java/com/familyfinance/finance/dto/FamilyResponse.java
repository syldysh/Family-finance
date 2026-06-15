package com.familyfinance.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<MemberResponse> members;
}
