package com.parolisoft.dbquerywatch.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({
    "com.parolisoft.dbquerywatch.adapters",
    "com.parolisoft.dbquerywatch.application.service",
})
@EnableJpaRepositories(
        basePackages = "com.parolisoft.dbquerywatch.adapters.db"
)
@EntityScan(basePackages = "com.parolisoft.dbquerywatch.adapters.db")
class TestConfig {
}
