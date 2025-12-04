# Java 8 → Java 21 마이그레이션 가이드

> **대상 독자**: Java 8 기반 Spring 애플리케이션을 Java 21(LTS)로 올리려는 팀. VS Code + Gradle 환경을 기본으로 설명합니다.

---

## 1. 왜 지금 JDK 21인가?

- **지원 주기**: JDK 8 LTS는 2025년 3월 이후 보안 패치가 제한됩니다. JDK 21은 2031년까지 LTS 지원을 받습니다.
- **생산성**: record, sealed class, switch 패턴, 텍스트 블록 등으로 도메인 모델과 API 작성이 단순해짐.
- **성능·안정성**: G1 기본, ZGC/셴안도어 저지연 GC, Class Data Sharing 기본 활성화.
- **보안**: TLS 1.3, ChaCha20, EdDSA 등 최신 암호 스위트와 강화된 기본 정책.

---

## 2. 버전별 하이라이트 타임라인

| 버전        | 주요 기능                                                     | 주의할 점                                |
| ----------- | ------------------------------------------------------------- | ---------------------------------------- |
| 9           | 모듈 시스템(JPMS), jshell, `List.of()`                        | 내부 `sun.*` 접근 제한, `rt.jar` 제거    |
| 10          | `var` 지역 변수                                               | 코드 가독성 유지 방안 필요               |
| 11 (LTS)    | HTTP Client, Flight Recorder 오픈                             | JavaFX, JAXB 등 기본 번들 제외           |
| 12–14       | Switch 표현식, 텍스트 블록, CDS 확장                          | 일부 기능은 프리뷰로 시작                |
| 15–17 (LTS) | Records, Sealed classes, 패턴 매칭                            | Nashorn 제거, Security Manager 폐지 예고 |
| 18–21 (LTS) | UTF-8 기본, Virtual Threads(프리뷰), String Templates(프리뷰) | `--enable-preview` 필요                  |

---

## 3. 언어 기능 비교와 예시

### 3.1 Records로 DTO 단순화

```java
// Java 8 스타일
public class UserDto {
    private final String name;
    private final int age;
    // 생성자, getter, equals/hashCode, toString ...
}

// Java 21
public record UserDto(String name, int age) {}
```

- 직렬화/패턴 매칭에 유리. Lombok을 대체하거나 함께 사용할지 팀 룰 정의.

### 3.2 Switch 표현식 & 패턴 매칭

```java
int result = switch (op) {
    case "add" -> a + b;
    case "mul" -> a * b;
    default -> throw new IllegalArgumentException();
};

if (obj instanceof OrderRecord or) {
    log.info(or.id());
}
```

- `yield` 키워드로 복잡한 분기 반환 가능.
- 패턴 매칭은 단계적으로 확장 중. (JDK 21: record 패턴 + switch 패턴 프리뷰)

### 3.3 텍스트 블록과 `String` API

```java
String json = """
    {
      "status": "OK",
      "message": "hello"
    }
    """;
```

- SQL/JSON 템플릿 가독성 상승. 자동 들여쓰기 제어 `stripIndent`, `translateEscapes` 활용.

### 3.4 `var` 사용 지침

- **추천**: 제네릭 타입이 장황하거나 타입이 명확할 때.
- **지양**: 공개 API 시그니처, 복잡한 표현식 결과. 팀 코딩 규약에 포함 필요.

---

## 4. 표준 API 확장

| 영역          | 새 기능                                                           | 예시                                 |
| ------------- | ----------------------------------------------------------------- | ------------------------------------ |
| HTTP          | `java.net.http.HttpClient`                                        | 비동기 HTTP/2, WebSocket 지원        |
| Reactive      | `java.util.concurrent.Flow`                                       | Publisher-Subscriber 표준 인터페이스 |
| 동시성        | `CompletableFuture` 개선, `Executor` 업데이트                     | `orTimeout`, `completeOnTimeout`     |
| 컬렉션/스트림 | `Collectors.filtering`, `Collectors.flatMapping`, `Map.ofEntries` | 파이프라인 표현력 향상               |
| Date/Time     | `Instant#toEpochMilli`, `Duration#toDaysPart` 등                  | 세부 단위 추출 API                   |
| NIO           | `Files.mismatch`, `Files.readString`                              | 테스트 및 유틸 작업 단순화           |

