package com.parolisoft.dbquerywatch.application.out;

import com.parolisoft.dbquerywatch.application.service.ArticleQuery;
import com.parolisoft.dbquerywatch.domain.Article;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    void save(Article article);

    Optional<Article> findById(long id);

    List<Article> query(ArticleQuery query, Pageable pageable);
}
