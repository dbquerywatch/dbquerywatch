package org.dbquerywatch.application.port.out;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.NamedDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractJdbcClient implements JdbcClient {

    private final NamedDataSource namedDataSource;

    public AbstractJdbcClient(NamedDataSource namedDataSource) {
        this.namedDataSource = namedDataSource;
    }

    @Override
    public Optional<String> queryForString(String querySql, List<ParameterSetOperation> operations) {
        return query(
            querySql,
            ps -> setParameters(ps, operations),
            rs -> rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty());
    }

    protected abstract <T> Optional<T> query(String sql, Consumer<PreparedStatement> pss, ResultSetExtractor<T> rse);

    @Override
    public NamedDataSource getNamedDataSource() {
        return this.namedDataSource;
    }

    @FunctionalInterface
    protected interface ResultSetExtractor<T> {
        Optional<T> extractData(ResultSet rs) throws SQLException;
    }

    private static void setParameters(PreparedStatement ps, List<ParameterSetOperation> operations) {
        try {
            for (ParameterSetOperation operation : operations) {
                operation.getMethod().invoke(ps, operation.getArgs());
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
