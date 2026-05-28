package net.pvytykac.modularapp.analytics.internal.dimension.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ApplicationDimensionService {

    private final ApplicationDimensionRepository repository;

    public String getDimensionKey(String applicationId) {
        log.debug("Looking up application dimension key for application id '{}'", applicationId);

        return repository.findByApplicationId(applicationId)
                .map(ApplicationDimension::getId)
                .orElseThrow();
    }

}
