package org.dbquerywatch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "org.dbquerywatch", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    private static final String BASE_PACKAGE = "org.dbquerywatch.";

    @ArchTest
    static final ArchRule no_cycles_in_classes = slices().matching(BASE_PACKAGE + "(*)..")
        .should().beFreeOfCycles();
}
