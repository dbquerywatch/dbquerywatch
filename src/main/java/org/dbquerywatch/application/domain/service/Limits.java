package org.dbquerywatch.application.domain.service;

import org.immutables.value.Value;

@Value.Immutable
public interface Limits {
    boolean allowSeqScans();
}
