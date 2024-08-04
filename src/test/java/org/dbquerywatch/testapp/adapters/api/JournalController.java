package org.dbquerywatch.testapp.adapters.api;

import org.dbquerywatch.testapp.application.service.JournalService;
import org.dbquerywatch.testapp.domain.Journal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/journals")
class JournalController {

    private final JournalService journalService;

    JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/{publisher}")
    public List<Journal> getJournal(@PathVariable("publisher") String publisher) {
        return journalService.findByPublisher(publisher);
    }
}
