# 스마트워치 실시간 생체 데이터 & 순위 시스템

## 아키텍처 개요
스마트워치(Apple Watch, Galaxy Watch, Garmin)에서 실시간 생체 데이터를 수집하고,
경기 참가자 간 실시간 순위를 계산하여 워치에 표시하는 시스템

---

## 📍 WebSocket 연결 정보

### 서버 설정 (현재 코드 기준)

```yaml
# application.yml
domain:
  websocket:
    game: /game        # WebSocket 엔드포인트
    publish: /pub      # 클라이언트 → 서버 메시지 prefix
    subscribe: /sub    # 서버 → 클라이언트 메시지 prefix
```

### URL 구조

| 항목 | URL | 설명 |
|------|-----|------|
| **WebSocket 연결** | `ws://localhost:8080/game` | 개발 환경 |
| **WebSocket 연결** | `wss://your-domain/game` | 운영 환경 (HTTPS) |
| **구독 Destination** | `/sub/game/my/{gameId}/{userId}` | 내 순위/상태 받기 |
| **전송 Destination** | `/pub/game/update` | 생체 데이터 전송 |

---

## 🔄 전체 흐름 (단계별)

### Step 1: 토큰 발급 (REST API)

```http
GET /v1/api/users/watch-connect-information/tokens?watchKey=xxx
```

**응답:**
```json
{
  "success": true,
  "message": "워치 연결 성공",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc..."
  }
}
```

### Step 2: WebSocket 연결

```javascript
// 1. WebSocket 연결
const socket = new SockJS('ws://localhost:8080/game');
const stompClient = Stomp.over(socket);

// 2. STOMP 연결 (인증 토큰 포함)
stompClient.connect(
    { 'Authorization': 'Bearer ' + accessToken },  // 헤더에 토큰 포함
    function(frame) {
        console.log('Connected: ' + frame);
        // → Step 3으로 이동
    }
);
```

### Step 3: 구독 (내 순위/상태 받기)

```javascript
const gameId = 123;
const userId = 456;

// 구독: 서버가 보내는 내 업데이트를 받음
stompClient.subscribe('/sub/game/my/' + gameId + '/' + userId, function(message) {
    const response = JSON.parse(message.body);

    // 받은 데이터
    console.log('현재 순위:', response.rank);
    console.log('현재 거리:', response.currentDistance);
    console.log('목표 심박수:', response.targetBpm);
    console.log('현재 심박수:', response.currentBpm);
    console.log('완주 여부:', response.isDone);

    // UI 업데이트
    updateWatchUI(response);
});
```

### Step 4: 생체 데이터 전송 (5초마다)

```javascript
// 5초마다 워치에서 수집한 생체 데이터 전송
setInterval(function() {
    // 워치 센서에서 실시간 데이터 수집
    const bpm = getHeartRate();        // 심박수
    const cadence = getCadence();      // 케이던스
    const distance = getDistance();    // 현재 이동 거리
    const speed = getSpeed();          // 현재 속도

    // 서버로 전송
    const data = {
        gameId: gameId,
        userId: userId,
        currentBpm: bpm,
        currentCadence: cadence,
        currentDistance: distance,
        currentSpeed: speed,
        currentFlightTime: 0,
        currentGroundContactTime: 0,
        currentPower: 0,
        currentVerticalOscillation: 0
    };

    stompClient.send('/pub/game/update', {}, JSON.stringify(data));

    // → 서버가 처리 후 Step 3의 구독으로 응답 전송
}, 5000);
```

### Step 5: 서버 응답 수신 (자동)

```javascript
// Step 3에서 등록한 구독 콜백이 자동으로 실행됨
// 서버 → 클라이언트 메시지 전송:
// destination: /sub/game/my/123/456
// body: { rank: 3, currentDistance: 1250.5, ... }

function updateWatchUI(response) {
    // 워치 화면 업데이트
    document.getElementById('rank').innerText = response.rank + '위';
    document.getElementById('distance').innerText = response.currentDistance + 'm';

    // 완주 확인
    if (response.isDone) {
        showFinishScreen();
    }
}
```

---

## 💻 전체 코드 예시

### Android (Wear OS) - Kotlin

