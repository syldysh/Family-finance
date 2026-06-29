package com.familyfinance.report.client;

import com.familyfinance.common.exception.ServiceUnavailableException;
import io.github.resilience4j.retry.annotation.Retry;
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

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String internalToken;

    public FinanceServiceClient(@Value("${services.finance.url}") String financeUrl,
                                @Value("${internal.token}") String internalToken) {
        this.restClient = RestClient.builder()
                .baseUrl(financeUrl)
                .build();
        this.internalToken = internalToken;
    }

    @Retry(name = "financeService")
    public List<Map<String, Object>> getPersonalTransactions(Long userId, LocalDate from, LocalDate to) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromPath("/api/internal/transactions/personal")
                    .queryParam("userId", userId);
            if (from != null) builder.queryParam("from", from);
            if (to != null) builder.queryParam("to", to);

            return restClient.get()
                    .uri(builder.toUriString())
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }

    @Retry(name = "financeService")
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
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }

    @Retry(name = "financeService")
    public List<Map<String, Object>> getAllAccessible(Long userId, List<Long> familyIds,
                                                      LocalDate from, LocalDate to) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromPath("/api/internal/transactions/all")
                    .queryParam("userId", userId);
            if (familyIds != null && !familyIds.isEmpty()) {
                builder.queryParam("familyIds", familyIds);
            }
            if (from != null) builder.queryParam("from", from);
            if (to != null) builder.queryParam("to", to);

            return restClient.get()
                    .uri(builder.toUriString())
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис финансов недоступен");
        }
    }
}
