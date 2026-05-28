package net.pvytykac.modularapp.license.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.api.ApplicationApi;
import net.pvytykac.modularapp.license.events.LicenseCreatedEvent;
import net.pvytykac.modularapp.license.events.LicenseDeletedEvent;
import net.pvytykac.modularapp.license.events.LicenseUpdatedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
class LicenseService {

    private final LicenseRepository repository;
    private final LicenseMapper mapper;
    private final ApplicationApi helper;
    private final ApplicationEventPublisher publisher;

    Page<License> list(String applicationId, Pageable pageable) {
        log.debug("Listing application licenses of application '{}' page '{}' of size '{}'", applicationId,
                pageable.getPageNumber(), pageable.getPageSize());

        return helper.mapIfApplicationExists(applicationId,
                () -> repository.findAllByApplicationId(applicationId, pageable).map(mapper::entityToRepresentation));
    }

    @Cacheable(cacheNames = "licenses", key = "#applicationId+#licenseId", unless = "#result == null")
    public @Nullable License get(String applicationId, String licenseId) {
        log.debug("Looking up optional license of application '{}' with licenseId '{}'", applicationId, licenseId);

        return helper.mapIfApplicationExists(applicationId, () -> repository.findByApplicationIdAndId(
                        applicationId, licenseId))
                .map(mapper::entityToRepresentation)
                .orElse(null);
    }

    @Cacheable(cacheNames = "licenses", key = "#applicationId+#licenseId")
    public License require(String applicationId, String licenseId) {
        log.debug("Looking up required license of application '{}' with licenseId '{}'", applicationId, licenseId);

        return helper.mapIfApplicationExists(applicationId, () -> repository.findByApplicationIdAndId(
                        applicationId, licenseId).map(mapper::entityToRepresentation))
                .orElseThrow();
    }

    License create(String applicationId, String name, BigDecimal monthlyPrice) {
        log.info("Creating application license of application '{}' with name '{}' and price '{}'", applicationId, name, monthlyPrice);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.save(LicenseEntity.builder()
                    .applicationId(applicationId)
                    .name(name)
                    .monthlyPrice(monthlyPrice)
                    .build());

            publisher.publishEvent(new LicenseCreatedEvent(applicationId, entity.getId(), entity.getName(),
                    entity.getMonthlyPrice()));

            return mapper.entityToRepresentation(entity);
        });
    }

    @CachePut(cacheNames = "licenses", key = "#applicationId+#licenseId")
    public License update(String applicationId, String licenseId, String name, BigDecimal monthlyPrice) {
        log.info("Updating name and price of application license '{}' of application '{}' to '{}' and '{}'",
                licenseId, applicationId, name, monthlyPrice);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.findByApplicationIdAndIdForUpdate(applicationId, licenseId).orElseThrow();

            entity.setName(name);
            entity.setMonthlyPrice(monthlyPrice);
            publisher.publishEvent(new LicenseUpdatedEvent(applicationId, licenseId, name, monthlyPrice));

            return mapper.entityToRepresentation(repository.save(entity));
        });
    }

    @CacheEvict(cacheNames = "licenses", key = "#applicationId+#licenseId")
    public License delete(String applicationId, String licenseId) {
        log.info("Deleting application license of application '{}' with licenseId '{}'", applicationId, licenseId);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.findByApplicationIdAndIdForUpdate(applicationId, licenseId);

            entity.ifPresent(e -> {
                repository.delete(e);
                publisher.publishEvent(new LicenseDeletedEvent(applicationId, licenseId, e.getName(), e.getMonthlyPrice()));
            });

            return entity.map(mapper::entityToRepresentation).orElseThrow();
        });
    }
}