```kotlin
class GameWebSocketManager(
    private val gameId: Long,
    private val userId: Long,
    private val accessToken: String
) {
    private var stompClient: StompClient? = null

    // 1. WebSocket 연결
    fun connect() {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://your-server:8080/game"
        )

        // 2. 연결 (헤더에 토큰 포함)
        stompClient?.connect(
            listOf(StompHeader("Authorization", "Bearer $accessToken"))
        )?.subscribe { isConnected ->
            if (isConnected) {
                Log.d("WebSocket", "연결 성공!")

                // 3. 구독
                subscribeToMyUpdates()

                // 4. 생체 데이터 전송 시작
                startSendingBiometricData()
            }
        }
    }

    // 3. 구독 - 내 순위/상태 받기
    private fun subscribeToMyUpdates() {
        val destination = "/sub/game/my/$gameId/$userId"

        stompClient?.topic(destination)?.subscribe { message ->
            val response = Gson().fromJson(
                message.payload,
                GameInProgressWatchResponse::class.java
            )

            // UI 업데이트
            withContext(Dispatchers.Main) {
                rankTextView.text = "${response.rank}위"
                distanceTextView.text = "${response.currentDistance}m"
                bpmTextView.text = "${response.currentBpm.toInt()} bpm"

                if (response.isDone) {
                    showFinishDialog()
                }
            }
        }
    }

    // 4. 생체 데이터 전송 (5초마다)
    private fun startSendingBiometricData() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // 워치 센서에서 데이터 수집
                val bpm = heartRateSensor.getCurrentBpm()
                val cadence = cadenceSensor.getCurrentCadence()
                val distance = distanceSensor.getCurrentDistance()
                val speed = speedSensor.getCurrentSpeed()

                sendBiometricData(bpm, cadence, distance, speed)
            }
        }, 0, 5000) // 5초마다
    }

    private fun sendBiometricData(
        bpm: Double,
        cadence: Double,
        distance: Double,
        speed: Double
    ) {
        val request = GameHistoryUpdateRequest(
            gameId = gameId,
            userId = userId,
            currentBpm = bpm,
            currentCadence = cadence,
            currentDistance = distance,
            currentSpeed = speed,
            currentFlightTime = 0.0,
            currentGroundContactTime = 0.0,
            currentPower = 0.0,
            currentVerticalOscillation = 0.0
        )

        val json = Gson().toJson(request)
        stompClient?.send("/pub/game/update", json)?.subscribe()
    }

    // 연결 해제
    fun disconnect() {
        timer?.cancel()
        stompClient?.disconnect()
    }
}
```

### iOS (watchOS) - Swift

```swift
import Starscream

class GameWebSocketManager: WebSocketDelegate {
    private var socket: WebSocket?
    private var timer: Timer?
    private let gameId: Int
    private let userId: Int
    private let accessToken: String

    init(gameId: Int, userId: Int, accessToken: String) {
        self.gameId = gameId
        self.userId = userId
        self.accessToken = accessToken
    }

    // 1. WebSocket 연결
    func connect() {
        var request = URLRequest(url: URL(string: "ws://your-server:8080/game")!)
        request.timeoutInterval = 5

        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }

    // 2. 연결 성공 시
    func didReceive(event: WebSocketEvent, client: WebSocket) {
        switch event {
        case .connected(_):
            print("WebSocket 연결 성공")

            // STOMP CONNECT (인증 토큰 포함)
            sendStompConnect()

            // 구독
            subscribeToMyUpdates()

            // 생체 데이터 전송 시작
            startSendingBiometricData()

        case .text(let text):
            handleStompMessage(text)

        case .disconnected(let reason, let code):
            print("연결 해제: \(reason), code: \(code)")

        default:
            break
        }
    }

    // STOMP CONNECT
    private func sendStompConnect() {
        let connectFrame = """
        CONNECT
        Authorization:Bearer \(accessToken)
        accept-version:1.1,1.0
        heart-beat:10000,10000

        \u{0000}
        """
        socket?.write(string: connectFrame)
    }

    // 3. 구독
    private func subscribeToMyUpdates() {
        let destination = "/sub/game/my/\(gameId)/\(userId)"
        let subscribeFrame = """
        SUBSCRIBE
        id:sub-0
        destination:\(destination)

        \u{0000}
        """
        socket?.write(string: subscribeFrame)
    }

    // 4. 생체 데이터 전송 (5초마다)
    private func startSendingBiometricData() {
        timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
            // 워치 센서에서 데이터 수집
            let bpm = self.getHeartRate()
            let cadence = self.getCadence()
            let distance = self.getDistance()
            let speed = self.getSpeed()

            self.sendBiometricData(bpm: bpm, cadence: cadence, distance: distance, speed: speed)
        }
    }

    private func sendBiometricData(bpm: Double, cadence: Double, distance: Double, speed: Double) {
        let payload: [String: Any] = [
            "gameId": gameId,
            "userId": userId,
            "currentBpm": bpm,
            "currentCadence": cadence,
            "currentDistance": distance,
            "currentSpeed": speed,
            "currentFlightTime": 0,
            "currentGroundContactTime": 0,
            "currentPower": 0,
            "currentVerticalOscillation": 0
        ]

        let jsonData = try! JSONSerialization.data(withJSONObject: payload)
        let jsonString = String(data: jsonData, encoding: .utf8)!

        let sendFrame = """
        SEND
        destination:/pub/game/update
        content-type:application/json

        \(jsonString)\u{0000}
        """
        socket?.write(string: sendFrame)
    }

    // 5. 메시지 수신 처리
    private func handleStompMessage(_ text: String) {
        if text.hasPrefix("MESSAGE") {
            let lines = text.components(separatedBy: "\n")
            if let body = lines.last?.trimmingCharacters(in: .controlCharacters) {
                if let data = body.data(using: .utf8) {
                    let response = try? JSONDecoder().decode(
                        GameInProgressWatchResponse.self,
                        from: data
                    )

                    updateUI(response)
                }
            }
        }
    }

    private func updateUI(_ response: GameInProgressWatchResponse?) {
        guard let response = response else { return }

        DispatchQueue.main.async {
            self.rankLabel.text = "\(response.rank)위"
            self.distanceLabel.text = "\(response.currentDistance)m"
            self.bpmLabel.text = "\(Int(response.currentBpm)) bpm"

            if response.isDone {
                self.showFinishScreen()
            }
        }
    }

    func disconnect() {
        timer?.invalidate()
        socket?.disconnect()
    }
}
```

