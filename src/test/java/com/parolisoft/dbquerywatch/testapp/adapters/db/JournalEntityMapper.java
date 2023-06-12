package com.parolisoft.dbquerywatch.testapp.adapters.db;

import com.parolisoft.dbquerywatch.testapp.domain.Journal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface JournalEntityMapper {

    JpaJournalEntity toJpa(Journal journal);

    Journal fromJpa(JpaJournalEntity jpaJournalEntity);
}
