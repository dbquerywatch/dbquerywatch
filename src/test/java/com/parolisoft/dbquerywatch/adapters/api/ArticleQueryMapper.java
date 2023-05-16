package com.parolisoft.dbquerywatch.adapters.api;

import com.parolisoft.dbquerywatch.application.service.ArticleQuery;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ArticleQueryMapper {
    ArticleQuery fromModel(ArticleQueryModel model);
}
