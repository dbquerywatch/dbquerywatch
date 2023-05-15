package com.parolisoft.dbquerywatch.internal;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@UtilityClass
class JdbcTemplateUtils {

    public static Optional<String> queryForString(
        JdbcTemplate jdbcTemplate,
        String querySql,
        List<ParameterSetOperation> operations
    ) {
        return jdbcTemplate.query(
            querySql,
            ps -> setParameters(ps, operations),
            rs -> rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty());
    }

    @SneakyThrows
    private static void setParameters(PreparedStatement ps, List<ParameterSetOperation> operations) {
        for (ParameterSetOperation operation : operations) {
            operation.getMethod().invoke(ps, operation.getArgs());
        }
    }
}
