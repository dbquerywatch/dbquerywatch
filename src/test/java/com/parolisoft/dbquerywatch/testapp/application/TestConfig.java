package com.parolisoft.dbquerywatch.testapp.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({
    "com.parolisoft.dbquerywatch.testapp.adapters",
    "com.parolisoft.dbquerywatch.testapp.application.service",
})
@EnableJpaRepositories(
        basePackages = "com.parolisoft.dbquerywatch.testapp.adapters.db"
)
@EntityScan(basePackages = "com.parolisoft.dbquerywatch.testapp.adapters.db")
class TestConfig {
}
