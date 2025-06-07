# OpenJDK 17 슬림 버전 기반 이미지
FROM openjdk:17-jdk-slim

# Docker 빌드 시 JAR_FILE 변수를 받아서 해당 JAR을 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 환경 변수를 통해 실행 환경(Spring Profile) 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 컨테이너 실행 시 Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app.jar"]