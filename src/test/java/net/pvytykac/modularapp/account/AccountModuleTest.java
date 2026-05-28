package net.pvytykac.modularapp.account;

import net.pvytykac.modularapp.PostgresTestContainerConfiguration;
import net.pvytykac.modularapp.application.events.ApplicationCreatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.user.events.UserCreatedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.modulith.test.ApplicationModuleTest(
        mode = org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@Import(PostgresTestContainerConfiguration.class)
class AccountModuleTest {

    private static final Instant INITIAL_LAST_USED = Instant.parse("2024-01-10T10:15:30Z");
    private static final Instant UPDATED_LAST_USED = Instant.parse("2024-02-10T10:15:30Z");

    @Autowired
    private WebTestClient client;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Test
    void createAndGetAccount() throws Exception {
        var applicationId = "app-account-create";
        var userId = "user-account-create";
        setupApplicationAndUser(applicationId, "Zoom", userId, "Alice", "alice+account-create@example.com");

        var accountId = createAccount(applicationId, userId, "ext-acc-1", INITIAL_LAST_USED,
                new JSONArray().put("LIC-A").put("LIC-B"));

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(accountId)
                .jsonPath("$.externalId").isEqualTo("ext-acc-1")
                .jsonPath("$.lastUsed").isEqualTo(INITIAL_LAST_USED.toString())
                .jsonPath("$.licenseIds[?(@ == 'LIC-A')]").exists()
                .jsonPath("$.licenseIds[?(@ == 'LIC-B')]").exists();
    }

    @Test
    void updateAccount() throws Exception {
        var applicationId = "app-account-update";
        var userId = "user-account-update";
        setupApplicationAndUser(applicationId, "Confluence", userId, "Bob", "bob+account-update@example.com");

        var accountId = createAccount(applicationId, userId, "ext-acc-2", INITIAL_LAST_USED,
                new JSONArray().put("LIC-OLD"));

        var payload = new JSONObject()
                .put("lastUsed", UPDATED_LAST_USED)
                .put("licenseIds", new JSONArray().put("LIC-NEW-1").put("LIC-NEW-2"))
                .toString();

        client.put()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(accountId)
                .jsonPath("$.externalId").isEqualTo("ext-acc-2")
                .jsonPath("$.lastUsed").isEqualTo(UPDATED_LAST_USED.toString())
                .jsonPath("$.licenseIds[?(@ == 'LIC-NEW-1')]").exists()
                .jsonPath("$.licenseIds[?(@ == 'LIC-NEW-2')]").exists();

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lastUsed").isEqualTo(UPDATED_LAST_USED.toString())
                .jsonPath("$.licenseIds[?(@ == 'LIC-NEW-1')]").exists()
                .jsonPath("$.licenseIds[?(@ == 'LIC-NEW-2')]").exists();
    }

    @Test
    void deleteAccount() throws Exception {
        var applicationId = "app-account-delete";
        var userId = "user-account-delete";
        setupApplicationAndUser(applicationId, "Mural", userId, "Charlie", "charlie+account-delete@example.com");

        var accountId = createAccount(applicationId, userId, "ext-acc-3", INITIAL_LAST_USED,
                new JSONArray().put("LIC-D"));

        client.delete()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(accountId)
                .jsonPath("$.externalId").isEqualTo("ext-acc-3");

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listAccountsIncludesCreatedAccount() throws Exception {
        var applicationId = "app-account-list";
        var userId = "user-account-list";
        setupApplicationAndUser(applicationId, "Jira", userId, "Diana", "diana+account-list@example.com");

        var accountId = createAccount(applicationId, userId, "ext-acc-4", INITIAL_LAST_USED,
                new JSONArray().put("LIC-LIST"));

        client.get()
                .uri("/v1/applications/{applicationId}/accounts", applicationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").exists()
                .jsonPath("$.content[?(@.id == '" + accountId + "')]").exists()
                .jsonPath("$.content[?(@.externalId == 'ext-acc-4')]").exists();
    }

    @Test
    void applicationDeletedEventRemovesAccounts() throws Exception {
        var applicationId = "app-account-event-delete-app";
        var userId = "user-account-event-delete-app";
        setupApplicationAndUser(applicationId, "Notion", userId, "Erin", "erin+account-event-app@example.com");

        var accountId = createAccount(applicationId, userId, "ext-acc-event-app", INITIAL_LAST_USED,
                new JSONArray().put("LIC-EVENT-APP"));

        publisher.publishEvent(new ApplicationDeletedEvent(applicationId, "Notion", Instant.now()));

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationId, accountId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void userDeletedEventRemovesAccounts() throws Exception {
        var userId = "user-account-event-delete-user";
        var applicationOneId = "app-account-event-delete-user-1";
        var applicationTwoId = "app-account-event-delete-user-2";
        var displayName = "Frank";
        var email = "frank+account-event-user@example.com";

        setupApplication(applicationOneId, "Trello");
        setupApplication(applicationTwoId, "Asana");
        setupUser(userId, displayName, email);

        var accountOneId = createAccount(applicationOneId, userId, "ext-acc-event-user-1", INITIAL_LAST_USED,
                new JSONArray().put("LIC-EVENT-USER"));
        var accountTwoId = createAccount(applicationTwoId, userId, "ext-acc-event-user-2", INITIAL_LAST_USED,
                new JSONArray().put("LIC-EVENT-USER"));

        publisher.publishEvent(new UserDeletedEvent(userId, displayName, email));

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationOneId, accountOneId)
                .exchange()
                .expectStatus().isNotFound();

        client.get()
                .uri("/v1/applications/{applicationId}/accounts/{accountId}", applicationTwoId, accountTwoId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private String createAccount(String applicationId, String userId, String externalId, Instant lastUsed,
                                 JSONArray licenseIds) throws Exception {
        var payload = new JSONObject()
                .put("userId", userId)
                .put("externalId", externalId)
                .put("lastUsed", lastUsed)
                .put("licenseIds", licenseIds)
                .toString();

        var result = client.post()
                .uri("/v1/applications/{applicationId}/accounts", applicationId)
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

    private void setupApplicationAndUser(String applicationId, String applicationName, String userId,
                                         String displayName, String email) {
        setupApplication(applicationId, applicationName);
        setupUser(userId, displayName, email);
    }

    private void setupApplication(String applicationId, String applicationName) {
        var createdAt = Instant.now();
        jdbcTemplate.update("INSERT INTO applications(id, name, created_at) VALUES (?, ?, ?)",
                applicationId, applicationName, new Timestamp(createdAt.toEpochMilli()));
        publisher.publishEvent(new ApplicationCreatedEvent(applicationId, applicationName, createdAt));
    }

    private void setupUser(String userId, String displayName, String email) {
        publisher.publishEvent(new UserCreatedEvent(userId, displayName, email));
    }

}
