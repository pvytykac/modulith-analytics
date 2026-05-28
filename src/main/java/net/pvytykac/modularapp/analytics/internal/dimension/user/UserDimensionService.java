package net.pvytykac.modularapp.analytics.internal.dimension.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserDimensionService {

    private final UserDimensionRepository repository;

    public String getDimensionKey(String userId) {
        log.debug("Looking up user dimension key for user id '{}'", userId);

        return repository.findByUserId(userId)
                .map(UserDimension::getId)
                .orElseThrow();
    }

}
