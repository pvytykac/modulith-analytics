package net.pvytykac.modularapp.analytics.internal.dimension.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.user.events.UserCreatedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import net.pvytykac.modularapp.user.events.UserUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
@Slf4j
class UserDimensionEventHandlers {

    private final UserDimensionRepository repository;

    @EventListener(UserCreatedEvent.class)
    void onUserCreated(UserCreatedEvent event) {
        log.info("Creating an analytics dimension for user '{}'", event.userId());

        repository.save(UserDimension.builder()
                .userId(event.userId())
                .email(event.email())
                .displayName(event.displayName())
                .build());
    }

    @EventListener(UserUpdatedEvent.class)
    void onUserUpdated(UserUpdatedEvent event) {
        log.info("Updating the analytics dimension of user '{}'", event.userId());

        repository.findByUserIdForUpdate(event.userId())
                .ifPresent(d -> {
                    d.setEmail(event.email());
                    d.setDisplayName(event.displayName());
                });
    }

    @EventListener(UserDeletedEvent.class)
    void onUserDeleted(UserDeletedEvent event) {
        log.info("Deactivating the analytics dimension of user '{}'", event.userId());

        repository.deactivateByUserId(event.userId());
    }

}
