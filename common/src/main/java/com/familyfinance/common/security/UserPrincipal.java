package com.familyfinance.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private final Long userId;
    private final String email;
    private final List<String> roles;
}
