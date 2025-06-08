package com.capstone.kakas.crawlingdb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilteredResultDto {
    private Long productId;
    private String productName;
    private Integer originalItemCount;
    private Integer filteredItemCount;
    private List<BunjangItemDto> filteredItems;
    private LocalDateTime filteredAt;
}
