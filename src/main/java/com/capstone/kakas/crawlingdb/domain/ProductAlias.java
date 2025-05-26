package com.capstone.kakas.crawlingdb.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_alias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAlias {
    /**
     * 제품 별칭(동의어) 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String alias;

    @Column(nullable = false, length = 20)
    private String type;  // SHORT, LOCALE, BRAND 등
}

