package net.pvytykac.modularapp.application.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pvytykac.modularapp.application.events.ApplicationCreatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.application.events.ApplicationUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;
    private final ApplicationEventPublisher publisher;

    Page<Application> list(Pageable pageable) {
        log.debug("Listing applications page '{}' of size '{}'", pageable.getPageNumber(), pageable.getPageSize());

        return repository.findAll(pageable)
                .map(mapper::entityToRepresentation);
    }

    Application require(String id) {
        log.debug("Looking up required application with userId '{}'", id);

        return repository.findById(id)
                .map(mapper::entityToRepresentation)
                .orElseThrow();
    }

    Application create(String name) {
        log.info("Creating application with name '{}'", name);

        var entity = repository.save(ApplicationEntity.builder()
                .name(name)
                .build());

        publisher.publishEvent(new ApplicationCreatedEvent(entity.getId(), entity.getName(),
                entity.getCreatedAt()));

        return mapper.entityToRepresentation(entity);
    }

    Application update(String id, String name) {
        log.info("Updating name of application '{}' to '{}'", id, name);

        var entity = repository.findByIdForUpdate(id).orElseThrow();

        entity.setName(name);
        publisher.publishEvent(new ApplicationUpdatedEvent(id, name));

        return mapper.entityToRepresentation(repository.save(entity));
    }

    Application delete(String id) {
        log.info("Deleting application with userId '{}'", id);

        var entity = repository.findByIdForUpdate(id);

        entity.ifPresent(e -> {
            repository.delete(e);
            publisher.publishEvent(new ApplicationDeletedEvent(id, e.getName(), Instant.now()));
        });

        return entity.map(mapper::entityToRepresentation)
                .orElseThrow();
    }
}
