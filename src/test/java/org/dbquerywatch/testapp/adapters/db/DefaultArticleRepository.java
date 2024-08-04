package org.dbquerywatch.testapp.adapters.db;

import org.dbquerywatch.testapp.application.out.ArticleRepository;
import org.dbquerywatch.testapp.application.service.ArticleQuery;
import org.dbquerywatch.testapp.domain.Article;
import org.dbquerywatch.testapp.infra.jdbi.Condition;
import org.dbquerywatch.testapp.infra.jdbi.Conditions;
import org.dbquerywatch.testapp.infra.jdbi.ImmutableCondition;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Repository
class DefaultArticleRepository implements ArticleRepository {

    private final Jdbi jdbi;

    public DefaultArticleRepository(DataSource datasource) {
        this.jdbi = Jdbi.create(datasource);
        this.jdbi.getConfig(JdbiImmutables.class)
            .registerImmutable(Article.class);
    }

    @Override
    public Optional<Article> findById(long id) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT id, published_at, author_full_name, author_last_name, title " +
                "FROM articles WHERE id = :id";
            return handle.createQuery(sql)
                .bind("id", id)
                .mapTo(Article.class)
                .findFirst();
        });
    }

    @Override
    public List<Article> query(ArticleQuery query) {
        List<Condition> conditions = Conditions.of(
            authorLastName(query.getAuthorLastName()),
            fromYear(query.getFromYear()),
            toYear(query.getToYear())
        );

        return jdbi.withHandle(handle -> {
            String sql = "SELECT id, published_at, author_full_name, author_last_name, title " +
                "FROM articles " + Conditions.whereClause(conditions);
            return Conditions.customize(handle.createQuery(sql), conditions)
                .mapTo(Article.class)
                .list();
        });
    }

    private static @Nullable Condition authorLastName(@Nullable String str) {
        if (str == null) {
            return null;
        }
        return ImmutableCondition.of("author_last_name = :authorLastName", query -> query.bind("authorLastName", str));
    }

    private static @Nullable Condition fromYear(@Nullable Integer year) {
        if (year == null) {
            return null;
        }
        return ImmutableCondition.of("published_at >= :fromYear",
            query -> query.bind("fromYear", LocalDate.of(year, Month.JANUARY, 1)));
    }

    private static @Nullable Condition toYear(@Nullable Integer year) {
        if (year == null) {
            return null;
        }
        return ImmutableCondition.of("published_at <= :toYear",
            query -> query.bind("toYear", LocalDate.of(year, Month.DECEMBER, 31)));
    }
}
