package com.parolisoft.dbquerywatch.internal.spring;

import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.Named;

import javax.sql.DataSource;

@Value
@Getter(onMethod_ = {@Override})
class NamedDataSource implements Named<DataSource> {
    String name;
    DataSource payload;
}
