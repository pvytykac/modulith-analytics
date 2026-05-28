package net.pvytykac.modularapp.analytics.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/v1/applications/{applicationId}/analytics/licenseAssignments")
@RequiredArgsConstructor
class ApplicationAnalyticsController {

    private final LicenseAssignmentRepository repository;

    @GetMapping("/dailyBreakdown")
    public ApplicationDailyBreakdownResponse getApplicationLicenseAssignmentBreakdown(
            @PathVariable String applicationId,
            @RequestParam(value = "startTime") Instant startTime,
            @RequestParam(value = "endTime") Instant endTime,
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "licenseId", required = false) String licenseId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "minApplicationCreatedAt", required = false) Instant minApplicationCreatedAt,
            @RequestParam(value = "maxApplicationCreatedAt", required = false) Instant maxApplicationCreatedAt,
            @RequestParam(value = "minLicenseCost", required = false) BigDecimal minLicenseCost,
            @RequestParam(value = "maxLicenseCost", required = false) BigDecimal maxLicenseCost,
            @RequestParam(value = "minLastUsed", required = false) Instant minLastUsed,
            @RequestParam(value = "maxLastUsed", required = false) Instant maxLastUsed) {
        var content = repository.getDailyLicenseAssignmentBreakdown(startTime, endTime, applicationId, accountId,
                userId, licenseId, email, minLicenseCost, maxLicenseCost, minApplicationCreatedAt,
                maxApplicationCreatedAt, minLastUsed, maxLastUsed);

        return new ApplicationDailyBreakdownResponse(startTime, endTime, applicationId, content);
    }

    @GetMapping("/statistics")
    public Page<LicenseAssignmentStatistics> getApplicationLicenseAssignmentBreakdown(
            @PathVariable String applicationId,
            @RequestParam(value = "startTime") Instant startTime,
            @RequestParam(value = "endTime") Instant endTime,
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "licenseId", required = false) String licenseId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "minApplicationCreatedAt", required = false) Instant minApplicationCreatedAt,
            @RequestParam(value = "maxApplicationCreatedAt", required = false) Instant maxApplicationCreatedAt,
            @RequestParam(value = "minLicenseCost", required = false) BigDecimal minLicenseCost,
            @RequestParam(value = "maxLicenseCost", required = false) BigDecimal maxLicenseCost,
            @RequestParam(value = "minLastUsed", required = false) Instant minLastUsed,
            @RequestParam(value = "maxLastUsed", required = false) Instant maxLastUsed,
            Pageable pageable) {
        return repository.getLicenseAssignmentStatistics(startTime, endTime, applicationId, accountId,
                userId, licenseId, email, minLicenseCost, maxLicenseCost, minApplicationCreatedAt,
                maxApplicationCreatedAt, minLastUsed, maxLastUsed, pageable);
    }

    record ApplicationDailyBreakdownResponse(Instant startTime, Instant endTime, String applicationId,
                                             List<LicenseAssignmentBreakdown> content) {
    }
}
