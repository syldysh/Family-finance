package com.familyfinance.finance.controller;

import com.familyfinance.common.exception.ServiceUnavailableException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Прокси к сервису отчётов")
public class ReportProxyController {

    private final RestClient restClient;

    public ReportProxyController(@Value("${services.report.url}") String reportUrl) {
        this.restClient = RestClient.builder().baseUrl(reportUrl).build();
    }

    @PostMapping("/personal")
    @Operation(summary = "Личный отчёт")
    public ResponseEntity<Map<String, Object>> personal(@RequestBody Map<String, Object> body,
                                                        HttpServletRequest request) {
        return forwardJson("/api/reports/personal", body, request);
    }

    @PostMapping("/family")
    @Operation(summary = "Отчёт по группе")
    public ResponseEntity<Map<String, Object>> family(@RequestBody Map<String, Object> body,
                                                      HttpServletRequest request) {
        return forwardJson("/api/reports/family", body, request);
    }

    @PostMapping("/full")
    @Operation(summary = "Полный отчёт по всем доступным операциям")
    public ResponseEntity<Map<String, Object>> full(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        return forwardJson("/api/reports/full", body, request);
    }

    @PostMapping("/personal/export")
    @Operation(summary = "Экспорт личного отчёта в CSV")
    public ResponseEntity<byte[]> personalExport(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest request) {
        return forwardCsv("/api/reports/personal/export", body, request);
    }

    @PostMapping("/family/export")
    @Operation(summary = "Экспорт отчёта по группе в CSV")
    public ResponseEntity<byte[]> familyExport(@RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        return forwardCsv("/api/reports/family/export", body, request);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> forwardJson(String path, Map<String, Object> body,
                                                            HttpServletRequest request) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return ResponseEntity.ok(response);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис отчётов недоступен");
        }
    }

    private ResponseEntity<byte[]> forwardCsv(String path, Map<String, Object> body,
                                              HttpServletRequest request) {
        try {
            byte[] csv = restClient.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csv);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Сервис отчётов недоступен");
        }
    }
}
