package com.capstone.kakas.crawlingdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.capstone.kakas.crawlingdb.service.OfficialCrawlService;

@Service
@RequiredArgsConstructor
public class CrawlingScheduler {
    private final OfficialCrawlService officialService;

    @Scheduled(cron = "0 0 1 * * *")
    public void officialJob() {
        officialService.crawlAndSaveAllOfficial();
    }
}
