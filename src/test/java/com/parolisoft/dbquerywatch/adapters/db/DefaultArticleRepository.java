package com.parolisoft.dbquerywatch.adapters.db;

import com.parolisoft.dbquerywatch.application.out.ArticleRepository;
import com.parolisoft.dbquerywatch.application.service.ArticleQuery;
import com.parolisoft.dbquerywatch.domain.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DefaultArticleRepository implements ArticleRepository {

    private final JpaArticleRepository jpaRepository;
    private final ArticleEntityMapper entityMapper;

    @Override
    public void save(Article article) {
        jpaRepository.save(entityMapper.toJpa(article));
    }

    @Override
    public Optional<Article> findById(long id) {
        return jpaRepository.findById(id)
                .map(entityMapper::fromJpa);
    }

    @Override
    public List<Article> query(ArticleQuery query, Pageable pageable) {
        Specification<JpaArticleEntity> spec = Specification.where(authorLastName(query.getAuthorLastName()))
                .and(fromYear(query.getFromYear()))
                .and(toYear(query.getToYear()));
        return jpaRepository.findAll(spec, pageable).stream()
                .map(entityMapper::fromJpa)
                .collect(Collectors.toList());
    }

    private static @Nullable Specification<JpaArticleEntity> authorLastName(@Nullable String str) {
        if (str == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("authorLastName"), str);
    }

    private static @Nullable Specification<JpaArticleEntity> fromYear(@Nullable Integer year) {
        if (year == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("publishedAt"),
                LocalDate.of(year, Month.JANUARY, 1)
        );
    }

    private static @Nullable Specification<JpaArticleEntity> toYear(@Nullable Integer year) {
        if (year == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                root.get("publishedAt"),
                LocalDate.of(year, Month.DECEMBER, 31)
        );
    }
}
