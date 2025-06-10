package com.capstone.kakas.crawlingdb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterKeywords {
    private List<String> searchKeywords;
    private List<String> excludeKeywords;
}
