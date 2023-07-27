package org.dbquerywatch.testapp.application.out;

import org.dbquerywatch.testapp.domain.Journal;

import java.util.List;

public interface JournalRepository {

    List<Journal> findByPublisher(String publisher);
}
