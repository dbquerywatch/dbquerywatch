package com.parolisoft.dbquerywatch.testapp.adapters.api;

import com.parolisoft.dbquerywatch.testapp.application.service.ArticleQuery;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ArticleQueryMapper {
    ArticleQuery fromModel(ArticleQueryModel model);
}
