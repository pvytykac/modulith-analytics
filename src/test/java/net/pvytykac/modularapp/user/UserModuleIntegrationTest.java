package net.pvytykac.modularapp.user;

import net.pvytykac.modularapp.BaseIntegrationTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES;

@ApplicationModuleTest(
        mode = ALL_DEPENDENCIES,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UserModuleIntegrationTest extends BaseIntegrationTest {

    @Test
    void createAndGetUser() throws Exception {
        var id = createUser("Alice", "alice+create@example.com");

        getClient()
                .get()
                .uri("/v1/users/{userId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.displayName").isEqualTo("Alice")
                .jsonPath("$.email").isEqualTo("alice+create@example.com");
    }

    @Test
    void updateUser() throws Exception {
        var id = createUser("Bob", "bob+before@example.com");
        var updatedDisplayName = "Robert";
        var updatedEmail = "bob+after@example.com";
        var payload = new JSONObject()
                .put("displayName", updatedDisplayName)
                .put("email", updatedEmail)
                .toString();

        getClient()
                .put()
                .uri("/v1/users/{userId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.displayName").isEqualTo(updatedDisplayName)
                .jsonPath("$.email").isEqualTo(updatedEmail);

        getClient()
                .get()
                .uri("/v1/users/{userId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.displayName").isEqualTo(updatedDisplayName)
                .jsonPath("$.email").isEqualTo(updatedEmail);
    }

    @Test
    void deleteUser() throws Exception {
        var id = createUser("Charlie", "charlie+delete@example.com");

        getClient()
                .delete()
                .uri("/v1/users/{userId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.displayName").isEqualTo("Charlie")
                .jsonPath("$.email").isEqualTo("charlie+delete@example.com");

        getClient()
                .get()
                .uri("/v1/users/{userId}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listUsersIncludesCreatedUser() throws Exception {
        var id = createUser("Diana", "diana+list@example.com");

        getClient()
                .get()
                .uri("/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").exists()
                .jsonPath("$.content[?(@.id == '" + id + "')]").exists()
                .jsonPath("$.content[?(@.displayName == 'Diana')]").exists()
                .jsonPath("$.content[?(@.email == 'diana+list@example.com')]").exists();
    }

    private String createUser(String displayName, String email) throws Exception {
        var payload = new JSONObject()
                .put("displayName", displayName)
                .put("email", email)
                .toString();

        var result = getClient()
                .post()
                .uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();

        assertThat(result).isNotNull();
        return new JSONObject(new String(result, StandardCharsets.UTF_8)).getString("id");
    }

}
