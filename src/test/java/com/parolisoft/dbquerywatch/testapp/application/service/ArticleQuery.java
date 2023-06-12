package com.parolisoft.dbquerywatch.testapp.application.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@NoArgsConstructor
public class ArticleQuery {
    @Nullable
    String authorLastName;

    @Nullable
    Integer fromYear;

    @Nullable
    Integer toYear;

    @Nullable
    String journalName;
}
