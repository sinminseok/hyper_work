# AssertJ 사용 가이드

## 개요

AssertJ는 유창한(fluent) API를 제공하는 Java 테스트 검증 라이브러리입니다. JUnit의 기본 assert보다 가독성이 높고 풍부한 검증 메서드를 제공합니다.

## 기본 Import

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;  // 필요 시
```

## 기본 검증 패턴

### 기본 구조

```java
assertThat(실제값).검증메서드(기대값);
```

## 일반적인 검증

### 동등성 검증

```java
// 값이 같은지 검증
assertThat(gameHistory.getRank()).isEqualTo(1);
assertThat(user.getName()).isEqualTo("사용자1");

// 값이 다른지 검증
assertThat(gameHistory.getRank()).isNotEqualTo(0);

// null 검증
assertThat(game).isNotNull();
assertThat(deletedGame).isNull();
```

### 숫자 검증

```java
// 크기 비교
assertThat(gameHistory.getRank()).isGreaterThan(5);
assertThat(gameHistory.getRank()).isGreaterThanOrEqualTo(5);
assertThat(gameHistory.getRank()).isLessThan(10);
assertThat(gameHistory.getRank()).isLessThanOrEqualTo(10);

// 범위 검증
assertThat(gameHistory.getRank()).isBetween(1, 10);

// 양수/음수/0 검증
assertThat(user.getPoint()).isPositive();
assertThat(user.getPoint()).isNegative();
assertThat(user.getPoint()).isZero();
```

### 부울 검증

```java
// boolean 필드 검증
assertThat(gameHistory.isDone()).isTrue();
assertThat(gameHistory.isDone()).isFalse();
```

### 문자열 검증

```java
// 정확한 일치
assertThat(game.getName()).isEqualTo("테스트용경기");

// 포함 여부
assertThat(game.getName()).contains("테스트");
assertThat(game.getName()).doesNotContain("실제");

// 시작/종료
assertThat(game.getName()).startsWith("테스트");
assertThat(game.getName()).endsWith("경기");

// 비어있는지
assertThat(game.getName()).isNotEmpty();
assertThat(game.getName()).isNotBlank();

// 대소문자 무시 비교
assertThat(game.getName()).isEqualToIgnoringCase("테스트용경기");
```

## 컬렉션 검증

### 리스트/배열 검증

```java
List<GameHistory> histories = gameHistoryRepository.findAllByGameId(gameId);

// 크기 검증
assertThat(histories).hasSize(3);
assertThat(histories).isEmpty();
assertThat(histories).isNotEmpty();

// 포함 여부
assertThat(histories).contains(history1);
assertThat(histories).containsExactly(history1, history2, history3);  // 순서 중요
assertThat(histories).containsExactlyInAnyOrder(history3, history1, history2);  // 순서 무관

// 특정 조건 검증
assertThat(histories).allMatch(h -> h.isDone());
assertThat(histories).anyMatch(h -> h.getRank() == 1);
assertThat(histories).noneMatch(h -> h.getRank() < 0);
```

### 추출 및 검증

```java
// 필드 추출하여 검증
assertThat(histories)
    .extracting(GameHistory::getRank)
    .containsExactly(1, 2, 3);

assertThat(histories)
    .extracting("userId", "rank")
    .containsExactly(
        tuple(1L, 1),
        tuple(2L, 2),
        tuple(3L, 3)
    );
```

## 사용자 정의 메시지

검증 실패 시 표시될 메시지를 커스터마이징할 수 있습니다:

```java
assertThat(gameHistory.getRank())
    .as("userId %d 의 예상 등수는 %d", userId, expectedRank)
    .isEqualTo(expectedRank);
```

실제 프로젝트 예시:
```java
Map<Long, Integer> expected = Map.of(0L, 1, 1L, 2, 2L, 3);

for (GameHistory history : allByGameId) {
    Long userId = history.getUserId();
    int expectedRank = expected.get(userId);
    assertThat(history.getRank())
            .as("userId %d 의 예상 등수는 %d", userId, expectedRank)
            .isEqualTo(expectedRank);
}
```

## 복잡한 검증

### 여러 필드 동시 검증

```java
assertThat(game)
    .extracting("name", "type", "distance")
    .containsExactly("테스트용경기", GameType.CADENCE, GameDistance.FIVE_KM_COURSE);
