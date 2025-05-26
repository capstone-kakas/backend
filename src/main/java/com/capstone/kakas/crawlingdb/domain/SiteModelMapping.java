package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_model_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteModelMapping {
    /**
     * 사이트별 모델↔상품 매핑 엔티티
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

    @Column(nullable = false, length = 100)
    private String siteProductId;
}
