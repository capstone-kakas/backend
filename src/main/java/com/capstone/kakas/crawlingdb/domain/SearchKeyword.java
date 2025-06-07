package com.capstone.kakas.crawlingdb.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_keyword")
@Data
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
}
