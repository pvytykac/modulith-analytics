package net.pvytykac.modularapp.account.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/v1/applications/{applicationId}/accounts")
@RequiredArgsConstructor
class AccountController {

    private final AccountService service;

    @PostMapping(consumes = "application/json")
    Account create(@PathVariable String applicationId, @RequestBody @Validated PostRequest payload) {
        return service.create(applicationId, payload.userId(), payload.externalId(), payload.lastUsed(),
                payload.licenseIds());
    }

    @GetMapping
    Page<Account> list(@PathVariable String applicationId, Pageable pageable) {
        return service.list(applicationId, pageable);
    }

    @GetMapping("/{accountId}")
    Account get(@PathVariable String applicationId, @PathVariable String accountId) {
        return service.require(applicationId, accountId);
    }

    @PutMapping("/{accountId}")
    Account update(@PathVariable String applicationId, @PathVariable String accountId, @RequestBody @Validated PutRequest payload) {
        return service.update(applicationId, accountId, payload.lastUsed(), payload.licenseIds());
    }

    @DeleteMapping("/{accountId}")
    Account delete(@PathVariable String applicationId, @PathVariable String accountId) {
        return service.delete(applicationId, accountId);
    }

    record PostRequest(@NotBlank String userId, @NotBlank String externalId, @PastOrPresent Instant lastUsed,
                       @NotNull Set<String> licenseIds) {
    }

    record PutRequest(@PastOrPresent Instant lastUsed, @NotNull Set<String> licenseIds) {
    }

}
