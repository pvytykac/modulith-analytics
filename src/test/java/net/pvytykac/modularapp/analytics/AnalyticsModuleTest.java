package net.pvytykac.modularapp.analytics;

import net.pvytykac.modularapp.PostgresTestContainerConfiguration;
import net.pvytykac.modularapp.account.events.AccountCreatedEvent;
import net.pvytykac.modularapp.account.events.AccountDeletedEvent;
import net.pvytykac.modularapp.account.events.AccountUpdatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationCreatedEvent;
import net.pvytykac.modularapp.application.events.ApplicationDeletedEvent;
import net.pvytykac.modularapp.application.events.ApplicationUpdatedEvent;
import net.pvytykac.modularapp.license.api.LicensePriceInfo;
import net.pvytykac.modularapp.license.events.LicenseCreatedEvent;
import net.pvytykac.modularapp.license.events.LicenseDeletedEvent;
import net.pvytykac.modularapp.license.events.LicenseUpdatedEvent;
import net.pvytykac.modularapp.user.events.UserCreatedEvent;
import net.pvytykac.modularapp.user.events.UserDeletedEvent;
import net.pvytykac.modularapp.user.events.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationModuleTest(
        mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@Import(PostgresTestContainerConfiguration.class)
class AnalyticsModuleTest {

    private static final Instant BASE = Instant.parse("2024-03-01T00:00:00Z");

    @Autowired
    private WebTestClient client;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void dailyBreakdownWithoutFilters() {
        var fixture = publishFixture();

        query(fixture, "")
                .jsonPath("$.content.length()").isEqualTo(4)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[0].revokedCount").isEqualTo(0)
                .jsonPath("$.content[1].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(0)
                .jsonPath("$.content[2].assignedCount").isEqualTo(1)
                .jsonPath("$.content[2].revokedCount").isEqualTo(1)
                .jsonPath("$.content[3].assignedCount").isEqualTo(0)
                .jsonPath("$.content[3].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByAccountId() {
        var fixture = publishFixture();

        query(fixture, "&accountId=" + fixture.accountA())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByUserId() {
        var fixture = publishFixture();

        query(fixture, "&userId=" + fixture.userB())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByLicenseId() {
        var fixture = publishFixture();

        query(fixture, "&licenseId=" + fixture.licenseA())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[0].revokedCount").isEqualTo(0)
                .jsonPath("$.content[1].assignedCount").isEqualTo(0)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByEmail() {
        var fixture = publishFixture();

        query(fixture, "&email=" + fixture.userAEmail())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByMinApplicationCreatedAt() {
        var fixture = publishFixture();

        query(fixture, "&minApplicationCreatedAt=" + fixture.applicationCreatedAt().plusSeconds(1))
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void dailyBreakdownFilteredByMaxApplicationCreatedAt() {
        var fixture = publishFixture();

        query(fixture, "&maxApplicationCreatedAt=" + fixture.applicationCreatedAt())
                .jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void dailyBreakdownFilteredByMinLicenseCost() {
        var fixture = publishFixture();

        query(fixture, "&minLicenseCost=20")
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].assignedCount").isEqualTo(1)
                .jsonPath("$.content[2].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByMaxLicenseCost() {
        var fixture = publishFixture();

        query(fixture, "&maxLicenseCost=20")
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByMinLastUsed() {
        var fixture = publishFixture();

        query(fixture, "&minLastUsed=" + fixture.lastUsedB())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void dailyBreakdownFilteredByMaxLastUsed() {
        var fixture = publishFixture();

        query(fixture, "&maxLastUsed=" + fixture.lastUsedB())
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].assignedCount").isEqualTo(1)
                .jsonPath("$.content[1].revokedCount").isEqualTo(1);
    }

    @Test
    void accountDeletedEventDeactivatesAccountDimension() {
        var fixture = publishFixture();

        publisher.publishEvent(new AccountDeletedEvent(
                fixture.applicationId(),
                fixture.userA(),
                fixture.accountA(),
                "ext-" + fixture.accountA(),
                fixture.lastUsedA(),
                List.of(new PriceInfo(fixture.licenseA(), BigDecimal.valueOf(10))),
                BASE.plusSeconds(8 * 86_400L)
        ));

        assertDimensionInactive("dim_accounts", "account_id", fixture.accountA());
    }

    @Test
    void userDeletedEventDeactivatesUserAndAccountDimensions() {
        var fixture = publishFixture();

        publisher.publishEvent(new UserDeletedEvent(fixture.userA(), "Anna", fixture.userAEmail()));

        assertDimensionInactive("dim_users", "user_id", fixture.userA());
        assertDimensionInactive("dim_accounts", "account_id", fixture.accountA());
    }

    @Test
    void licenseDeletedEventDeactivatesLicenseDimension() {
        var fixture = publishFixture();

        publisher.publishEvent(new LicenseDeletedEvent(fixture.applicationId(), fixture.licenseA(), "Starter", BigDecimal.valueOf(10)));

        assertDimensionInactive("dim_licenses", "license_id", fixture.licenseA());
    }

    @Test
    void applicationDeletedEventDeactivatesAllRelatedDimensions() {
        var fixture = publishFixture();

        publisher.publishEvent(new ApplicationDeletedEvent(fixture.applicationId(), "App", Instant.now()));

        assertDimensionInactive("dim_applications", "application_id", fixture.applicationId());
        assertDimensionInactive("dim_accounts", "account_id", fixture.accountA());
        assertDimensionInactive("dim_licenses", "license_id", fixture.licenseA());
    }

    @Test
    void applicationUpdatedEventUpdatesApplicationDimension() {
        var fixture = publishFixture();
        var updatedName = "Updated App " + fixture.applicationId();

        publisher.publishEvent(new ApplicationUpdatedEvent(fixture.applicationId(), updatedName));

        assertDimensionValue("dim_applications", "application_id", fixture.applicationId(), "name", updatedName);
    }

    @Test
    void userUpdatedEventUpdatesUserDimension() {
        var fixture = publishFixture();
        var updatedDisplayName = "Updated Anna " + fixture.userA();
        var updatedEmail = "updated-" + fixture.userA() + "@example.com";

        publisher.publishEvent(new UserUpdatedEvent(fixture.userA(), updatedDisplayName, updatedEmail));

        assertDimensionValue("dim_users", "user_id", fixture.userA(), "display_name", updatedDisplayName);
        assertDimensionValue("dim_users", "user_id", fixture.userA(), "email", updatedEmail);
    }

    @Test
    void licenseUpdatedEventUpdatesLicenseDimension() {
        var fixture = publishFixture();
        var updatedName = "Updated Starter " + fixture.licenseA();
        var updatedCost = BigDecimal.valueOf(44.44);

        publisher.publishEvent(new LicenseUpdatedEvent(fixture.applicationId(), fixture.licenseA(), updatedName, updatedCost));

        assertDimensionValue("dim_licenses", "license_id", fixture.licenseA(), "name", updatedName);
        assertDimensionValue("dim_licenses", "license_id", fixture.licenseA(), "cost", updatedCost);
    }

    @Test
    void accountUpdatedEventUpdatesAccountDimension() {
        var fixture = publishFixture();
        var updatedLastUsed = BASE.plusSeconds(12 * 86_400L);

        publisher.publishEvent(new AccountUpdatedEvent(
                fixture.applicationId(),
                fixture.userA(),
                fixture.accountA(),
                "ext-" + fixture.accountA(),
                updatedLastUsed,
                List.of(new PriceInfo(fixture.licenseB(), BigDecimal.valueOf(30))),
                List.of(new PriceInfo(fixture.licenseA(), BigDecimal.valueOf(10))),
                BASE.plusSeconds(13 * 86_400L)
        ));

        assertDimensionValue("dim_accounts", "account_id", fixture.accountA(),
                "to_char(last_used, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"')", updatedLastUsed.plus(1, ChronoUnit.HOURS));
    }

    private WebTestClient.BodyContentSpec query(Fixture fixture, String extraQuery) {
        return client.get()
                .uri("/v1/applications/{applicationId}/analytics/licenseAssignments/dailyBreakdown?startTime={startTime}&endTime={endTime}" + extraQuery,
                        fixture.applicationId(), fixture.startTime(), fixture.endTime())
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    private Fixture publishFixture() {
        var suffix = Long.toString(System.nanoTime());
        var applicationId = "app-analytics-" + suffix;
        var userA = "user-a-" + suffix;
        var userB = "user-b-" + suffix;
        var userAEmail = "anna-" + suffix + "@example.com";
        var userBEmail = "ben-" + suffix + "@example.com";
        var licenseA = "lic-a-" + suffix;
        var licenseB = "lic-b-" + suffix;
        var accountA = "acc-a-" + suffix;
        var accountB = "acc-b-" + suffix;

        var applicationCreatedAt = BASE.minusSeconds(86_400);
        var assignmentDay1 = BASE.plusSeconds(86_400);
        var assignmentDay2 = BASE.plusSeconds(2 * 86_400L);
        var assignmentDay3 = BASE.plusSeconds(3 * 86_400L);
        var assignmentDay4 = BASE.plusSeconds(4 * 86_400L);
        var lastUsedA = BASE.plusSeconds(2 * 86_400L);
        var lastUsedB = BASE.plusSeconds(5 * 86_400L);

        publisher.publishEvent(new ApplicationCreatedEvent(applicationId, "App " + suffix, applicationCreatedAt));
        publisher.publishEvent(new UserCreatedEvent(userA, "Anna " + suffix, userAEmail));
        publisher.publishEvent(new UserCreatedEvent(userB, "Ben " + suffix, userBEmail));
        publisher.publishEvent(new LicenseCreatedEvent(applicationId, licenseA, "Starter " + suffix, BigDecimal.valueOf(10)));
        publisher.publishEvent(new LicenseCreatedEvent(applicationId, licenseB, "Enterprise " + suffix, BigDecimal.valueOf(30)));

        var licAInfo = new PriceInfo(licenseA, BigDecimal.valueOf(10));
        var licBInfo = new PriceInfo(licenseB, BigDecimal.valueOf(30));

        publisher.publishEvent(new AccountCreatedEvent(applicationId, userA, accountA, "ext-" + accountA,
                lastUsedA, List.of(licAInfo), assignmentDay1));
        publisher.publishEvent(new AccountCreatedEvent(applicationId, userB, accountB, "ext-" + accountB,
                lastUsedB, List.of(licBInfo), assignmentDay2));
        publisher.publishEvent(new AccountUpdatedEvent(applicationId, userA, accountA, "ext-" + accountA,
                lastUsedA, List.of(licBInfo), List.of(licAInfo), assignmentDay3));
        publisher.publishEvent(new AccountDeletedEvent(applicationId, userB, accountB, "ext-" + accountB,
                lastUsedB, List.of(licBInfo), assignmentDay4));

        return new Fixture(
                applicationId,
                userA,
                userB,
                userAEmail,
                licenseA,
                licenseB,
                accountA,
                applicationCreatedAt,
                lastUsedA,
                lastUsedB,
                BASE,
                BASE.plusSeconds(7 * 86_400L)
        );
    }

    private void assertDimensionInactive(String table, String idColumn, String id) {
        var active = jdbcTemplate.queryForObject(
                "SELECT active FROM " + table + " WHERE " + idColumn + " = ? ORDER BY id DESC LIMIT 1",
                Boolean.class,
                id
        );

        if (active == null || active) {
            throw new AssertionError("Expected inactive dimension in " + table + " for " + idColumn + "=" + id);
        }
    }

    private void assertDimensionValue(String table, String idColumn, String id, String valueColumn, Object expectedValue) {
        var actual = jdbcTemplate.queryForObject(
                "SELECT " + valueColumn + " FROM " + table + " WHERE " + idColumn + " = ? ORDER BY id DESC LIMIT 1",
                Object.class,
                id
        );

        if (actual == null || !actual.toString().equals(expectedValue.toString())) {
            throw new AssertionError("Expected " + table + "." + valueColumn + "=" + expectedValue + ", actual=" + actual);
        }
    }

    private record Fixture(
            String applicationId,
            String userA,
            String userB,
            String userAEmail,
            String licenseA,
            String licenseB,
            String accountA,
            Instant applicationCreatedAt,
            Instant lastUsedA,
            Instant lastUsedB,
            Instant startTime,
            Instant endTime
    ) {
    }

    private record PriceInfo(String id, BigDecimal monthlyPrice) implements LicensePriceInfo {
    }

}
