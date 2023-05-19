package com.parolisoft.dbquerywatch.adapters.api;

import com.parolisoft.dbquerywatch.application.service.JournalService;
import com.parolisoft.dbquerywatch.domain.Journal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/journals")
class JournalController {

    private final JournalService journalService;

    @GetMapping("/{publisher}")
    public List<Journal> getJournal(@PathVariable String publisher) {
        return journalService.findByPublisher(publisher);
    }
}
