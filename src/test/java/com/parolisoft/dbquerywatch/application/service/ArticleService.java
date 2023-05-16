package com.parolisoft.dbquerywatch.application.service;

import com.parolisoft.dbquerywatch.application.out.ArticleRepository;
import com.parolisoft.dbquerywatch.domain.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repository;

    public Optional<Article> findById(long id) {
        return repository.findById(id);
    }

    public List<Article> query(ArticleQuery query, Pageable pageable) {
        return repository.query(query, pageable);
    }
}
