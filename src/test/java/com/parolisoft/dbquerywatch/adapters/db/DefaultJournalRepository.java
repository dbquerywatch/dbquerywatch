package com.parolisoft.dbquerywatch.adapters.db;

import com.parolisoft.dbquerywatch.application.out.JournalRepository;
import com.parolisoft.dbquerywatch.domain.Journal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DefaultJournalRepository implements JournalRepository {

    private final JpaJournalRepository jpaRepository;
    private final JournalEntityMapper entityMapper;

    @Override
    public List<Journal> findByPublisher(String publisher) {
        return jpaRepository.findByPublisher(publisher).stream()
            .map(entityMapper::fromJpa)
            .collect(Collectors.toList());
    }
}
