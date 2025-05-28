package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exclude_keyword")
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcludeKeyword {
    /**
     * 검색 제외 키워드 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_code_id", nullable = false)
    private ModelCode modelCode;

    @Column(nullable = false, length = 50)
    private String keyword;
}
