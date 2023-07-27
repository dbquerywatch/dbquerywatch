package org.dbquerywatch.testapp.infra.jdbi;

import com.google.common.base.Joiner;
import lombok.experimental.UtilityClass;
import org.jdbi.v3.core.statement.Query;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class Conditions {

    public static List<Condition> of(Condition... conditions) {
        return Arrays.stream(conditions)
            .filter(Objects::nonNull)
            .toList();
    }

    public static String whereClause(List<Condition> conditions) {
        if (conditions.isEmpty()) {
            return "";
        }
        Iterator<String> predicates = conditions.stream()
            .map(Condition::predicate)
            .iterator();
        return "WHERE " + Joiner.on(" AND ").join(predicates);
    }

    public static Query customize(Query query, List<Condition> conditions) {
        conditions.forEach(condition -> condition.customizer().accept(query));
        return query;
    }
}
