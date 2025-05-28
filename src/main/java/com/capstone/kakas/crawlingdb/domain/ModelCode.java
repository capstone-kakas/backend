package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "model_code")
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelCode {
    /**
     * 모델 코드 엔티티 (제조사 공식 코드)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @OneToMany(mappedBy = "modelCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SearchKeyword> includeKeywords;

    @OneToMany(mappedBy = "modelCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExcludeKeyword> excludeKeywords;

    @OneToMany(mappedBy = "modelCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteSearchOverride> searchOverrides;

    @OneToMany(mappedBy = "modelCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteModelMapping> siteMappings;
}
