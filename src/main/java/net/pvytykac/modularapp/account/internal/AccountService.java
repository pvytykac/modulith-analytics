package net.pvytykac.modularapp.account.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.account.events.AccountCreatedEvent;
import net.pvytykac.modularapp.account.events.AccountDeletedEvent;
import net.pvytykac.modularapp.account.events.AccountUpdatedEvent;
import net.pvytykac.modularapp.application.api.ApplicationApi;
import net.pvytykac.modularapp.license.api.LicensePriceApi;
import net.pvytykac.modularapp.license.api.LicensePriceInfo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class AccountService {

    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final ApplicationApi helper;
    private final LicensePriceApi licensePriceApi;
    private final ApplicationEventPublisher publisher;

    Page<Account> list(String applicationId, Pageable pageable) {
        log.debug("Listing application accounts of application '{}' page '{}' of size '{}'", applicationId,
                pageable.getPageNumber(), pageable.getPageSize());

        return helper.mapIfApplicationExists(applicationId,
                () -> repository.findAllByApplicationId(applicationId, pageable).map(mapper::entityToRepresentation));
    }

    Account require(String applicationId, String accountId) {
        log.debug("Looking up required account of application '{}' with userId '{}'", applicationId, accountId);

        return helper.mapIfApplicationExists(applicationId, () -> repository.findByApplicationIdAndId(
                        applicationId, accountId).map(mapper::entityToRepresentation))
                .orElseThrow();
    }

    Account create(String applicationId, String userId, String externalId, Instant lastUsed, Set<String> licenseIds) {
        log.info("Creating application account of application '{}' and userId '{}' with externalId '{}' and lastUsed '{}'",
                applicationId, userId, externalId, lastUsed);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.save(AccountEntity.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .externalId(externalId)
                    .lastUsed(lastUsed)
                    .licenseIds(licenseIds)
                    .build());

            var licenses = fetchLicenses(applicationId, licenseIds);

            publisher.publishEvent(new AccountCreatedEvent(entity.getApplicationId(), entity.getUserId(),
                    entity.getId(), entity.getExternalId(), entity.getLastUsed(), licenses, Instant.now()));

            return mapper.entityToRepresentation(entity);
        });
    }

    Account update(String applicationId, String accountId, Instant lastUsed, Set<String> licenseIds) {
        log.info("Updating lastUsed of application account '{}' of application '{}' to '{}'",
                accountId, applicationId, lastUsed);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.findByApplicationIdAndIdForUpdate(applicationId, accountId).orElseThrow();
            var assignedLicenseIds = diff(licenseIds, entity.getLicenseIds());
            var revokedLicensesIds = diff(entity.getLicenseIds(), licenseIds);
            var assignedLicenses = fetchLicenses(applicationId, assignedLicenseIds);
            var revokedLicenses = fetchLicenses(applicationId, revokedLicensesIds);

            entity.setLastUsed(lastUsed);
            entity.setLicenseIds(licenseIds);

            publisher.publishEvent(new AccountUpdatedEvent(entity.getApplicationId(), entity.getUserId(),
                    entity.getId(), entity.getExternalId(), entity.getLastUsed(), assignedLicenses, revokedLicenses,
                    Instant.now()));

            return mapper.entityToRepresentation(repository.save(entity));
        });
    }

    Account delete(String applicationId, String accountId) {
        log.info("Deleting application account of application '{}' with userId '{}'", applicationId, accountId);

        return helper.mapIfApplicationExists(applicationId, () -> {
            var entity = repository.findByApplicationIdAndIdForUpdate(applicationId, accountId);

            entity.ifPresent(e -> {
                repository.delete(e);
                var licenses = fetchLicenses(applicationId, e.getLicenseIds());

                publisher.publishEvent(new AccountDeletedEvent(e.getApplicationId(), e.getUserId(),
                        e.getId(), e.getExternalId(), e.getLastUsed(), licenses, Instant.now()));
            });

            return entity.map(mapper::entityToRepresentation).orElseThrow();
        });
    }

    private Collection<LicensePriceInfo> fetchLicenses(String applicationId, Set<String> licenseIds) {
        return licensePriceApi.getAllOrFallback(applicationId, licenseIds).values();
    }

    private <T extends Comparable<T>> Set<T> diff(Set<T> left, Set<T> right) {
        return left.stream()
                .filter(item -> !right.contains(item))
                .collect(Collectors.toSet());
    }
}
