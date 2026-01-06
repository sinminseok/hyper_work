# Helper 클래스 작성 가이드

## 개요

Helper 클래스는 테스트 데이터를 일관되게 생성하기 위한 유틸리티 클래스입니다. 테스트 코드의 가독성을 높이고, 중복을 제거하며, 테스트 데이터 생성 로직을 중앙화합니다.

## Helper 클래스 구조

### 위치
```
run_core/src/test/java/hyper/run/helper/
├── GameHelper.java
├── UserHelper.java
└── {Domain}Helper.java  // 필요에 따라 추가
```

### 기본 구조

```java
package hyper.run.helper;

import hyper.run.domain.{domain}.entity.{Entity};
import hyper.run.domain.{domain}.dto.request.{Request};

public class {Domain}Helper {

    public static {Entity} generate{Entity}({필요한_파라미터}) {
        return {Entity}.builder()
                .field1(value1)
                .field2(value2)
                // ...
                .build();
    }

    // 다양한 변형 메서드 추가 가능
}
```

## 작성 원칙

### 1. Static 메서드 사용
모든 Helper 메서드는 static으로 선언하여 인스턴스 생성 없이 사용 가능하게 합니다.

```java
public static Game generateGame(GameType gameType, GameDistance gameDistance) {
    // ...
}
```

### 2. Builder 패턴 활용
엔티티가 Builder 패턴을 지원한다면 반드시 활용합니다.

```java
return Game.builder()
        .name("테스트용경기")
        .type(gameType)
        .distance(gameDistance)
        .gameDate(LocalDate.now())
        // ...
        .build();
```

### 3. 합리적인 기본값 설정
테스트에서 중요하지 않은 필드는 합리적인 기본값을 설정합니다.

```java
public static User generateUser(Long id) {
    return User.builder()
            .name("사용자" + id)              // ID 기반 유니크한 이름
            .email("testEmail" + id)         // ID 기반 유니크한 이메일
            .password("password")            // 고정된 기본 비밀번호
            .phoneNumber("0101234123" + id)  // ID 기반 유니크한 번호
            .birth("001031")                 // 고정된 생일
            .loginType(LoginType.EMAIL)      // 기본 로그인 타입
            .coupon(100)                     // 기본 쿠폰 수
            .point(0)                        // 기본 포인트
            .build();
}
```

### 4. 필요한 파라미터만 받기
테스트마다 달라져야 하는 값만 파라미터로 받고, 나머지는 기본값 사용:

```java
// Good: 필수 파라미터만 받음
public static Game generateGame(GameType gameType, GameDistance gameDistance) {
    return Game.builder()
            .name("테스트용경기")  // 고정값
            .type(gameType)       // 파라미터
            .distance(gameDistance)  // 파라미터
            .gameDate(LocalDate.now())  // 고정값
            .participatedCount(10)      // 고정값
            // ...
            .build();
}

// Bad: 모든 필드를 파라미터로 받음 (불필요하게 복잡)
public static Game generateGame(String name, GameType type, GameDistance distance,
                                LocalDate gameDate, LocalDateTime startAt, ...) {
    // 너무 많은 파라미터
}
```

## GameHelper 예시

```java
package hyper.run.helper;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GameHelper {

    /**
     * 기본 게임 생성
     */
    public static Game generateGame(GameType gameType, GameDistance gameDistance) {
        return Game.builder()
                .name("테스트용경기")
                .type(gameType)
                .distance(gameDistance)
                .gameDate(LocalDate.now())
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(5))
                .participatedCount(10)
                .totalPrize(1000)
                .firstPlacePrize(800)
                .secondPlacePrize(150)
                .thirdPlacePrize(50)
                .build();
    }

    /**
     * 케이던스 게임 히스토리 생성
     */
    public static GameHistory generateCadenceGameHistory(
            String id, Long gameId, Long userId,
            int targetCadence, boolean isDone, int rank) {
        return GameHistory.builder()
                .id(id)
                .gameId(gameId)
                .userId(userId)
                .gameDistance(GameDistance.FIVE_KM_COURSE)
                .rank(rank)
                .prize(0)
                .targetCadence(targetCadence)
                .done(isDone)
                .build();
    }

    /**
     * 케이던스 업데이트 요청 DTO 생성
     */
    public static GameHistoryUpdateRequest generateCadenceGameHistoryUpdateRequest(
            Long gameId, Long userId, double currentCadence) {
        return GameHistoryUpdateRequest.builder()
                .gameId(gameId)
                .userId(userId)
                .currentCadence(currentCadence)
                .build();
    }
}
```

## UserHelper 예시

```java
package hyper.run.helper;

import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.User;

public class UserHelper {

    /**
     * 기본 사용자 생성
     * @param id 사용자 ID (유니크한 값 생성에 사용)
     */
    public static User generateUser(Long id) {
        return User.builder()
                .name("사용자" + id)
                .email("testEmail" + id)
                .password("password")
                .phoneNumber("0101234123" + id)
                .birth("001031")
                .loginType(LoginType.EMAIL)
                .coupon(100)
                .point(0)
                .build();
    }
}
```

## 테스트에서 사용

### Static Import 활용

```java
import static hyper.run.helper.GameHelper.*;
import static hyper.run.helper.UserHelper.generateUser;

@Test
void 게임_결과_저장_테스트(){
    //given
    Game game = gameRepository.save(
        generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE)
    );

    User user = userRepository.save(generateUser(1L));

    GameHistory history = generateCadenceGameHistory(
        "1", game.getId(), user.getId(), 120, false, 0
    );

    //when & then
    // ...
}
```

## 다양한 시나리오를 위한 변형 메서드

하나의 엔티티에 대해 여러 시나리오가 필요한 경우 변형 메서드를 추가:

```java
public class GameHelper {

    // 기본 게임
    public static Game generateGame(GameType gameType, GameDistance gameDistance) {
        // ...
    }

    // 이미 시작된 게임
    public static Game generateStartedGame(GameType gameType, GameDistance gameDistance) {
        return Game.builder()
                .name("진행중인경기")
                .type(gameType)
                .distance(gameDistance)
                .gameDate(LocalDate.now().minusDays(1))
                .startAt(LocalDateTime.now().minusHours(2))
                .endAt(LocalDateTime.now().plusHours(3))
                // ...
                .build();
    }

    // 종료된 게임
    public static Game generateFinishedGame(GameType gameType, GameDistance gameDistance) {
        return Game.builder()
                .name("종료된경기")
                .type(gameType)
                .distance(gameDistance)
                .gameDate(LocalDate.now().minusDays(1))
                .startAt(LocalDateTime.now().minusHours(10))
                .endAt(LocalDateTime.now().minusHours(1))
                // ...
                .build();
    }
}
```

## 새 Helper 추가 시 체크리스트

- [ ] `src/test/java/hyper/run/helper/` 위치에 생성
- [ ] 클래스명: `{Domain}Helper`
- [ ] 모든 메서드를 `public static`으로 선언
- [ ] Builder 패턴 활용
- [ ] 필수 파라미터만 받기
- [ ] 합리적인 기본값 설정
- [ ] JavaDoc 주석 추가 (선택적)
- [ ] 메서드명: `generate{Entity}` 또는 `generate{Adjective}{Entity}`
