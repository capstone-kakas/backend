package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.devdb.domain.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Product {
    /**
     * 최상위 제품 엔티티 (예: PlayStation 5)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String saleUrl;

    @Column(length = 109)
    private String bunjangUrl;

}
