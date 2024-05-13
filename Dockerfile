# 빌드 스테이지
FROM openjdk:17-slim AS builder
WORKDIR /app

# 소스 코드 및 Gradle 래퍼 파일 복사
COPY . .

# Gradle을 사용하여 애플리케이션 빌드
RUN ./gradlew build

# 런타임 스테이지
FROM openjdk:17-slim
WORKDIR /app

# 빌드 단계에서 생성된 실행 가능한 JAR 파일을 런타임 이미지로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# keystore.p12 파일을 이미지에 포함
COPY --from=builder /app/src/main/resources/keystore.p12 keystore.p12

# 컨테이너가 시작될 때 실행될 명령어, SSL/TLS를 위한 추가 JVM 옵션을 포함
ENTRYPOINT ["java", "-Djavax.net.ssl.keyStore=keystore.p12", "-Djavax.net.ssl.keyStorePassword=danco", "-Djavax.net.ssl.keyStoreType=PKCS12", "-jar", "app.jar"]

# 컨테이너에서 노출할 포트 지정, 443 포트 추가
EXPOSE 8080 443

