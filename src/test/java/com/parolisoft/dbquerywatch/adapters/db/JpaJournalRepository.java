package com.parolisoft.dbquerywatch.adapters.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
interface JpaJournalRepository extends CrudRepository<JpaJournalEntity, Long> {

    List<JpaJournalEntity> findByPublisher(String publisher);
}
