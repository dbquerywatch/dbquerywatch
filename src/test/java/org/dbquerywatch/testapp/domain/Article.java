package org.dbquerywatch.testapp.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Article {
    Long id;
    LocalDate publishedAt;
    String authorFullName;
    String authorLastName;
    String title;
    Journal journal;
}
