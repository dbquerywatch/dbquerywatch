package org.dbquerywatch.testapp.application.service;

import org.dbquerywatch.testapp.application.out.ArticleRepository;
import org.dbquerywatch.testapp.domain.Article;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public Optional<Article> findById(long id) {
        return repository.findById(id);
    }

    public List<Article> query(ArticleQuery query) {
        return repository.query(query);
    }
}
