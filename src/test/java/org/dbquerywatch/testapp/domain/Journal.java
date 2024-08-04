package org.dbquerywatch.testapp.domain;

import org.immutables.value.Value;

@Value.Immutable
public interface Journal {
    String getId();

    String getName();

    String getPublisher();
}
