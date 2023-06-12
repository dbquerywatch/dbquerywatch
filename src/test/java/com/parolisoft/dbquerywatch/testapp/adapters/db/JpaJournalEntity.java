package com.parolisoft.dbquerywatch.testapp.adapters.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "journals")
@Data
@NoArgsConstructor
class JpaJournalEntity {

    @Id
    @Column(length = 10)
    String id;

    @Column
    String name;

    @Column
    String publisher;
}
