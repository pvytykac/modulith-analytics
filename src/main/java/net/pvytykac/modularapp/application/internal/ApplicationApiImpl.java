package net.pvytykac.modularapp.application.internal;

import lombok.RequiredArgsConstructor;
import net.pvytykac.modularapp.application.api.ApplicationApi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
@Transactional
@RequiredArgsConstructor
class ApplicationApiImpl implements ApplicationApi {

    private final ApplicationRepository repository;

    @Override
    public <T> T mapIfApplicationExists(String applicationId, Supplier<T> supplier) {
        return repository.findByIdForUpdate(applicationId).map(_ -> supplier.get()).orElseThrow();
    }

}
