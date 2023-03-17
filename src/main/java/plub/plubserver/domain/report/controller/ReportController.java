package plub.plubserver.domain.report.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import plub.plubserver.common.dto.ApiResponse;
import plub.plubserver.domain.report.dto.ReportDto.*;
import plub.plubserver.domain.report.service.ReportService;

import java.util.List;

import static plub.plubserver.common.dto.ApiResponse.success;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Api(tags = "신고 API", hidden = true)
public class ReportController {
    private final ReportService reportService;

    @ApiOperation(value = "신고 사유 조회")
    @GetMapping()
    public ApiResponse<List<ReportTypeResponse>> getReportType(
    ) {
        return success(reportService.getReportType());
    }
}