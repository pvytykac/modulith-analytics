package net.pvytykac.modularapp.license.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/applications/{applicationId}/licenses")
@RequiredArgsConstructor
class LicenseController {

    private final LicenseService service;

    @PostMapping(consumes = "application/json")
    License create(@PathVariable String applicationId, @RequestBody @Validated PostRequest payload) {
        return service.create(applicationId, payload.name(), payload.monthlyPrice());
    }

    @GetMapping
    Page<License> list(@PathVariable String applicationId, Pageable pageable) {
        return service.list(applicationId, pageable);
    }

    @GetMapping("/{licenseId}")
    License get(@PathVariable String applicationId, @PathVariable String licenseId) {
        return service.require(applicationId, licenseId);
    }

    @PutMapping("/{licenseId}")
    License update(@PathVariable String applicationId, @PathVariable String licenseId, @RequestBody @Validated PutRequest payload) {
        return service.update(applicationId, licenseId, payload.name(), payload.monthlyPrice());
    }

    @DeleteMapping("/{licenseId}")
    License delete(@PathVariable String applicationId, @PathVariable String licenseId) {
        return service.delete(applicationId, licenseId);
    }

    record PostRequest(@NotBlank String name, @PositiveOrZero BigDecimal monthlyPrice) {
    }

    record PutRequest(@NotBlank String name, @PositiveOrZero BigDecimal monthlyPrice) {
    }

}
