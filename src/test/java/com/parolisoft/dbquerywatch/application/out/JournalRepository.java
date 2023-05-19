package com.parolisoft.dbquerywatch.application.out;

import com.parolisoft.dbquerywatch.domain.Journal;

import java.util.List;

public interface JournalRepository {

    List<Journal> findByPublisher(String publisher);
}
