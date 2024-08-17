package org.dbquerywatch.application.port.out;

import org.dbquerywatch.application.domain.model.SeqScan;

import java.util.List;

public class AnalysisReport {
    private final String executionPlan;
    private final List<SeqScan> seqScans;

    public AnalysisReport(String executionPlan, List<SeqScan> seqScans) {
        this.executionPlan = executionPlan;
        this.seqScans = seqScans;
    }

    public String getExecutionPlan() {
        return executionPlan;
    }

    public List<SeqScan> getSeqScans() {
        return seqScans;
    }
}
