package com.capstone.kakas.devdb.service;

import com.capstone.kakas.apiPayload.code.status.ErrorStatus;
import com.capstone.kakas.apiPayload.exception.handler.TempHandler;
import com.capstone.kakas.devdb.domain.*;
import com.capstone.kakas.devdb.domain.enums.ProductCategory;
import com.capstone.kakas.devdb.dto.request.AiRequestDto;
import com.capstone.kakas.devdb.dto.request.ChatRoomRequestDto;
import com.capstone.kakas.devdb.dto.response.AiApiResponse;
import com.capstone.kakas.devdb.dto.response.ChatRoomResponseDto;
import com.capstone.kakas.devdb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final DEVProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ChatAnalysisRepository chatAnalysisRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebClient webClient;


    private static final String AI_RECOMMEND_API = "http://52.63.203.92:3000/recommend";



    // 카테고리별 상품 데이터 - ProductCategory enum 활용
    private static final Map<ProductCategory, List<String>> CATEGORY_PRODUCTS = Map.of(
            ProductCategory.NINTENDO, Arrays.asList(
                    "닌텐도 스위치 OLED",
                    "닌텐도 스위치 OLED 스플래툰 3 에디션",
                    "닌텐도 스위치 OLED 모여봐요 동물의숲 에디션",
                    "닌텐도 스위치 OLED 젤다의 전설 왕국의 눈물 에디션",
                    "닌텐도 스위치 OLED 스칼렛 바이올렛 에디션",
                    "닌텐도 스위치 OLED 마리오 레드 에디션",
                    "닌텐도 스위치 라이트",
                    "닌텐도 스위치 스포츠 번들 세트 본체",
                    "닌텐도 스위치 프로 컨트롤러 번들",
                    "닌텐도 스위치2",
                    "닌텐도 3DS",
                    "닌텐도 3DS XL",
                    "New 닌텐도 3DS",
                    "New 닌텐도 3DS XL",
                    "닌텐도 2DS",
                    "New 닌텐도 2DS XL"
            ),
            ProductCategory.PLAYSTATION, Arrays.asList(
                    "플레이스테이션 5 825GB 본체",
                    "플레이스테이션 5 825GB 디지털 에디션",
                    "플레이스테이션 5 슬림 1TB",
                    "플레이스테이션 5 슬림 1TB 듀얼센스 번들 본체",
                    "플레이스테이션 5 슬림 디지털 에디션",
                    "플레이스테이션 5 프로 2TB",
                    "플레이스테이션 5 프로 2TB 듀얼센스 번들",
                    "플레이스테이션 5 디지털 에디션 30주년 기념 한정판",
                    "플레이스테이션 5 디지털 에디션 몬스터 헌터 와일즈 에디션",
                    "플레이스테이션 5 듀얼센스 엣지 컨트롤러 번들",
                    "플레이스테이션 포털 리모트 플레이어",
                    "플레이스테이션 4 시리즈",
                    "플레이스테이션 4 슬림 500GB",
                    "플레이스테이션 4 슬림 1TB",
                    "플레이스테이션 4 프로 1TB"
            ),
            ProductCategory.XBOX, Arrays.asList(
                    "엑스박스 시리즈 X 1TB",
                    "엑스박스 시리즈 X 디아블로 IV 번들",
                    "엑스박스 시리즈 S 512GB",
                    "엑스박스 시리즈 S 포르자 호라이즌 5 에디션",
                    "엑스박스 시리즈 X 헤일로 인피니트 한정판",
                    "엑스박스 컨트롤러 + 배터리 세트",
                    "엑스박스 컨트롤러 듀얼 충전독 + 배터리 세트",
                    "엑스박스 엘리트 무선 컨트롤러 시리즈 2"
            )
    );

    // 한국어 상품명에 특화된 키워드 매핑
    private static final Map<String, List<String>> KEYWORD_MAPPING = Map.of(
            "플스", Arrays.asList("플레이스테이션", "PS"),
            "ps", Arrays.asList("플레이스테이션", "플스"),
            "xbox", Arrays.asList("엑스박스"),
            "디지털", Arrays.asList("디지털 에디션"),
            "슬림", Arrays.asList("슬림"),
            "프로", Arrays.asList("프로", "pro"),
            "스위치", Arrays.asList("닌텐도 스위치"),
            "닌텐도", Arrays.asList("닌텐도"),
            "3ds", Arrays.asList("3DS"),
            "2ds", Arrays.asList("2DS")
    );



    @Transactional
    public ChatRoomResponseDto.addChatRoomDto addChatRoom(ChatRoomRequestDto.createChatRoomDto request) {
        Member newMember = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 요청된 제목을 기반으로, 유사도가 높은 상품 이름 상위 4개를 가져온다
        List<String> suggestedProductNames = filteringProductName(request.getChatRoomTitle(), request.getCategory());

        ProductCategory category = ProductCategory.fromCode(request.getCategory());

        ChatRoom chatRoom = ChatRoom.builder()
                .member(newMember)
                .title(request.getChatRoomTitle())
                .content(request.getContent())
                .category(category)
                .deliveryFee(request.getDeliveryFee())
                .seller(request.getSeller())
                .price(request.getPrice())
                .status(request.getStatus())
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return ChatRoomResponseDto.addChatRoomDto.builder()
                .chatRoomId(saved.getId())
                .suggestedProductNames(suggestedProductNames)
                .build();
    }

    /**
     * 제목과 카테고리를 기반으로 유사도가 높은 상품명 상위 4개를 반환
     */
    private List<String> filteringProductName(String title, Integer categoryCode) {
        try {
            // 1. Integer 카테고리 코드를 ProductCategory enum으로 변환
            ProductCategory category = ProductCategory.fromCode(categoryCode);

            // 2. 해당 카테고리의 상품 리스트 가져오기
            List<String> categoryProducts = CATEGORY_PRODUCTS.get(category);
            if (categoryProducts == null || categoryProducts.isEmpty()) {
                return Collections.emptyList();
            }

            // 3. 각 상품과의 유사도 계산
            List<ProductSimilarity> similarities = new ArrayList<>();
            String normalizedTitle = normalizeText(title);

            for (String product : categoryProducts) {
                double similarity = calculateSimilarity(normalizedTitle, normalizeText(product));
                similarities.add(new ProductSimilarity(product, similarity));
            }

            // 4. 유사도 순으로 정렬하여 상위 4개의 상품명만 반환
            return similarities.stream()
                    .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                    .limit(4)
                    .map(ps -> ps.productName)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 카테고리 코드인 경우 빈 리스트 반환
            return Collections.emptyList();
        }
    }

    // 복합 유사도 계산 (여러 알고리즘 조합)
    private double calculateSimilarity(String text1, String text2) {
        // 1. Jaccard 유사도 (토큰 기반)
        double jaccardSim = calculateJaccardSimilarity(text1, text2);

        // 2. 편집 거리 기반 유사도 (Levenshtein)
        double levenshteinSim = calculateLevenshteinSimilarity(text1, text2);

        // 3. 키워드 매칭 점수
        double keywordSim = calculateKeywordSimilarity(text1, text2);

        // 4. 포함 관계 점수 (부분 문자열)
        double containsSim = calculateContainsSimilarity(text1, text2);

        // 가중 평균 (키워드 매칭과 포함 관계에 더 높은 가중치)
        return (jaccardSim * 0.2) + (levenshteinSim * 0.2) + (keywordSim * 0.4) + (containsSim * 0.2);
    }

    // 텍스트 정규화
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Jaccard 유사도 계산
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // Levenshtein 거리 기반 유사도
    private double calculateLevenshteinSimilarity(String text1, String text2) {
        int distance = levenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    // Levenshtein 거리 계산
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    // 키워드 매핑 기반 유사도
    private double calculateKeywordSimilarity(String text1, String text2) {
        double score = 0.0;

        for (Map.Entry<String, List<String>> entry : KEYWORD_MAPPING.entrySet()) {
            String keyword = entry.getKey();
            List<String> synonyms = entry.getValue();

            boolean text1HasKeyword = text1.contains(keyword) ||
                    synonyms.stream().anyMatch(text1::contains);
            boolean text2HasKeyword = text2.contains(keyword) ||
                    synonyms.stream().anyMatch(text2::contains);

            if (text1HasKeyword && text2HasKeyword) {
                score += 0.3; // 키워드 매치당 점수
            }
        }

        return Math.min(score, 1.0); // 최대 1.0으로 제한
    }

    // 포함 관계 기반 유사도
    private double calculateContainsSimilarity(String text1, String text2) {
        String[] tokens1 = text1.split("\\s+");
        String[] tokens2 = text2.split("\\s+");

        int matches = 0;
        for (String token1 : tokens1) {
            for (String token2 : tokens2) {
                if (token1.equals(token2) || token1.contains(token2) || token2.contains(token1)) {
                    matches++;
                    break;
                }
            }
        }

        return (double) matches / Math.max(tokens1.length, tokens2.length);
    }

    // 내부 클래스 - 유사도 계산용
    private static class ProductSimilarity {
        String productName;
        double similarity;

        ProductSimilarity(String productName, double similarity) {
            this.productName = productName;
            this.similarity = similarity;
        }
    }

    @Transactional
    public ChatRoomResponseDto.ChatRoomAssignProductDto assignProductToChatRoom(
            ChatRoomRequestDto.ChatRoomAssignProductDto request
    ) {
        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));
        // Product 조회
        Product product = productRepository.findByName(request.getProductName())
                .orElseThrow(() -> new TempHandler(ErrorStatus.PRODUCT_NOT_FOUND));
        // 연관관계 설정
        chatRoom.setProduct(product);


        // 변경 감지를 위해 save() 호출 (필요 시)
        ChatRoom updated = chatRoomRepository.save(chatRoom);

        // 응답 DTO 생성
        return ChatRoomResponseDto.ChatRoomAssignProductDto.builder()
                .chatRoomId(updated.getId())
                .productId(product.getId())
                .productName(product.getName())
                .build();
    }

    @Transactional
    //특정 채팅방의 메세지를 분석하는 service
