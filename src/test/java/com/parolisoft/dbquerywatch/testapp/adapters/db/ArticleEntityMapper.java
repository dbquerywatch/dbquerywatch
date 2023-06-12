package com.parolisoft.dbquerywatch.testapp.adapters.db;

import com.parolisoft.dbquerywatch.testapp.domain.Article;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ArticleEntityMapper {

    JpaArticleEntity toJpa(Article article);

    Article fromJpa(JpaArticleEntity jpaArticleEntity);
}
