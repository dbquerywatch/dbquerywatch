package org.dbquerywatch.application.domain.model;

import lombok.Value;

import javax.sql.DataSource;

@Value
public class NamedDataSource {
    String name;
    String productName;
    DataSource dataSource;
}
