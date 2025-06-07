FROM openjdk:17-jdk-slim

# 기본 패키지 설치
RUN apt-get update && apt-get install -y \
    apt-transport-https \
    ca-certificates \
    wget \
    gnupg2 \
    curl \
    unzip \
    fonts-liberation \
    libappindicator3-1 \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libcups2 \
    libdbus-1-3 \
    libgdk-pixbuf2.0-0 \
    libnspr4 \
    libnss3 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    xdg-utils \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Google Chrome 설치
RUN curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
        > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable && \
    ln -sf /usr/bin/google-chrome-stable /usr/bin/google-chrome

# JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Spring Profile 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app.jar"]