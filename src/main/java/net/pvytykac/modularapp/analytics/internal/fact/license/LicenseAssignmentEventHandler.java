package net.pvytykac.modularapp.analytics.internal.fact.license;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.account.events.AccountCreatedEvent;
import net.pvytykac.modularapp.account.events.AccountDeletedEvent;
import net.pvytykac.modularapp.account.events.AccountUpdatedEvent;
import net.pvytykac.modularapp.analytics.internal.dimension.DimensionFacade;
import net.pvytykac.modularapp.analytics.internal.fact.license.LicenseAssignmentFact.AssignmentEventType;
import net.pvytykac.modularapp.license.api.LicensePriceInfo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
class LicenseAssignmentEventHandler {

    private final LicenseAssignmentFactRepository repository;
    private final DimensionFacade dimensionFacade;

    @EventListener(AccountCreatedEvent.class)
    void onAccountCreated(AccountCreatedEvent event) {
        log.info("Saving assigned license events for account '{}'", event.accountId());

        var facts = factBuilder(event.applicationId(), event.accountId(), event.userId(), event.licenses(),
                AssignmentEventType.ASSIGNED, event.timestamp())
                .toList();

        repository.saveAll(facts);
    }

    @EventListener(AccountUpdatedEvent.class)
    void onAccountUpdated(AccountUpdatedEvent event) {
        log.info("Saving assigned and revoked license events for account '{}'", event.accountId());

        var facts = Stream.concat(
                factBuilder(event.applicationId(), event.accountId(), event.userId(), event.assignedLicenses(), AssignmentEventType.ASSIGNED, event.timestamp()),
                factBuilder(event.applicationId(), event.accountId(), event.userId(), event.revokedLicenses(), AssignmentEventType.REVOKED, event.timestamp())
        ).toList();

        repository.saveAll(facts);
    }

    @EventListener(AccountDeletedEvent.class)
    void onAccountDeleted(AccountDeletedEvent event) {
        log.info("Saving revoked license events for account '{}'", event.accountId());

        var facts = factBuilder(event.applicationId(), event.accountId(), event.userId(), event.licenses(),
                AssignmentEventType.REVOKED, event.timestamp())
                .toList();

        repository.saveAll(facts);
    }

    private Stream<LicenseAssignmentFact> factBuilder(String applicationId, String accountId, String userId,
                                                      Collection<LicensePriceInfo> licenses, AssignmentEventType eventType,
                                                      Instant assignmentTime) {
        return licenses.stream()
                .map(license -> LicenseAssignmentFact.builder()
                        .applicationDimensionKey(dimensionFacade.getApplicationDimensionKey(applicationId))
                        .accountDimensionKey(dimensionFacade.getAccountDimensionKey(accountId))
                        .userDimensionKey(dimensionFacade.getUserDimensionKey(userId))
                        .licenseDimensionKey(dimensionFacade.getLicenseDimensionKey(license.id()))
                        .licenseMonthlyPrice(license.monthlyPrice())
                        .assignmentTime(assignmentTime)
                        .eventType(eventType)
                        .build());
    }

}
