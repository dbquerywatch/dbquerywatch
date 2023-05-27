package com.parolisoft.dbquerywatch.internal.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    Optional<T> extractData(ResultSet rs) throws SQLException;
}
