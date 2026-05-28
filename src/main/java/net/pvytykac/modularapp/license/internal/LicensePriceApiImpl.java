package net.pvytykac.modularapp.license.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.license.api.LicensePriceApi;
import net.pvytykac.modularapp.license.api.LicensePriceInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
class LicensePriceApiImpl implements LicensePriceApi {

    private final LicenseService service;

    @Override
    public LicensePriceInfo getOneOrFallback(String applicationId, String licenseId) {
        return Optional.ofNullable(service.get(applicationId, licenseId))
                .map(LicensePriceInfo.class::cast)
                .orElseGet(() -> fallback(licenseId));
    }

    @Override
    public Map<String, LicensePriceInfo> getAllOrFallback(String applicationId, Collection<String> licenseIds) {
        return licenseIds.stream()
                .map(licenseId -> getOneOrFallback(applicationId, licenseId))
                .collect(Collectors.toMap(LicensePriceInfo::id, Function.identity()));
    }

    private static LicensePriceInfo fallback(String licenseId) {
        return new License(licenseId, "", BigDecimal.ZERO);
    }
}
