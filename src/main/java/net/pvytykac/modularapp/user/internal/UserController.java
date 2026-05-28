package net.pvytykac.modularapp.user.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/v1/users")
@RequiredArgsConstructor
class UserController {

    private final UserService service;

    @PostMapping(consumes = "application/json")
    User create(@RequestBody @Validated PostRequest payload) {
        return service.create(payload.displayName(), payload.email());
    }

    @GetMapping
    Page<User> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{userId}")
    User get(@PathVariable String userId) {
        return service.require(userId);
    }

    @PutMapping("/{userId}")
    User update(@PathVariable String userId, @RequestBody @Validated PutRequest payload) {
        return service.update(userId, payload.displayName(), payload.email());
    }

    @DeleteMapping("/{userId}")
    User delete(@PathVariable String userId) {
        return service.delete(userId);
    }

    record PostRequest(@NotBlank String displayName, @NotNull @Email String email) {
    }

    record PutRequest(@NotBlank String displayName, @NotNull @Email String email) {
    }


}
