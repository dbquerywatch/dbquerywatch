package org.dbquerywatch.internal.jdbc;

import lombok.Value;

import javax.sql.DataSource;

@Value
public class NamedDataSource {
    String name;
    String productName;
    DataSource dataSource;
}
