package com.parolisoft.dbquerywatch.testapp.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
    "com.parolisoft.dbquerywatch.testapp.adapters",
    "com.parolisoft.dbquerywatch.testapp.application.service",
})
class TestConfig {
}
