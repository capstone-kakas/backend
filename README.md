# backend
CAU capstone05 kakas

# 🛠️ Capstone Backend API Server

Spring Boot 기반으로 구축된 백엔드 서버입니다.  
Chrome Extension, AI 분석 서버, 크롤링 DB와 통신하며 사용자의 거래 채팅을 분석하고 응답합니다.

---

## 📁 프로젝트 구조

```
com.capstone.kakas
├── apiPayload                 # 공통 API 응답 및 예외 핸들링 클래스
│
├── crawlingdb                # 크롤링 전용 DB 도메인 (상품 시세, 중고가 등)
│   ├── controller            # 크롤링 API 엔드포인트
│   ├── converter             # 데이터 변환기 (필요 시 Entity ↔ DTO 등)
│   ├── domain                # 크롤링용 Entity 클래스 (SalePrice, UsedPrice 등)
│   ├── dto                   # 크롤링 관련 데이터 전송 객체
│   ├── repository            # 크롤링 DB JPA 인터페이스
│   └── service               # 크롤링 관련 비즈니스 로직 처리
│
├── devdb                     # 사용자, 채팅방 등 핵심 도메인 DB
│   ├── controller            # 사용자/채팅방 API 엔드포인트
│   ├── domain                # 핵심 Entity 클래스 (ChatRoom, Member 등)
│   ├── dto                   # 사용자 및 채팅 관련 DTO 클래스
│   ├── repository            # dev DB용 Repository
│   └── service               # 핵심 서비스 로직 처리
│
├── global                    # 공통 설정 및 유틸
│   ├── common                # 공통 엔티티, 상속 베이스 클래스 (e.g. BaseEntity)
│   └── config                # 전체 프로젝트 설정
│       ├── datasource        # 다중 DB 설정 (dev, crawling)
│       ├── SwaggerConfig     # Swagger 문서 자동화 설정
│       ├── WebClientConfig   # 외부 서버 호출용 WebClient 설정
│       ├── WebConfig         # CORS, 인터셉터 등 웹 설정
│       └── WebDriverConfig   # Selenium WebDriver 설정 (크롤링용)
│
└── KakasApplication.java     # Spring Boot 시작점 (Main 클래스)
```

## 🧩 주요 기능 요약
	•	Chrome Extension ↔ Spring API 연동
	•	사용자의 채팅을 기반으로 AI 응답 생성
	•	채팅방 생성 및 상품 가격 조회
	•	크롤링된 가격 정보 DB 분리 저장
	•	다중 DB 연동 (devdb + crawlingdb)

## SWAGGER
https://13.125.148.205/swagger-ui/index.html#/
(미구동중)
![Image](https://github.com/user-attachments/assets/55b6acb2-bf53-4f57-8356-ca776f938c3f)
