package com.parolisoft.dbquerywatch.testapp.application.out;

import com.parolisoft.dbquerywatch.testapp.application.service.ArticleQuery;
import com.parolisoft.dbquerywatch.testapp.domain.Article;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    void save(Article article);

    Optional<Article> findById(long id);

    List<Article> query(ArticleQuery query, Pageable pageable);
}