---

## 5. 런타임 & 성능

- **GC 변화**: G1 기본(9), ZGC(11+)·Shenandoah(12+) 저지연 옵션. `-XX:+UseConcMarkSweepGC`는 제거됨.
- **클래스 데이터 공유(CDS)**: 12부터 기본 on. 기동 속도, 메모리 절약.
- **가상 스레드(프로젝트 Loom, JDK 21 프리뷰)**
  ```java
  try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      IntStream.range(0, 1_000_000).forEach(i -> executor.submit(() -> handleRequest(i)));
  }
  ```
  - IO 중심 서비스에서 스레드 관리가 단순화. 프리뷰라 `--enable-preview` 필요.

---

## 6. 보안/암호

- TLS 1.3 기본 사용, RC4 등 취약 알고리즘 제거. 서버/클라이언트가 하위 버전만 지원하면 핸드셰이크 실패.
- ChaCha20-Poly1305, Ed25519/Ed448 서명 지원.
- 강화된 키/인증서 검증: 만료·취소 체크 강화. 사설 CA 사용 시 체인 구성 확인.
- Security Manager가 17부터 deprecated → 대체 권한 모델/감사 조치 필요.

---

## 7. 도구/빌드 체인

- **Gradle**: 8.x 이상에서 JDK 21 정식 지원. `gradle.properties`에 `org.gradle.java.home` 설정 가능.
- **Maven**: 3.9+ 권장, `maven-toolchains-plugin`으로 JDK 선택.
- **VS Code**: Java Extension Pack 업데이트, `settings.json`에 `"java.configuration.runtimes"`로 JDK 21 등록.
- **jshell**: 빠른 실험 가능. `jshell --enable-preview`로 최신 문법 테스트.
- **jlink/jpackage**: 모듈식 런타임 이미지 생성. Docker 이미지 슬림화에 유리.

---

## 8. 제거·호환성 이슈

| 항목                                     | 조치                                                                   |
| ---------------------------------------- | ---------------------------------------------------------------------- |
| `sun.misc.Unsafe`, `com.sun.*` 직접 사용 | 공식 API 대체 또는 `--add-opens` 임시 사용 후 코드 수정                |
| JAXB, JAX-WS, CORBA                      | Maven/Gradle 의존성 명시 (예: `jakarta.xml.bind:jakarta.xml.bind-api`) |
| Nashorn JavaScript 엔진                  | GraalVM JS, Rhino 등 대체 도입                                         |
| Pack200                                  | 배포 파이프라인에서 제거                                               |
| Security Manager                         | 모듈/정책 기반 감사 체계 구축                                          |

---

## 9. 마이그레이션 체크리스트

1. **환경 준비**
   - CI · 로컬 모두 JDK 21 설치.
   - Gradle Wrapper 버전 업 (`./gradlew wrapper --gradle-version 8.10`).
2. **코드베이스 점검**
   - `sourceCompatibility`, `targetCompatibility` 21.
   - `--release 21` 컴파일 설정.
   - 내부 API, `Illegal reflective access` 경고 위치 파악.
3. **의존성 업데이트**
   - Spring Boot 3.x 이상(= Jakarta EE 9 네임스페이스) 필요.
   - JDBC 드라이버, 로깅, 보안 라이브러리 최신화.
4. **테스트 전략**
   - 유닛/통합 테스트를 `--add-opens` 없이 실행해 반사 접근 문제 제거.
   - TLS·JDBC 연결, 직렬화 포맷 호환성 검증.
5. **성능/운영**
   - 기존 GC 플래그 재검토, JDK 21 기본값으로 먼저 테스트.
   - Flight Recorder + Mission Control로 병목 분석.
6. **배포**
   - Docker 베이스 이미지 교체(e.g., `eclipse-temurin:21-jre`).
   - Canary → Blue/Green 순으로 점진 배포.

---

## 10. 흔히 헷갈리는 부분 FAQ

