package org.dbquerywatch.application.domain.service;

import org.dbquerywatch.application.domain.model.ReportElement;
import org.dbquerywatch.common.CleanRuntimeException;
import org.dbquerywatch.common.Yaml;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

/**
 * Thrown when one or multiple issues are found on the queries performed by the integration tests.
 */
public class DatabasePerformanceIssuesDetectedException extends CleanRuntimeException {

    private final List<ReportElement> reports;

    /**
     * Creates an instance of the exception based on collection of ReportElement items.
     *
     * @param reports The ReportElement items.
     */
    public DatabasePerformanceIssuesDetectedException(List<ReportElement> reports) {
        super(describe(reports));
        this.reports = new ArrayList<>(reports);
    }

    /**
     * Retrieves the ReportElement items.
     *
     * @return The ReportElement items.
     */
    public List<ReportElement> getReports() {
        return unmodifiableList(reports);
    }

    private static String describe(List<ReportElement> reports) {
        StringBuilder sb = new StringBuilder(format("Found %d database performance issues!\n", reports.size()));
        Yaml yaml = new Yaml(sb);
        yaml.toYaml(reports);
        return sb.toString();
    }
}
