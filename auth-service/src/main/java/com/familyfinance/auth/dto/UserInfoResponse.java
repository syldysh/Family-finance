package com.familyfinance.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
