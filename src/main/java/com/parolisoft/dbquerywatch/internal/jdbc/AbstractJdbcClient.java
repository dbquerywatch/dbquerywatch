package com.parolisoft.dbquerywatch.internal.jdbc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.junit.jupiter.api.Named;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter(onMethod_ = {@Override})
public abstract class AbstractJdbcClient implements JdbcClient {

    private final Named<DataSource> namedDataSource;

    @Override
    public Optional<String> queryForString(String querySql, List<ParameterSetOperation> operations) {
        return query(
            querySql,
            ps -> setParameters(ps, operations),
            rs -> rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty());
    }

    protected abstract <T> Optional<T> query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse);

    @SneakyThrows
    private static void setParameters(PreparedStatement ps, List<ParameterSetOperation> operations) {
        for (ParameterSetOperation operation : operations) {
            operation.getMethod().invoke(ps, operation.getArgs());
        }
    }
}
