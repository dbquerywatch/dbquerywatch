package com.parolisoft.dbquerywatch.testapp.adapters.db;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
interface JpaArticleRepository extends CrudRepository<JpaArticleEntity, Long>, JpaSpecificationExecutor<JpaArticleEntity> {
}
