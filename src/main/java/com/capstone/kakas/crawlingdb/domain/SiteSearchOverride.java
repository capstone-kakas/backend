package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_search_override")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSearchOverride {
    /**
     * 사이트별 검색 오버라이드 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_code_id", nullable = false)
    private ModelCode modelCode;

    @Column(nullable = false, length = 255)
    private String overrideQuery;

    @Column(nullable = false)
    private Integer priority;
}
