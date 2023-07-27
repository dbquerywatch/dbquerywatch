package org.dbquerywatch.internal.jdbc;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JdbcClient {

    NamedDataSource getNamedDataSource();

    Optional<String> queryForString(String querySql, List<ParameterSetOperation> operations);

    List<Map<String, Object>> queryForList(String sql, @Nullable Object... args);
}
