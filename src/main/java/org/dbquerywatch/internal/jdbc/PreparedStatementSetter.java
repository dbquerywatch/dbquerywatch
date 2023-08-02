package org.dbquerywatch.internal.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {
	void setValues(PreparedStatement ps) throws SQLException;
}
