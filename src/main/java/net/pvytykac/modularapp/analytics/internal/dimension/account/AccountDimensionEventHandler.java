package net.pvytykac.modularapp.analytics.internal.dimension.account;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.account.events.AccountCreatedEvent;
import net.pvytykac.modularapp.account.events.AccountDeletedEvent;
import net.pvytykac.modularapp.account.events.AccountUpdatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
@Slf4j
class AccountDimensionEventHandler {

    private final AccountDimensionRepository repository;

    @EventListener(AccountCreatedEvent.class)
    void onAccountCreated(AccountCreatedEvent event) {
        log.info("Creating an analytics dimension for account '{}'", event.accountId());

        repository.save(AccountDimension.builder()
                .applicationId(event.applicationId())
                .accountId(event.accountId())
                .userId(event.userId())
                .lastUsed(event.lastUsed())
                .build());
    }

    @EventListener(AccountUpdatedEvent.class)
    void onAccountUpdated(AccountUpdatedEvent event) {
        log.info("Updating the analytics dimension for account '{}'", event.accountId());

        repository.findByAccountIdForUpdate(event.accountId())
                .ifPresent(d -> d.setLastUsed(event.lastUsed()));
    }

    @EventListener(AccountDeletedEvent.class)
    void onAccountDeleted(AccountDeletedEvent event) {
        log.info("Deactivating the analytics dimension of deleted account '{}'", event.accountId());

        repository.deactivateByAccountId(event.accountId());
    }

    @EventListener(ApplicationDeletedEvent.class)
    void onApplicationDeleted(ApplicationDeletedEvent event) {
        log.info("Deactivating all analytics dimensions of deleted application '{}'", event.applicationId());

        repository.deactivateByApplicationId(event.applicationId());
    }

    @EventListener(UserDeletedEvent.class)
    void onUserDeleted(UserDeletedEvent event) {
        log.info("Deactivating all analytics dimensions of deleted user '{}'", event.userId());

        repository.deactivateByUserId(event.userId());
    }

}
