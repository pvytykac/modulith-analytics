package net.pvytykac.modularapp.application.internal;

import jakarta.validation.constraints.NotBlank;
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

@RestController
@RequestMapping(value = "/v1/applications", produces = "application/json")
@RequiredArgsConstructor
class ApplicationController {

    private final ApplicationService service;

    @PostMapping(consumes = "application/json")
    Application create(@RequestBody @Validated PostRequest payload) {
        return service.create(payload.name());
    }

    @GetMapping
    Page<Application> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{applicationId}")
    Application get(@PathVariable String applicationId) {
        return service.require(applicationId);
    }

    @PutMapping("/{applicationId}")
    Application update(@PathVariable String applicationId, @RequestBody @Validated PutRequest payload) {
        return service.update(applicationId, payload.name());
    }

    @DeleteMapping("/{applicationId}")
    Application delete(@PathVariable String applicationId) {
        return service.delete(applicationId);
    }

    record PostRequest(@NotBlank String name) {
    }

    record PutRequest(@NotBlank String name) {
    }

}
