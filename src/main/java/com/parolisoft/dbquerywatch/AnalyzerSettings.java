package com.parolisoft.dbquerywatch;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
class AnalyzerSettings {
    @Builder.Default
    List<String> smallTables = new ArrayList<>();
}
