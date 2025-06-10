package com.capstone.kakas.devdb.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiApiResponse {
    private List<String> response;
    private double latency;

    // 분석 결과를 문자열로 반환하는 메서드
    public String getAnalysis() {
        if (response == null || response.isEmpty()) {
            return "분석 결과를 받을 수 없습니다.";
        }

        return String.join("\n", response);
    }
}
