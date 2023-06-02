package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.internal.jdbc.AbstractJdbcClient;
import com.parolisoft.dbquerywatch.internal.jdbc.PreparedStatementSetter;
import com.parolisoft.dbquerywatch.internal.jdbc.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpringJdbcClient extends AbstractJdbcClient {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcClient(String dataSourceName, DataSource dataSource) {
        super(dataSourceName);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
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
