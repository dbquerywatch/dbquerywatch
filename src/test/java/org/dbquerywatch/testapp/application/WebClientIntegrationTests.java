package org.dbquerywatch.testapp.application;

import org.dbquerywatch.application.domain.service.TestMethodIdRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Disabled("Expected to fail. Go to junit-platform.properties to re-enable all disabled tests at once.")
@SuppressWarnings({"java:S5786", "WeakerAccess"})  // public required by ByteBuddy
@Execution(CONCURRENT)
public class WebClientIntegrationTests extends BaseIntegrationTests {

    @AfterAll
    void verifyMetrics() {
        assertThat(TestMethodIdRepository.getMdcHits()).isGreaterThan(0);
        assertThat(TestMethodIdRepository.getThreadLocalHits()).isEqualTo(0);
    }

    @Test
    void should_find_article_by_author_last_name(WebTestClient client) {
        client.post()
            .uri("/articles/query")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new JSONObject().put("author_last_name", "Parnas").toString())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].author_full_name").isEqualTo("David L. Parnas");
    }

    @Test
    @Tag("slow-query")
    void should_find_article_by_year_range(WebTestClient client) {
        client.post()
            .uri("/articles/query")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new JSONObject().put("from_year", 1970).put("to_year", 1980).toString())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody().json("[" +
                "{'author_last_name': 'Parnas'}, " +
                "{'author_last_name': 'Diffie-Hellman'}, " +
                "{'author_last_name': 'Lamport'}" +
                "]");
    }

    @Test
    @Tag("slow-query")
    void should_find_journal_by_publisher(WebTestClient client) {
        client.get()
            .uri("/journals/{publisher}", "ACM")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].name").isEqualTo("Communications of the ACM");
    }
}
