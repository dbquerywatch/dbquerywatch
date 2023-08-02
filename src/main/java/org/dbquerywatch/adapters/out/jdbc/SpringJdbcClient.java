package org.dbquerywatch.adapters.out.jdbc;

import org.dbquerywatch.application.domain.model.NamedDataSource;
import org.dbquerywatch.application.port.out.AbstractJdbcClient;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("SqlSourceToSinkFlow")
public class SpringJdbcClient extends AbstractJdbcClient {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcClient(NamedDataSource namedDataSource) {
        super(namedDataSource);
        this.jdbcTemplate = new JdbcTemplate(namedDataSource.getDataSource());
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, @Nullable Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    @Override
    protected  <T> Optional<T> query(String sql, Consumer<PreparedStatement> pss, ResultSetExtractor<T> rse) {
        return jdbcTemplate.query(sql, pss::accept, rse::extractData);
    }
}
