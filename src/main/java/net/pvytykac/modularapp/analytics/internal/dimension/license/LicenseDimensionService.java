package net.pvytykac.modularapp.analytics.internal.dimension.license;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LicenseDimensionService {

    private final LicenseDimensionRepository repository;

    public String getDimensionKey(String licenseId) {
        log.debug("Looking up license dimension key for license id '{}'", licenseId);

        return repository.findByLicenseId(licenseId)
                .map(LicenseDimension::getId)
                .orElseThrow();
    }

}
