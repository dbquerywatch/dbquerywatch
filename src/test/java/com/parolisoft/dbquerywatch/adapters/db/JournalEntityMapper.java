package com.parolisoft.dbquerywatch.adapters.db;

import com.parolisoft.dbquerywatch.domain.Journal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface JournalEntityMapper {

    JpaJournalEntity toJpa(Journal journal);

    Journal fromJpa(JpaJournalEntity jpaJournalEntity);
}
