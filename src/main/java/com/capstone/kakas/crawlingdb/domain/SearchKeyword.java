package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "search_keyword")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchKeyword {
    /**
     * 검색 포함 키워드 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
