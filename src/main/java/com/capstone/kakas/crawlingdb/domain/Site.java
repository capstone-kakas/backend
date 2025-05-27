package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.crawlingdb.domain.enums.SiteName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "site")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {
    /**
     * 사이트 정보 엔티티 (공식몰 및 중고마켓)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiteName siteName;

    @Column(nullable = false, length = 255)
    private String searchUrlTemplate;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteScraperRule> scraperRules;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteModelMapping> modelMappings;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteSearchOverride> searchOverrides;
}

