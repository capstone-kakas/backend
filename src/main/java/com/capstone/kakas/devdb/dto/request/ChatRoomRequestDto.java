package com.capstone.kakas.devdb.dto.request;

import lombok.*;

public class ChatRoomRequestDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createChatRoomDto {
//        private Long memberId;
        private String chatRoomTitle;
        private String content;
        private String seller;
        private Integer price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomAssignProductDto {
        private Long chatRoomId;
        private String productName;
    }
}
