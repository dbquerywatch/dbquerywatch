package com.parolisoft.dbquerywatch.testapp.application.out;

import com.parolisoft.dbquerywatch.testapp.domain.Journal;

import java.util.List;

public interface JournalRepository {

    List<Journal> findByPublisher(String publisher);
}
