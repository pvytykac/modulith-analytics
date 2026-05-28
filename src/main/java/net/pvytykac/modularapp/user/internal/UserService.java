package net.pvytykac.modularapp.user.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.user.events.UserCreatedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import net.pvytykac.modularapp.user.events.UserUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final ApplicationEventPublisher publisher;

    Page<User> list(Pageable pageable) {
        log.debug("Listing users page '{}' of size '{}'", pageable.getPageNumber(), pageable.getPageSize());

        return repository.findAll(pageable)
                .map(mapper::entityToRepresentation);
    }

    User require(String id) {
        log.debug("Looking up required user with userId '{}'", id);

        return repository.findById(id)
                .map(mapper::entityToRepresentation)
                .orElseThrow();
    }

    User create(String displayName, String email) {
        log.info("Creating user with displayName '{}' and email '{}'", displayName, email);

        var entity = repository.save(UserEntity.builder()
                .displayName(displayName)
                .email(email)
                .build());

        publisher.publishEvent(new UserCreatedEvent(entity.getId(), entity.getDisplayName(),
                entity.getEmail()));

        return mapper.entityToRepresentation(entity);
    }

    User update(String id, String displayName, String email) {
        log.info("Updating name and email of user '{}' to '{}' and '{}'", id, displayName, email);

        var entity = repository.findByIdForUpdate(id).orElseThrow();

        entity.setDisplayName(displayName);
        entity.setEmail(email);

        publisher.publishEvent(new UserUpdatedEvent(id, displayName, email));

        return mapper.entityToRepresentation(repository.save(entity));
    }

    User delete(String id) {
        log.info("Deleting user with userId '{}'", id);

        var entity = repository.findByIdForUpdate(id);

        entity.ifPresent(e -> {
            repository.delete(e);
            publisher.publishEvent(new UserDeletedEvent(id, e.getDisplayName(), e.getEmail()));
        });

        return entity.map(mapper::entityToRepresentation)
                .orElseThrow();
    }
}
