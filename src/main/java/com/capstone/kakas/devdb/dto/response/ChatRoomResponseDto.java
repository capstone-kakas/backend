package com.capstone.kakas.devdb.dto.response;

import lombok.*;

import java.util.List;

public class ChatRoomResponseDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class addChatRoomDto {
        private Long chatRoomId;
        private List<String> suggestedProductNames;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomAssignProductDto {
        private Long chatRoomId;
        private Long productId;
        private String productName;
    }

//    @Getter
//    @Setter
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class messageAnalysisResultDto{
//        private String messageAnalysis;
//    }
}
