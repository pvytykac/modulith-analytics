package net.pvytykac.modularapp.application;

import net.pvytykac.modularapp.BaseIntegrationTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(
        mode = ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApplicationModuleIntegrationTest extends BaseIntegrationTest {

    @Test
    void createAndGetApplication() throws Exception {
        var id = createApplication("Slack");

        getClient()
                .get()
                .uri("/v1/applications/{applicationId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo("Slack")
                .jsonPath("$.createdAt").exists();
    }

    @Test
    void updateApplication() throws Exception {
        var id = createApplication("Office 365");
        var updatedName = "MS Office 365";
        var payload = new JSONObject()
                .put("name", updatedName)
                .toString();

        getClient()
                .put()
                .uri("/v1/applications/{applicationId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(updatedName);

        getClient()
                .get()
                .uri("/v1/applications/{applicationId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo(updatedName);
    }

    @Test
    void deleteApplication() throws Exception {
        var id = createApplication("Miro");

        getClient()
                .delete()
                .uri("/v1/applications/{applicationId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo("Miro");

        getClient()
                .get()
                .uri("/v1/applications/{applicationId}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listApplicationsIncludesCreatedApplication() throws Exception {
        var id = createApplication("e-Plan");

        getClient()
                .get()
                .uri("/v1/applications")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").exists()
                .jsonPath("$.content[?(@.id == '" + id + "')]").exists()
                .jsonPath("$.content[?(@.name == 'e-Plan')]").exists();
    }

    private String createApplication(String name) throws Exception {
        var payload = new JSONObject()
                .put("name", name)
                .toString();

        var result = getClient()
                .post()
                .uri("/v1/applications")
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
