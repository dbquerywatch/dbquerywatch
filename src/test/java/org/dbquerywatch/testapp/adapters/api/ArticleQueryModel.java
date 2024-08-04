package org.dbquerywatch.testapp.adapters.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

@Value.Immutable
@JsonDeserialize(as = ImmutableArticleQueryModel.class)
interface ArticleQueryModel {
    @Nullable
    @JsonProperty("author_last_name")
    String getAuthorLastName();

    @Nullable
    @JsonProperty("from_year")
    Integer getFromYear();

    @Nullable
    @JsonProperty("to_year")
    Integer getToYear();

    @Nullable
    @JsonProperty("journal_name")
    String getJournalName();
}
