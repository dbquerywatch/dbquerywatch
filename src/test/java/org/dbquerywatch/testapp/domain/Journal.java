package org.dbquerywatch.testapp.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Journal {
    String id;
    String name;
    String publisher;
}
