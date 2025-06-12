package com.capstone.kakas.devdb.controller;

import com.capstone.kakas.apiPayload.ApiResponse;
import com.capstone.kakas.devdb.dto.request.ChatRoomRequestDto;
import com.capstone.kakas.devdb.dto.response.AiApiResponse;
import com.capstone.kakas.devdb.dto.response.ChatRoomResponseDto;
import com.capstone.kakas.devdb.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatController {

    private final ChatRoomService chatRoomService;


    //채팅방 생성 controller
    @PostMapping
    @Operation(summary = "채팅방 생성 API / 완료",description = "상품이 결정되기 전의 채팅방 생성 / response로 상품4개 추천")
    public ApiResponse<ChatRoomResponseDto.addChatRoomDto> CreateChatRoom(
            @RequestBody ChatRoomRequestDto.createChatRoomDto request
    ){

        ChatRoomResponseDto.addChatRoomDto response = chatRoomService.addChatRoom(request);

        return ApiResponse.onSuccess(response);
    }

    //채팅방 상품 설정 controller
    @PutMapping
    @Operation(summary = "채팅방 상품 결정 API / 완료",description = "채팅방 생성 Api 사용 후 상품 결정하는 api / 상품이름를 채팅방에 설정")
    public ApiResponse<ChatRoomResponseDto.ChatRoomAssignProductDto> assignProduct(
            @RequestBody ChatRoomRequestDto.ChatRoomAssignProductDto request
    ) {

        ChatRoomResponseDto.ChatRoomAssignProductDto response = chatRoomService.assignProductToChatRoom(request);

        return ApiResponse.onSuccess(response);
    }



    //채팅방 첫질문 추천 끝
    // ai api post /recommend
    @GetMapping("/recommend/{chatRoomId}")
    @Operation(summary = "채팅방생성 직후 추천 질문 - 분석 / 연결완료",description = "채팅방 id를 통해 채팅방의 data를 ai 서버에 분석요청 후 추천답변 반환")
    public ApiResponse<List<String>> recommendQuestion(
            @PathVariable("chatRoomId") Long chatRoomId
    ) {

        List<String> response = chatRoomService.recommendQuestion(chatRoomId);

        return ApiResponse.onSuccess(response);
    }




    //chat-seller
    //진위여부판단
    @PostMapping("/isReal")
    @Operation(summary = "진위여부판단 API / 연결완료",description = "채팅방id와 1개의 메세지 string 통해 ai 분석을 반환")
    public ApiResponse<String> chatSeller(
            @RequestBody ChatRoomRequestDto.chatSellerDto request
    ){
        String response = chatRoomService.chatSeller(request);
        return ApiResponse.onSuccess(response);
    }



    //채팅방 메세지 분석 controller
    // recommend-chat
    @PostMapping("/message")
    @Operation(summary = "채팅방 메세지 분석 API ai->recommend-chat / 연결완료",description = "채팅방id와 메세지를 통해 ai 분석을 반환")
    public ApiResponse<List<String>> messageAnalysis(
            @RequestBody ChatRoomRequestDto.messageAnalysisDto request
    ){
        List<String> response = chatRoomService.messageAnalysis(request);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/chat")
    @Operation(summary = "사용자 질문만 받아서 웹 검색해서 답장 API ai->chat",description = "question만 보내서 string response")
    public ApiResponse<String> chatAnalysis(
            @RequestBody ChatRoomRequestDto.chatDto request
    ){
        String response = chatRoomService.chatAnalysis(request);
        return ApiResponse.onSuccess(response);
    }



    @PostMapping("/message/async")
    @Operation(summary = "채팅방 메세지 분석 API - 사용X",description = "채팅방id와 메세지를 통해 ai 분석을 반환 / ai api 비동기적으로 사용")
    public ApiResponse<CompletableFuture<String>> asyncMessageAnalysis(
            @RequestBody ChatRoomRequestDto.messageAnalysisDto request
    ){
        CompletableFuture<String> response = chatRoomService.messageAnalysisAsync(request);
        return ApiResponse.onSuccess(response);
    }
}
