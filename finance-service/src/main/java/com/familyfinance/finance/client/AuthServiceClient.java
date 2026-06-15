package com.familyfinance.finance.client;

import com.familyfinance.common.exception.NotFoundException;
import com.familyfinance.common.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(@Value("${services.auth.url}") String authUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authUrl)
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserByEmail(String email, String token) {
        try {
            return restClient.get()
                    .uri("/api/auth/user/email/{email}", email)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new NotFoundException("Пользователь с email " + email + " не найден");
            }
            throw new ServiceUnavailableException("Сервис авторизации недоступен");
        }
    }
}
