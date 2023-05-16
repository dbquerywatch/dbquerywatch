package com.parolisoft.dbquerywatch.adapters.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
class JpaArticleEntity {

    @Id
    Long id;

    LocalDate publishedAt;

    String authorFullName;

    String authorLastName;

    @Column(length = 100)
    String title;
}
