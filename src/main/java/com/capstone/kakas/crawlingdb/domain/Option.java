package com.capstone.kakas.crawlingdb.domain;

import com.capstone.kakas.crawlingdb.domain.enums.OptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {
    /**
     * 제품 옵션 엔티티 (저장용량, 색상 등)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OptionType type;

    @Column(nullable = false, length = 30)
    private String value;
}
