package com.familyfinance.report.controller;

import com.familyfinance.common.security.UserPrincipal;
import com.familyfinance.report.dto.ReportRequest;
import com.familyfinance.report.dto.ReportResponse;
import com.familyfinance.report.service.CsvExportService;
import com.familyfinance.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчеты", description = "Построение и экспорт отчетов")
public class ReportController {

    private final ReportService reportService;
    private final CsvExportService csvExportService;

    public ReportController(ReportService reportService, CsvExportService csvExportService) {
        this.reportService = reportService;
        this.csvExportService = csvExportService;
    }

    @PostMapping("/personal")
    @Operation(summary = "Отчет по личным операциям")
    public ResponseEntity<ReportResponse> personalReport(@Valid @RequestBody ReportRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reportService.buildPersonalReport(principal.getUserId(), request));
    }

    @PostMapping("/family")
    @Operation(summary = "Отчет по группе")
    public ResponseEntity<ReportResponse> familyReport(@Valid @RequestBody ReportRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reportService.buildFamilyReport(principal.getUserId(), request));
    }

    @PostMapping("/full")
    @Operation(summary = "Полный отчет по всем доступным операциям")
    public ResponseEntity<ReportResponse> fullReport(@Valid @RequestBody ReportRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reportService.buildFullReport(principal.getUserId(), request));
    }

    @PostMapping("/personal/export")
    @Operation(summary = "Экспорт личного отчета в CSV")
    public ResponseEntity<byte[]> exportPersonalReport(@Valid @RequestBody ReportRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        ReportResponse report = reportService.buildPersonalReport(principal.getUserId(), request);
        return buildCsvResponse(report);
    }

    @PostMapping("/family/export")
    @Operation(summary = "Экспорт отчета по группе в CSV")
    public ResponseEntity<byte[]> exportFamilyReport(@Valid @RequestBody ReportRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        ReportResponse report = reportService.buildFamilyReport(principal.getUserId(), request);
        return buildCsvResponse(report);
    }

    private ResponseEntity<byte[]> buildCsvResponse(ReportResponse report) {
        byte[] csv = csvExportService.exportReport(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
