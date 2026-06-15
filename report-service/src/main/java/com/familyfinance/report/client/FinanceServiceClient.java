package com.familyfinance.report.client;

import com.familyfinance.common.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class FinanceServiceClient {

    private final RestClient restClient;

    public FinanceServiceClient(@Value("${services.finance.url}") String financeUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(financeUrl)
                .build();
    }

    public List<Map<String, Object>> getPersonalTransactions(Long userId, LocalDate from, LocalDate to) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromPath("/api/internal/transactions/personal")
                    .queryParam("userId", userId);
            if (from != null) builder.queryParam("from", from);
            if (to != null) builder.queryParam("to", to);

            return restClient.get()
                    .uri(builder.toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }

    public List<Map<String, Object>> getFamilyTransactions(Long familyId, Long userId,
                                                            LocalDate from, LocalDate to) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromPath("/api/internal/transactions/family/{familyId}")
                    .queryParam("userId", userId);
            if (from != null) builder.queryParam("from", from);
            if (to != null) builder.queryParam("to", to);

            return restClient.get()
                    .uri(builder.buildAndExpand(familyId).toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }

    public List<Map<String, Object>> getAllAccessible(Long userId, List<Long> familyIds,
                                                      LocalDate from, LocalDate to) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromPath("/api/internal/transactions/all")
                    .queryParam("userId", userId)
                    .queryParam("familyIds", familyIds);
            if (from != null) builder.queryParam("from", from);
            if (to != null) builder.queryParam("to", to);

            return restClient.get()
                    .uri(builder.toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }
}
