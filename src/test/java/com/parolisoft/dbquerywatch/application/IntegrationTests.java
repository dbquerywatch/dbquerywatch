package com.parolisoft.dbquerywatch.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.parolisoft.dbquerywatch.junit5.CatchSlowQueries;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql({"/data.sql"})
@CatchSlowQueries
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
    "dbquerywatch.small-tables=journals"
})
abstract class IntegrationTests {

    @Autowired
    MockMvc mvc;

    @AfterAll
    void silentLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        // avoid spurious log messages after all tests are ran
        logger.setLevel(Level.WARN);
    }

    @Test
    void should_find_article_by_author_last_name() throws Exception {
        mvc.perform(post("/articles/query")
                .content(new JSONObject(Map.of("author_last_name", "Parnas")).toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].author_full_name").value("David L. Parnas"));
    }

    @Test
    void should_find_article_by_year_range() throws Exception {
        mvc.perform(post("/articles/query")
                .content(new JSONObject(Map.of("from_year", 1970, "to_year", 1980)).toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[*].author_last_name").value(containsInAnyOrder(
                "Parnas",
                "Diffie-Hellman",
                "Lamport"
            )));
    }

    @Test
    void should_find_journal_by_publisher() throws Exception {
        mvc.perform(get("/journals/{publisher}", "ACM")
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Communications of the ACM"));
    }
}
