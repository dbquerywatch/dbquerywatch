package org.dbquerywatch.application.domain.model;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Value.Immutable
@SuppressWarnings("java:S1948")  // we are only using serializable implementations of Set/List for these fields
public interface PerStatementIssuesReport extends ReportElement, StatementReport, Serializable {

    List<SeqScan> getSeqScans();

    @Override
    default String title() {
        return "SeqScan";
    }

    @Override
    default Map<String, Object> toPojo() {
        Map<String, Object> result = StatementReport.super.toPojo();
        result.put("SeqScans", getSeqScans());
        return result;
    }
}
