package com.parolisoft.dbquerywatch.testapp.adapters.db;

import com.parolisoft.dbquerywatch.testapp.application.out.JournalRepository;
import com.parolisoft.dbquerywatch.testapp.domain.Journal;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
class DefaultJournalRepository implements JournalRepository {

    private final Jdbi jdbi;

    public DefaultJournalRepository(DataSource datasource) {
        this.jdbi = Jdbi.create(datasource)
            .registerRowMapper(BeanMapper.factory(Journal.class));
    }

    @Override
    public List<Journal> findByPublisher(String publisher) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT id, name, publisher " +
                "FROM journals WHERE publisher = :publisher";
            return handle.createQuery(sql)
                .bind("publisher", publisher)
                .mapTo(Journal.class)
                .list();
        });
    }
}
