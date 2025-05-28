package com.capstone.kakas.crawlingdb.repository;

import com.capstone.kakas.crawlingdb.domain.Site;
import com.capstone.kakas.crawlingdb.domain.SiteScraperRule;
import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteScraperRuleRepository extends JpaRepository<SiteScraperRule, Long> {
    List<SiteScraperRule> findBySiteName(SiteName siteName);

    List<SiteScraperRule> findBySite(Site site);
}
