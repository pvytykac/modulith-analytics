package net.pvytykac.modularapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@Import(PostgresTestContainerConfiguration.class)
public abstract class BaseIntegrationTest {

    @Autowired
    private WebTestClient client;

    protected WebTestClient getClient() {
        return client;
    }
}
