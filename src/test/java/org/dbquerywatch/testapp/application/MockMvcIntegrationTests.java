package org.dbquerywatch.testapp.application;

import org.dbquerywatch.application.domain.service.ClassIdRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Disabled("Expected to fail. Go to junit-platform.properties to re-enable all disabled tests at once.")
@SuppressWarnings({"java:S5786", "WeakerAccess"})  // public required by ByteBuddy
public class MockMvcIntegrationTests extends BaseIntegrationTests {

    @Autowired
    MockMvc mvc;

    @AfterAll
    void verifyMetrics() {
        assertThat(ClassIdRepository.getThreadLocalHits()).isGreaterThan(0);
        assertThat(ClassIdRepository.getMdcHits()).isEqualTo(0);
    }

    @Test
    void should_find_article_by_author_last_name() throws Exception {
        mvc.perform(post("/articles/query")
                .content(new JSONObject().put("author_last_name", "Parnas").toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.length()").value(1),
                jsonPath("$[0].author_full_name").value("David L. Parnas")
            );
    }

    @Test
    @Tag("slow-query")
    void should_find_article_by_year_range() throws Exception {
        mvc.perform(post("/articles/query")
                .content(new JSONObject().put("from_year", 1970).put("to_year", 1980).toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.length()").value(3),
                jsonPath("$[*].author_last_name").value(containsInAnyOrder(
                    "Parnas",
                    "Diffie-Hellman",
                    "Lamport"
                ))
            );
    }

    @Test
    @Tag("slow-query")
    void should_find_journal_by_publisher() throws Exception {
        mvc.perform(get("/journals/{publisher}", "ACM")
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.length()").value(1),
                jsonPath("$[0].name").value("Communications of the ACM")
            );
    }
}
