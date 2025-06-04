package com.capstone.kakas.devdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.devdb.dto.request.ChatRoomRequestDto;
import com.capstone.kakas.devdb.dto.response.ChatRoomResponseDto;
import com.capstone.kakas.devdb.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatController {

    private final ChatRoomService chatRoomService;


    //채팅방 생성 controller
    @PostMapping
    public ApiResponse<ChatRoomResponseDto.addChatRoomDto> CreateChatRoom(
            @RequestBody ChatRoomRequestDto.createChatRoomDto request
    ){

        ChatRoomResponseDto.addChatRoomDto response = chatRoomService.addChatRoom(request);

        return ApiResponse.onSuccess(response);
    }

    //채팅방 상품 설정 controller
    @PutMapping
    public ApiResponse<ChatRoomResponseDto.ChatRoomAssignProductDto> assignProduct(
            @RequestBody ChatRoomRequestDto.ChatRoomAssignProductDto request
    ) {

        ChatRoomResponseDto.ChatRoomAssignProductDto response = chatRoomService.assignProductToChatRoom(request);

        return ApiResponse.onSuccess(response);
    }

    //채팅방 메세지 분석 controller
    @PostMapping("/message")
    public ApiResponse<String> messageAnalysis(
            @RequestBody ChatRoomRequestDto.messageAnalysisDto request
    ){
        String response = chatRoomService.messageAnalysis(request);
        return ApiResponse.onSuccess(response);
    }
}
