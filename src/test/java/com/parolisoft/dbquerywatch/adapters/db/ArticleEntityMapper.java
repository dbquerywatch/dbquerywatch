package com.parolisoft.dbquerywatch.adapters.db;

import com.parolisoft.dbquerywatch.domain.Article;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ArticleEntityMapper {

    JpaArticleEntity toJpa(Article article);

    Article fromJpa(JpaArticleEntity jpaArticleEntity);
}
