---
name: test-guide
description: FunnyRun 프로젝트의 테스트 코드 작성 가이드. JUnit 5, AssertJ, Spring Boot Test 기반의 테스트 패턴, Helper 클래스 작성법, 검증 방법을 제공. "테스트 작성", "테스트 코드", "단위 테스트", "통합 테스트", "Helper 클래스", "AssertJ" 관련 요청 시 사용.
---

# FunnyRun 테스트 작성 가이드

## 개요

FunnyRun 프로젝트의 테스트 코드 작성을 위한 가이드입니다. 프로젝트에서 사용하는 테스트 프레임워크, 컨벤션, 패턴을 제공하여 일관된 테스트 코드를 작성할 수 있도록 돕습니다.

## 핵심 컨벤션

### 테스트 프레임워크
- **JUnit 5**: 테스트 실행 프레임워크
- **AssertJ**: 유창한 API 스타일의 검증 라이브러리
- **Spring Boot Test**: 스프링 통합 테스트

### 기본 구조
```java
@ActiveProfiles("test")
@SpringBootTest
public class SampleServiceTest {

    @Autowired
    private SampleService sampleService;

    @BeforeEach
    void setInit(){
        // 테스트 데이터 초기화
    }

    @Test
    void 테스트_메서드명_한글로_작성(){
        //given - 테스트 데이터 준비

        //when - 실제 테스트 수행

        //then - 결과 검증
    }
}
```

### 네이밍 규칙
- **테스트 클래스**: `{대상클래스명}Test`
- **테스트 메서드**: 한글로 작성 (예: `케이던스_순위_계산_알고리즘_테스트`)
- **Helper 클래스**: `{Domain}Helper` (예: `GameHelper`, `UserHelper`)

### Given-When-Then 패턴
모든 테스트는 BDD 스타일의 Given-When-Then 패턴을 따릅니다:
- **Given**: 테스트 데이터 준비 (Helper 클래스 활용)
- **When**: 실제 테스트할 동작 실행
- **Then**: AssertJ로 결과 검증

## 상세 문서

필요한 내용을 선택하여 확인하세요:

### 1. 테스트 작성 패턴 → `references/test-patterns.md`
프로젝트의 전반적인 테스트 작성 패턴과 컨벤션:
- 테스트 프레임워크 설정
- 테스트 클래스 및 메서드 구조
- Given-When-Then 패턴 상세
- 통합 테스트 vs 단위 테스트
- 데이터 초기화 방법

**읽어야 할 때**:
- 새로운 테스트 클래스를 작성할 때
- 프로젝트의 테스트 컨벤션을 확인하고 싶을 때
- 테스트 구조와 패턴을 이해하고 싶을 때

### 2. Helper 클래스 가이드 → `references/helper-guide.md`
테스트 데이터 생성을 위한 Helper 클래스 작성법:
- Helper 클래스 구조와 위치
- 작성 원칙 (static 메서드, Builder 패턴)
- 기본값 설정 전략
- GameHelper, UserHelper 예시
- 다양한 시나리오를 위한 변형 메서드

**읽어야 할 때**:
- 새로운 Helper 클래스를 만들 때
- 기존 Helper에 메서드를 추가할 때
- 테스트 데이터 생성 방법을 개선하고 싶을 때

### 3. AssertJ 검증 가이드 → `references/assertion-guide.md`
AssertJ를 사용한 효과적인 검증 방법:
- 기본 검증 패턴
- 숫자, 문자열, 부울, 컬렉션 검증
- 사용자 정의 메시지 추가
- Optional, 예외 검증
- 실전 예시와 베스트 프랙티스

**읽어야 할 때**:
- 특정 타입의 검증 방법을 찾을 때
- 검증 코드의 가독성을 높이고 싶을 때
- 복잡한 검증 로직을 작성할 때

## 빠른 시작

### 서비스 통합 테스트 작성

```java
import static hyper.run.helper.GameHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setInit(){
        gameRepository.deleteAll();
    }

    @Test
    void 게임_생성_테스트(){
        //given
        GameCreateRequest request = new GameCreateRequest(
            "테스트 경기", GameType.SPEED, GameDistance.FIVE_KM_COURSE
        );

        //when
        Game game = gameService.createGame(request);

        //then
        assertThat(game.getId()).isNotNull();
        assertThat(game.getName()).isEqualTo("테스트 경기");
        assertThat(game.getType()).isEqualTo(GameType.SPEED);
    }
}
```

### Helper 클래스 활용

```java
import static hyper.run.helper.GameHelper.*;
import static hyper.run.helper.UserHelper.generateUser;

@Test
void 경기_참가_테스트(){
    //given
    Game game = gameRepository.save(
        generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE)
    );
    User user = userRepository.save(generateUser(1L));

    //when
    gameService.joinGame(game.getId(), user.getId());

    //then
    GameHistory history = gameHistoryRepository.findByGameIdAndUserId(
        game.getId(), user.getId()
    ).orElseThrow();

    assertThat(history.getGameId()).isEqualTo(game.getId());
    assertThat(history.getUserId()).isEqualTo(user.getId());
}
```

## 사용 방법

상세 정보가 필요하면 위 파일 경로를 Read tool로 읽어서 확인하세요.

예시:
```
테스트 작성 패턴이 궁금해
→ .claude/skills/test-guide/references/test-patterns.md 읽기

Helper 클래스 만드는 법이 궁금해
→ .claude/skills/test-guide/references/helper-guide.md 읽기

AssertJ 검증 방법이 궁금해
→ .claude/skills/test-guide/references/assertion-guide.md 읽기
```
