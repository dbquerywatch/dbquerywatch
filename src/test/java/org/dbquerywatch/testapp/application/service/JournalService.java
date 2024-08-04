package org.dbquerywatch.testapp.application.service;

import org.dbquerywatch.testapp.application.out.JournalRepository;
import org.dbquerywatch.testapp.domain.Journal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {

    private final JournalRepository repository;

    public JournalService(JournalRepository repository) {
        this.repository = repository;
    }

    public List<Journal> findByPublisher(String publisher) {
        return repository.findByPublisher(publisher);
    }
}
