package org.dbquerywatch.testapp.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
    "org.dbquerywatch.testapp.adapters",
    "org.dbquerywatch.testapp.application.service",
})
class TestConfig {
}
