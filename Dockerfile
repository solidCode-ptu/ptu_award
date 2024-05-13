# 베이스 이미지로 OpenJDK 17 JDK 포함된 이미지 사용
FROM openjdk:17-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 및 Gradle 래퍼 파일 복사
COPY . .

# Gradle을 사용하여 애플리케이션 빌드
RUN ./gradlew build

# 실행 이미지 단계
FROM openjdk:17-slim

# 애플리케이션 파일을 이미지 내부로 복사하기 위한 디렉토리 생성
WORKDIR /app

# 빌드 단계에서 생성된 실행 가능한 JAR 파일을 런타임 이미지로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너가 시작될 때 실행될 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

# 컨테이너에서 노출할 포트 지정
EXPOSE 8080

