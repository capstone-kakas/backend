package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_scraper_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteScraperRule {
    /**
     * 사이트별 스크래퍼 룰 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false, length = 50)
    private String fieldName;

    @Column(nullable = false, length = 150)
    private String cssSelector;
}
