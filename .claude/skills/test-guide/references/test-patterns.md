# FunnyRun 테스트 작성 패턴

## 테스트 프레임워크

- **JUnit 5**: 테스트 실행 프레임워크
- **AssertJ**: 유창한(fluent) API 스타일의 검증 라이브러리
- **Spring Boot Test**: 스프링 컨텍스트를 활용한 통합 테스트

## 기본 구조

### 테스트 클래스 구조

```java
@ActiveProfiles("test")
@SpringBootTest
public class SampleServiceTest {

    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleRepository sampleRepository;

    @BeforeEach
    void setInit(){
        // 각 테스트 전에 데이터 초기화
        sampleRepository.deleteAll();
    }

    @Test
    void 테스트_메서드명_한글로_작성(){
        // given - 테스트 데이터 준비

        // when - 실제 테스트 수행

        // then - 결과 검증
    }
}
```

### 필수 애노테이션

1. **@SpringBootTest**: 스프링 부트 전체 컨텍스트 로드
2. **@ActiveProfiles("test")**: 테스트 프로파일 활성화
3. **@Test**: 테스트 메서드 표시
4. **@BeforeEach**: 각 테스트 실행 전 초기화 (선택적)

## 네이밍 컨벤션

### 테스트 클래스명
- 패턴: `{테스트대상클래스명}Test`
- 예시:
  - `GameServiceTest`
  - `UserRepositoryTest`
  - `CadenceRankServiceTest`

### 테스트 메서드명
- **한글 사용**: 테스트 의도를 명확하게 표현
- 패턴: `{테스트_내용을_한글로_명확하게}`
- 예시:
  - `케이던스_순위_계산_알고리즘_테스트()`
  - `이미_완료된_사용자가_있을경우_순위_계산_테스트()`
  - `게임_결과_저장_테스트()`

## Given-When-Then 패턴

모든 테스트는 BDD(Behavior-Driven Development) 스타일의 Given-When-Then 패턴을 따릅니다.

```java
@Test
void 케이던스_순위_계산_알고리즘_테스트() {
    //given - 테스트에 필요한 데이터 준비
    Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
    for (int i = 0; i < 3; i++) {
        gameHistoryRepository.save(generateCadenceGameHistory(
            String.valueOf(i + 1), game.getId(), (long) i, 120, false, 0));
    }

    //when - 실제 테스트할 동작 실행
    gameHistoryService.updateGameHistory(
        generateCadenceGameHistoryUpdateRequest(game.getId(), 0L, 120));
    cadenceRankService.calculateRank(game);

    //then - 결과 검증
    List<GameHistory> allByGameId = gameHistoryRepository.findAllByGameId(game.getId());
    assertThat(allByGameId).hasSize(3);
    assertThat(allByGameId.get(0).getRank()).isEqualTo(1);
}
```

### 섹션별 가이드라인

**Given (준비)**
- 테스트에 필요한 데이터 생성
- Helper 클래스 활용 권장 (GameHelper, UserHelper 등)
- 데이터베이스 초기 상태 설정

**When (실행)**
- 실제 테스트할 비즈니스 로직 호출
- 하나의 명확한 동작에 집중

**Then (검증)**
- AssertJ를 사용한 결과 검증
- 예상 결과와 실제 결과 비교

## Helper 클래스 활용

테스트 데이터 생성은 Helper 클래스를 통해 중앙화합니다.

### Helper 클래스 위치
```
src/test/java/hyper/run/helper/
├── GameHelper.java
└── UserHelper.java
```

### 사용 예시

```java
import static hyper.run.helper.GameHelper.*;
import static hyper.run.helper.UserHelper.generateUser;

@Test
void 게임_결과_저장_테스트(){
    // Helper 메서드 활용
    Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
    User user = userRepository.save(generateUser(1L));
    GameHistory history = generateCadenceGameHistory("1", game.getId(), user.getId(), 120, false, 0);

    // 테스트 로직...
}
```

## 통합 테스트 vs 단위 테스트

### 통합 테스트 (현재 주로 사용)
- @SpringBootTest 사용
- 실제 데이터베이스 사용
- 여러 컴포넌트 간의 상호작용 테스트
- Service 레이어 테스트에 주로 사용

```java
@ActiveProfiles("test")
@SpringBootTest
public class CadenceRankServiceTest {
    @Autowired
    private CadenceRankService cadenceRankService;

    @Autowired
    private GameRepository gameRepository;
    // ...
}
```

### Entity 테스트
- 엔티티의 비즈니스 로직 테스트
- @SpringBootTest 사용하여 필요 시 Repository 주입

```java
@SpringBootTest
public class GameHistoryTest {
    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    @Test
    void 케이던스_계산_테스트(){
        // 엔티티 메서드 테스트
    }
}
```

## 데이터 초기화

각 테스트 간 독립성을 보장하기 위해 @BeforeEach 사용:

```java
@BeforeEach
void setInit(){
    gameHistoryRepository.deleteAll();
    // 필요한 다른 Repository도 초기화
}
```

## 테스트 프로파일 설정

`src/test/resources/application-test.yml` 또는 `application-test.properties`에 테스트용 설정 작성:
- 테스트 데이터베이스 설정
- 로깅 레벨 조정
- 외부 API Mock 설정 등
