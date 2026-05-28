package net.pvytykac.modularapp.account.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
class AccountEventHandler {

    private final AccountRepository repository;

    @EventListener(ApplicationDeletedEvent.class)
    void onApplicationDeletedEvent(ApplicationDeletedEvent event) {
        log.info("Deleting all application accounts of the deleted application '{}'", event.applicationId());

        repository.deleteAllByApplicationId(event.applicationId());
    }

    @EventListener(UserDeletedEvent.class)
    void onUserDeleted(UserDeletedEvent event) {
        log.info("Deleting all application accounts of the deleted user '{}'", event.userId());

        repository.deleteAllByUserId(event.userId());
    }
}
