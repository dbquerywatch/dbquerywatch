package org.dbquerywatch.application.port.out;

import org.dbquerywatch.application.domain.model.SeqScan;
import org.immutables.value.Value;

import java.util.List;

public class AnalysisReport {
    private final String executionPlan;
    private final List<SeqScan> seqScans;
    private final long totalCost;

    public AnalysisReport(String executionPlan, List<SeqScan> seqScans, long totalCost) {
        this.executionPlan = executionPlan;
        this.seqScans = seqScans;
        this.totalCost = totalCost;
    }

    public String getExecutionPlan() {
        return executionPlan;
    }

    public List<SeqScan> getSeqScans() {
        return seqScans;
    }

    public long getTotalCost() {
        return totalCost;
    }
}