---

## 📊 메시지 흐름도

```
워치 앱                                서버
  |                                    |
  |---(1) HTTP: 토큰 발급------------->|
  |<------ accessToken, refreshToken--|
  |                                    |
  |---(2) WebSocket 연결: /game------>|
  |<------ CONNECTED------------------|
  |                                    |
  |---(3) SUBSCRIBE------------------>|
  |     /sub/game/my/123/456          |
  |                                    | SimpleBroker에 구독 등록
  |                                    |
  |---(4) SEND (5초마다)------------->|
  |     /pub/game/update              |
  |     {bpm:150, distance:1000,...}  |
  |                                    |
  |                                    |-> GameWebSocketController.sendMessage()
  |                                    |-> service.updateGameHistory()
  |                                    |   (MongoDB 저장, 순위 계산)
  |                                    |-> template.convertAndSend()
  |                                    |
  |<---(5) MESSAGE---------------------|
  |     /sub/game/my/123/456          |
  |     {rank:3, distance:1000,...}   |
  |                                    |
  |--- UI 업데이트 (순위, 거리 표시) ---|
  |                                    |
  |---(4) SEND (5초 후 다시)--------->|
  |     {bpm:152, distance:1050,...}  |
  |                                    |
  |<---(5) MESSAGE---------------------|
  |     {rank:2, distance:1050,...}   | ← 순위가 올라감!
  |                                    |
```

---

## 🎯 핵심 포인트

### 1. 인증 방식
- **워치 전용 토큰**: Access Token(1시간) + Refresh Token(2주) 발급
- **자동 갱신**: 토큰 만료 시 자동 재발급
- **독립적인 토큰 관리**: 모바일과 워치의 refreshToken 별도 관리
- **API**:
  - `GET /v1/api/users/watch-connect-information/tokens?watchKey=xxx` - 토큰 발급
  - `POST /v1/api/auth/refresh` - 토큰 재발급

### 2. WebSocket 연결
- **하나의 엔드포인트**: 모든 경기가 `/game` 공유
- **Destination으로 구분**: `/sub/game/my/{gameId}/{userId}`로 개인별 라우팅
- **STOMP 프로토콜**: 구독, 메시지 전송, 수신 관리

### 3. 데이터 전송 주기
- **5초마다**: 배터리와 실시간성 밸런스
- **자동 전송**: Timer로 주기적 전송
- **센서 데이터**: 심박수, 케이던스, 거리, 속도 등

### 4. 응답 처리
- **구독 콜백**: 서버가 보낸 메시지를 자동으로 받음
- **UI 업데이트**: 순위, 거리, 완주 여부 실시간 표시
- **완주 처리**: `isDone: true` 수신 시 완료 화면 표시

### 5. 백그라운드 처리
- **Apple Watch**: `HKWorkoutSession` 사용
- **Wear OS**: `Foreground Service` 사용
- **연결 유지**: 운동 중 WebSocket 연결 지속