//    public ChatRoomResponseDto.messageAnalysisResultDto messageAnalysis(ChatRoomRequestDto.messageAnalysisDto request){
    public List<String> messageAnalysis(ChatRoomRequestDto.messageAnalysisDto request){

        // ChatRoom 조회 id로
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));


        List<ChatRoomRequestDto.messageRequestDto> messages = request.getMessage();

        List<List<String>> chatMessages = messages.stream()
                .map(m -> Arrays.asList(m.getSender(), m.getText()))
                .collect(Collectors.toList());

        String resultMessage = messages.stream()
                .map(m -> m.getSender() + ":" + m.getText())
                .collect(Collectors.joining(", "));


        // ai 앤드포인트를 기준으로 분석 결과 가져오기 아직 미구현
        AiRequestDto.messageAnalysisRequestDto aiRequestDto = AiRequestDto.messageAnalysisRequestDto.builder()
                .chatTitle(chatRoom.getTitle())
                .chatContent(chatRoom.getContent())
                .price(chatRoom.getPrice())
                .status(chatRoom.getStatus())
                .chat(chatMessages)
                .build();
//        aiRequestDto를 ai api로 전송 후 analysisResult 받아오기




        List<String> analysisResultList; // List<String>으로 변경
        String analysisResultString; // DB 저장용은 여전히 String

        try {
            AiApiResponse aiResponse = webClient.post()
                    .uri(AI_RECOMMEND_API)
                    .header("Content-Type", "application/json")
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        return Mono.error(new RuntimeException("AI API 클라이언트 오류: " + response.statusCode()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        return Mono.error(new RuntimeException("AI API 서버 오류: " + response.statusCode()));
                    })
                    .bodyToMono(AiApiResponse.class)
                    .timeout(Duration.ofMinutes(3))
                    .block();

            if (aiResponse != null && aiResponse.getResponse() != null && !aiResponse.getResponse().isEmpty()) {
                analysisResultList = aiResponse.getResponse(); // List<String> 직접 사용
                analysisResultString = aiResponse.getAnalysis(); // DB 저장용은 String으로 변환
            } else {
                analysisResultList = Arrays.asList("분석 결과를 받을 수 없습니다.");
                analysisResultString = "분석 결과를 받을 수 없습니다.";
            }

        } catch (Exception e) {
            analysisResultList = Arrays.asList("AI 분석 서비스 일시 중단 - 분석결과 temp");
            analysisResultString = "AI 분석 서비스 일시 중단 - 분석결과 temp";
            System.err.println("AI API 호출 실패: " + e.getMessage());
            e.printStackTrace();
        }

        // ChatMessage 생성 및 저장 (기존과 동일)
        ChatMessage message = ChatMessage.builder()
                .message(resultMessage)
                .chatRoom(chatRoom)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        if (savedMessage.getId() == null) {
            throw new RuntimeException("ChatMessage 저장 실패: ID가 생성되지 않았습니다.");
        }

        // ChatAnalysis 저장 (String 형태로 저장)
        ChatAnalysis chatAnalysis = ChatAnalysis.builder()
                .analysis(analysisResultString) // String으로 저장
                .chatMessage(savedMessage)
                .build();

        chatAnalysisRepository.save(chatAnalysis);

        return analysisResultList; // List<String> 반환
    }









    // 4. 비동기 버전 (선택사항)
    @Transactional
    public CompletableFuture<String> messageAnalysisAsync(ChatRoomRequestDto.messageAnalysisDto request) {

        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));



        List<ChatRoomRequestDto.messageRequestDto> messages = request.getMessage();


        List<List<String>> chatMessages = messages.stream()
                .map(m -> Arrays.asList(m.getSender(), m.getText()))
                .collect(Collectors.toList());

        String resultMessage = messages.stream()
                .map(m -> m.getSender() + ":" + m.getText())
                .collect(Collectors.joining(", "));

        // ai 앤드포인트를 기준으로 분석 결과 가져오기 아직 미구현
        AiRequestDto.messageAnalysisRequestDto aiRequestDto = AiRequestDto.messageAnalysisRequestDto.builder()
                .chatTitle(chatRoom.getTitle())
                .chatContent(chatRoom.getContent())
                .price(chatRoom.getPrice())
                .status(chatRoom.getStatus())
                .chat(chatMessages)
                .build();

        // 비동기 AI API 호출
        return webClient.post()
                .uri(AI_RECOMMEND_API)
                .header("Content-Type", "application/json")
                .bodyValue(aiRequestDto)
                .retrieve()
                .bodyToMono(AiApiResponse.class)
                .timeout(Duration.ofMinutes(2))
                .map(response -> response.getAnalysis())
                .onErrorReturn("AI 분석 서비스 오류")
                .toFuture()
                .thenApply(analysisResult -> {
                    // 데이터 저장 로직
                    ChatMessage message = ChatMessage.builder()
                            .message(resultMessage)
                            .build();

                    chatRoom.addChatMessage(message);

                    ChatAnalysis chatAnalysis = ChatAnalysis.builder()
                            .analysis(analysisResult)
                            .build();
                    message.addChatAnalysis(chatAnalysis);

                    chatRoomRepository.save(chatRoom);

                    return analysisResult;
                });
    }




    // post /recommend
    public String recommendQuestion(Long chatRoomId){

        // ChatRoom 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new TempHandler(ErrorStatus.CHATROOM_NOT_FOUND));

        AiRequestDto.recommendRequestDto aiRequestDto = AiRequestDto.recommendRequestDto.builder()
                .chatTitle(chatRoom.getTitle())
                .chatContent(chatRoom.getContent())
                .price(chatRoom.getPrice())
                .status(chatRoom.getStatus())
//                .deliveryFee(chatRoom.getDeliveryFee())
                .build();



        String analysisResult;
        try {
            AiApiResponse aiResponse = webClient.post()
                    .uri(AI_RECOMMEND_API)
                    .header("Content-Type", "application/json")
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        return Mono.error(new RuntimeException("AI API 클라이언트 오류: " + response.statusCode()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        return Mono.error(new RuntimeException("AI API 서버 오류: " + response.statusCode()));
                    })
                    .bodyToMono(AiApiResponse.class)
                    .timeout(Duration.ofSeconds(15)) // 30초 타임아웃
                    .block(); // 동기 호출

            analysisResult = aiResponse != null ? aiResponse.getAnalysis() : "분석 결과를 받을 수 없습니다.";

        } catch (Exception e) {
            // AI API 호출 실패 시 기본값 사용
            analysisResult = "AI 분석 서비스 일시 중단 - 분석결과 temp";
            // 로깅
            System.err.println("AI API 호출 실패: " + e.getMessage());
        }


        return analysisResult;
    }




}
