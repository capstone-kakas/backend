package com.capstone.kakas.devdb.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiApiResponse {
    private String analysis;
    private String status;
}
