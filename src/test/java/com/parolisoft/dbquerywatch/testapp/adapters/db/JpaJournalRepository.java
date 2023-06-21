package com.parolisoft.dbquerywatch.testapp.adapters.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface JpaJournalRepository extends CrudRepository<JpaJournalEntity, Long> {

    List<JpaJournalEntity> findByPublisher(String publisher);
}
