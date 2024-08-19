package org.dbquerywatch.api.spring.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The DbQueryWatch annotation is used to mark a class or method with the ability to watch and analyze database queries.
 * This annotation can be used on classes or methods to enable the monitoring of database queries during test execution.
 * <p>
 * The DbQueryWatch annotation supports the following attributes:
 * - maxSeqScans: The maximum number of sequential scans allowed. The default value is Integer.MAX_VALUE.
 * - maxSelects: The maximum number of SELECT queries allowed. The default value is Integer.MAX_VALUE.
 * - maxTotalCost: The maximum total cost of queries allowed. The default value is Long.MAX_VALUE.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 *    \@DbQueryWatch(maxSeqScans = 5, maxSelects = 10, maxTotalCost = 1000)
 *    public class MyClass {
 *        // class code here
 *    }
 * }</pre>
 *
 * In the above example, the MyClass class is marked with the DbQueryWatch annotation with custom values for the
 * maxSeqScans, maxSelects, and maxTotalCost attributes. At the end of test execution, the DbQueryWatchExtension class
 * will monitor the database queries performed by MyClass and analyze them based on the specified limits. If any
 * query exceeds the allowed limits, a test failure will occur.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(DbQueryWatchExtension.class)
public @interface DbQueryWatch {
    /**
     * Defines whether SeqScans are allowed for database queries.
     *
     * @return true if SeqScans are allowed, false otherwise
     */
    boolean allowSeqScans() default true;

    /**
     * Returns the maximum overall cost of queries allowed for database queries.
     *
     * @return the maximum overall cost of queries allowed
     */
    long maxOverallCost() default Long.MAX_VALUE;
}
