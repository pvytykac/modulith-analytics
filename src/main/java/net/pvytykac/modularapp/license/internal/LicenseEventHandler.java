package net.pvytykac.modularapp.license.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LicenseEventHandler {

    private final LicenseRepository licenseRepository;

    @EventListener(ApplicationDeletedEvent.class)
    void onApplicationDeleted(ApplicationDeletedEvent event) {
        log.info("Deleting all application licenses of the deleted application '{}'", event.applicationId());

        licenseRepository.deleteAllByApplicationId(event.applicationId());
    }

}
