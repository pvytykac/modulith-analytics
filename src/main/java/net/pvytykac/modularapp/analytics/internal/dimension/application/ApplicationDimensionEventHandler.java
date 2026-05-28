package net.pvytykac.modularapp.analytics.internal.dimension.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.events.ApplicationCreatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.application.events.ApplicationUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
@Slf4j
class ApplicationDimensionEventHandler {

    private final ApplicationDimensionRepository repository;

    @EventListener(ApplicationCreatedEvent.class)
    void onApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Creating an analytics dimension for application '{}'", event.applicationId());

        repository.save(ApplicationDimension.builder()
                .applicationId(event.applicationId())
                .name(event.name())
                .createdAt(event.createdAt())
                .build());
    }

    @EventListener(ApplicationUpdatedEvent.class)
    void onApplicationUpdated(ApplicationUpdatedEvent event) {
        log.info("Updating the analytics dimension of application '{}'", event.applicationId());

        repository.findByIdForUpdate(event.applicationId())
                .ifPresent(d -> d.setName(event.name()));
    }

    @EventListener(ApplicationDeletedEvent.class)
    void onApplicationDeleted(ApplicationDeletedEvent event) {
        log.info("Deactivating the analytics dimension of deleted application '{}'", event.applicationId());

        repository.deactivateByApplicationId(event.applicationId());
    }

}
