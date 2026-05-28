package net.pvytykac.modularapp.license;

import net.pvytykac.modularapp.PostgresTestContainerConfiguration;
import net.pvytykac.modularapp.application.events.ApplicationCreatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(
        mode = ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@Import(PostgresTestContainerConfiguration.class)
class LicenseModuleTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Test
    void createAndGetLicense() throws Exception {
        var applicationId = "app-license-create";
        setupApplication(applicationId, "Slack");

        var licenseId = createLicense(applicationId, "Basic", BigDecimal.valueOf(12.50));

        client.get()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(licenseId)
                .jsonPath("$.name").isEqualTo("Basic")
                .jsonPath("$.monthlyPrice").isEqualTo(12.50);
    }

    @Test
    void updateLicense() throws Exception {
        var applicationId = "app-license-update";
        setupApplication(applicationId, "Confluence");

        var licenseId = createLicense(applicationId, "Starter", BigDecimal.valueOf(9.99));
        var payload = new JSONObject()
                .put("name", "Pro")
                .put("monthlyPrice", BigDecimal.valueOf(19.99))
                .toString();

        client.put()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(licenseId)
                .jsonPath("$.name").isEqualTo("Pro")
                .jsonPath("$.monthlyPrice").isEqualTo(19.99);

        client.get()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Pro")
                .jsonPath("$.monthlyPrice").isEqualTo(19.99);
    }

    @Test
    void deleteLicense() throws Exception {
        var applicationId = "app-license-delete";
        setupApplication(applicationId, "Notion");

        var licenseId = createLicense(applicationId, "Enterprise", BigDecimal.valueOf(49.99));

        client.delete()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(licenseId)
                .jsonPath("$.name").isEqualTo("Enterprise")
                .jsonPath("$.monthlyPrice").isEqualTo(49.99);

        client.get()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listLicensesIncludesCreatedLicense() throws Exception {
        var applicationId = "app-license-list";
        setupApplication(applicationId, "Jira");

        var licenseId = createLicense(applicationId, "Team", BigDecimal.valueOf(29.99));

        client.get()
                .uri("/v1/applications/{applicationId}/licenses", applicationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").exists()
                .jsonPath("$.content[?(@.id == '" + licenseId + "')]").exists()
                .jsonPath("$.content[?(@.name == 'Team')]").exists();
    }

    @Test
    void applicationDeletedEventRemovesLicenses() throws Exception {
        var applicationId = "app-license-event-delete-app";
        setupApplication(applicationId, "Miro");

        var licenseId = createLicense(applicationId, "Power", BigDecimal.valueOf(39.99));

        publisher.publishEvent(new ApplicationDeletedEvent(applicationId, "Miro", Instant.now()));

        client.get()
                .uri("/v1/applications/{applicationId}/licenses/{licenseId}", applicationId, licenseId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private String createLicense(String applicationId, String name, BigDecimal monthlyPrice) throws Exception {
        var payload = new JSONObject()
                .put("name", name)
                .put("monthlyPrice", monthlyPrice)
                .toString();

        var result = client.post()
                .uri("/v1/applications/{applicationId}/licenses", applicationId)
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

    private void setupApplication(String applicationId, String applicationName) {
        var createdAt = Instant.now();
        jdbcTemplate.update("INSERT INTO applications(id, name, created_at) VALUES (?, ?, ?)",
                applicationId, applicationName, new Timestamp(createdAt.toEpochMilli()));
        publisher.publishEvent(new ApplicationCreatedEvent(applicationId, applicationName, createdAt));
    }

}
