package org.dbquerywatch.application.domain.model;

import org.immutables.value.Value;

import javax.sql.DataSource;

@Value.Immutable
@Value.Style(allParameters = true)
public interface NamedDataSource {
    String getName();
    String getProductName();
    DataSource getDataSource();
}