```

### Optional 검증

```java
Optional<GameHistory> gameHistory = gameHistoryRepository.findById("1");

// Optional이 값을 가지는지
assertThat(gameHistory).isPresent();
assertThat(gameHistory).isEmpty();

// Optional의 값 검증
assertThat(gameHistory.get().getRank()).isGreaterThan(5);

// 또는
assertThat(gameHistory)
    .isPresent()
    .hasValueSatisfying(gh -> {
        assertThat(gh.getRank()).isGreaterThan(5);
        assertThat(gh.isDone()).isTrue();
    });
```

### 예외 검증

```java
// 예외 발생 검증
assertThatThrownBy(() -> {
    gameService.joinGame(invalidGameId, userId);
})
    .isInstanceOf(GameNotFoundException.class)
    .hasMessageContaining("게임을 찾을 수 없습니다");

// 예외가 발생하지 않는지 검증
assertThatNoException().isThrownBy(() -> {
    gameService.joinGame(gameId, userId);
});
```

## JUnit Assertions와 함께 사용

경우에 따라 JUnit의 기본 assertion도 사용할 수 있습니다:

```java
import static org.junit.jupiter.api.Assertions.assertTrue;

// 복잡한 조건 검증
boolean isValid = (gameId >= 4 && gameId <= 6 && rank >= 1 && rank <= 3) ||
                  (gameId >= 1 && gameId <= 3 && rank >= 4 && rank <= 6);
assertTrue(isValid, "조건 불일치 - gameId: " + gameId + ", rank: " + rank);
```

하지만 가능하면 AssertJ 사용을 권장:
```java
// AssertJ 방식 (권장)
assertThat(isValid)
    .as("조건 불일치 - gameId: %d, rank: %d", gameId, rank)
    .isTrue();
```

## 실전 예시

### 순위 계산 검증

```java
@Test
void 케이던스_순위_계산_알고리즘_테스트() {
    //given
    Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
    for (int i = 0; i < 3; i++) {
        gameHistoryRepository.save(
            generateCadenceGameHistory(String.valueOf(i + 1), game.getId(), (long) i, 120, false, 0)
        );
    }

    //when
    gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 0L, 120)); // 1등
    gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 1L, 130)); // 2등
    gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 2L, 150)); // 3등
    cadenceRankService.calculateRank(game);

    //then
    List<GameHistory> allByGameId = gameHistoryRepository.findAllByGameId(game.getId());

    // 검증: userId 0L → rank 1, 1L → 2, 2L → 3
    Map<Long, Integer> expected = Map.of(0L, 1, 1L, 2, 2L, 3);

    for (GameHistory history : allByGameId) {
        Long userId = history.getUserId();
        int expectedRank = expected.get(userId);
        assertThat(history.getRank())
                .as("userId %d 의 예상 등수는 %d", userId, expectedRank)
                .isEqualTo(expectedRank);
    }
}
```

### 엔티티 계산 로직 검증

```java
@Test
void 케이던스_계산_테스트(){
    //given
    GameHistory gameHistory = gameHistoryRepository.save(
        generateCadenceGameHistory("1", 1L, 1L, 150, false, 0)
    );

    //when
    gameHistory.updateFrom(generateCadenceGameHistoryUpdateRequest(1L, 1L, 150));
    gameHistory.updateFrom(generateCadenceGameHistoryUpdateRequest(1L, 1L, 300));
    gameHistory.updateFrom(generateCadenceGameHistoryUpdateRequest(1L, 1L, 900));

    //then
    assertThat(gameHistory.getCurrentCadence()).isEqualTo(450.0);
}
```

## 베스트 프랙티스

1. **AssertJ를 기본으로 사용**: 가독성과 풍부한 메서드 제공
2. **의미있는 메시지 추가**: `.as()` 메서드로 실패 시 디버깅 정보 제공
3. **체이닝 활용**: 여러 검증을 연결하여 가독성 향상
4. **적절한 검증 메서드 선택**: `isEqualTo()` vs `isSameAs()` 등 차이 이해

## 참고 자료

- AssertJ 공식 문서: https://assertj.github.io/doc/
- AssertJ Core assertions: https://www.javadoc.io/doc/org.assertj/assertj-core/latest/index.html
