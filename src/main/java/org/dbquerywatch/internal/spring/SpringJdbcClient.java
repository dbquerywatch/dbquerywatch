package org.dbquerywatch.internal.spring;

import org.dbquerywatch.internal.jdbc.AbstractJdbcClient;
import org.dbquerywatch.internal.jdbc.NamedDataSource;
import org.dbquerywatch.internal.jdbc.PreparedStatementSetter;
import org.dbquerywatch.internal.jdbc.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class SpringJdbcClient extends AbstractJdbcClient {

    private final JdbcTemplate jdbcTemplate;

    SpringJdbcClient(NamedDataSource namedDataSource) {
        super(namedDataSource);
        this.jdbcTemplate = new JdbcTemplate(namedDataSource.getDataSource());
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, @Nullable Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    @Override
    protected  <T> Optional<T> query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return jdbcTemplate.query(sql, pss::setValues, rse::extractData);
    }
}
