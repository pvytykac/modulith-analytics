package net.pvytykac.modularapp.analytics.internal.dimension.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountDimensionService {

    private final AccountDimensionRepository repository;

    public String getDimensionKey(String accountId) {
        log.debug("Looking up account dimension key for account id '{}'", accountId);

        return repository.findByAccountId(accountId)
                .map(AccountDimension::getId)
                .orElseThrow();
    }

}
