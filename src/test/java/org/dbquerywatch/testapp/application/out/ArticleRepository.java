package org.dbquerywatch.testapp.application.out;

import org.dbquerywatch.testapp.application.service.ArticleQuery;
import org.dbquerywatch.testapp.domain.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    Optional<Article> findById(long id);

    List<Article> query(ArticleQuery query);
}
