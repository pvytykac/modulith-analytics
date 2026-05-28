package net.pvytykac.modularapp.analytics.internal.dimension.license;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.license.events.LicenseCreatedEvent;
import net.pvytykac.modularapp.license.events.LicenseDeletedEvent;
import net.pvytykac.modularapp.license.events.LicenseUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
@Slf4j
class LicenseDimensionEventHandlers {

    private final LicenseDimensionRepository repository;

    @EventListener(LicenseCreatedEvent.class)
    void onLicenseCreated(LicenseCreatedEvent event) {
        log.info("Creating an analytics dimension for license '{}'", event.applicationId());

        repository.save(LicenseDimension.builder()
                .applicationId(event.applicationId())
                .licenseId(event.licenseId())
                .name(event.name())
                .cost(event.monthlyPrice())
                .build());
    }

    @EventListener(LicenseUpdatedEvent.class)
    void onLicenseUpdated(LicenseUpdatedEvent event) {
        log.info("Updating the analytics dimension of license '{}'", event.licenseId());

        repository.findByLicenseIdForUpdate(event.licenseId())
                .ifPresent(d -> {
                    d.setName(event.name());
                    d.setCost(event.monthlyPrice());
                });
    }

    @EventListener(LicenseDeletedEvent.class)
    void onLicenseDeleted(LicenseDeletedEvent event) {
        log.info("Deactivating the analytics dimension of deleted license '{}'", event.licenseId());

        repository.deactivateByLicenseId(event.licenseId());
    }

    @EventListener(ApplicationDeletedEvent.class)
    void onApplicationDeleted(ApplicationDeletedEvent event) {
        log.info("Deactivating all analytics dimensions of deleted application '{}'", event.applicationId());

        repository.deactivateByApplicationId(event.applicationId());
    }

}