| 질문                                | 정리                                                                                                |
| ----------------------------------- | --------------------------------------------------------------------------------------------------- |
| _Jakarta 네임스페이스?_             | Spring Boot 3.x부터 `javax.*` 대신 `jakarta.*`. Servlet/JPA import 변경 필요.                       |
| _모듈 시스템 꼭 써야 하나?_         | 아니요. Classpath 모드로도 동작하지만 향후 모듈화를 준비하면 장점(패키지 캡슐화, 작은 런타임) 있음. |
| _`--add-opens` 항상 붙여도 되나요?_ | 임시 해결책일 뿐. 장기적으로는 공개 API로 전환해야 함.                                              |
| _Virtual Thread 바로 써도 될까?_    | 프리뷰 기능이라 운영 투입 시 리스크 평가 필요. 테스트/내부 도구부터 적용 권장.                      |
| _Nashorn 대체?_                     | Spring 환경이면 GraalVM JavaScript 또는 외부 JS 엔진을 라이브러리로 추가.                           |

---

## 11. 코드 예시 모음

### 11.1 Spring Security `@Configuration` 예

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
```

- Spring Boot 3.x/JDK 21에 맞춰 `SecurityFilterChain` Bean 방식 사용.

### 11.2 HTTP Client 교체 예

```java
HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.example.com/user"))
        .GET().build();
HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
```

- `java.net.HttpURLConnection` 대체 가능. 비동기는 `client.sendAsync` 사용.

### 11.3 패턴 매칭을 활용한 DTO 처리

```java
sealed interface Event permits UserCreated, OrderPlaced {}
record UserCreated(String name) implements Event {}
record OrderPlaced(long orderId) implements Event {}

String describe(Event event) {
    return switch (event) {
        case UserCreated uc -> "USER:" + uc.name();
        case OrderPlaced op -> "ORDER:" + op.orderId();
    };
}
```

- sealed hierarchy로 switch exhaustiveness 보장.

---

## 12. VS Code 워크플로

1. **JDK 설정**: `Ctrl+Shift+P → Java: Configure Java Runtime → Add JDK 21`. `settings.json` 예:
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-21",
         "path": "C:/jdk-21",
         "default": true
       }
     ]
   }
   ```
2. **Gradle 동기화**: `./gradlew.bat --stop` 후 `./gradlew.bat build` 실행, Java Language Server 재시작.
3. **DevTools Hot Reload**: `spring-boot-devtools` 추가 후 저장 시 자동 재시작 확인.
4. **미리보기 기능 테스트**: `tasks.json` 또는 `launch.json`에 `"vmArgs": "--enable-preview"` 추가.
5. **문제 해결**: Java Problems 뷰에서 `Illegal reflective access` 경고 추적, 빠르게 리팩토링.

---

## 13. 권장 마이그레이션 플랜

1. **준비 단계**: 브랜치 `feature/java21-base` 생성, 빌드 스크립트/Gradle Wrapper 업데이트.
2. **컴파일 단계**: Java 21로 전체 컴파일. `--add-opens` 없이 실패하는 위치 파악.
3. **테스트 단계**: 단위·통합 테스트, TLS/JDBC 연결, 직렬화 호환성 검증.
4. **성능 검증**: Flight Recorder + Mission Control, GC 로그 비교. 필요 시 ZGC 실험.
5. **배포 단계**: Docker 이미지 교체, Canary → 전체 배포. 모니터링 임계치 재설정.
6. **회고/문서화**: 남은 기술 부채, 신규 언어 기능 도입 전략 정리.

---

## 14. 참고 링크

- [OpenJDK Release Notes](https://wiki.openjdk.org) (버전별 변경사항)
- [Spring Boot 3.2 Migration Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#migration)
- [Gradle Java Toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
- [Project Loom](https://openjdk.org/projects/loom/)
- [JDK Flight Recorder & Mission Control](https://openjdk.org/projects/jmc/)

---

이 문서는 `docs/java8-to-21-migration.md`에 저장되었습니다. 필요 시 팀 환경에 맞춰 예시 경로, 커맨드, 정책을 추가해 확장하세요.
