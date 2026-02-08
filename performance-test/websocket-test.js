import ws from 'k6/ws';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('ws_errors');
const messageLatency = new Trend('ws_message_latency');

export const options = {
  stages: [
    { duration: '1m', target: 100 },    // Warm-up: 0 → 100명
    { duration: '2m', target: 500 },    // Load increase: 100 → 500명
    { duration: '2m', target: 1000 },   // Peak load: 500 → 1,000명
    { duration: '3m', target: 1000 },   // Sustain: 1,000명 유지
    { duration: '1m', target: 0 },      // Ramp-down
  ],
  thresholds: {
    'ws_message_latency': ['p(95)<100', 'p(99)<200'],
    'ws_errors': ['rate<0.01'],
  },
};

export default function () {
  const wsUrl = __ENV.WS_URL || 'ws://localhost:8080/ws/game';
  const gameId = __ENV.GAME_ID || '1';
  const userId = __ENV.USER_ID || '1';

  const params = { tags: { name: 'WebSocketConnection' } };

  const res = ws.connect(wsUrl, params, function (socket) {
    socket.on('open', () => {
      // 구독 시작
      const subscribeMessage = JSON.stringify({
        action: 'subscribe',
        gameId: gameId,
        userId: userId
      });
      socket.send(subscribeMessage);
    });

    socket.on('message', (data) => {
      const startTime = new Date().getTime();

      const success = check(data, {
        'message received': (msg) => msg.length > 0,
        'valid JSON': (msg) => {
          try {
            JSON.parse(msg);
            return true;
          } catch (e) {
            return false;
          }
        },
      });

      errorRate.add(!success);

      const endTime = new Date().getTime();
      messageLatency.add(endTime - startTime);
    });

    socket.on('error', (e) => {
      if (e.error() != 'websocket: close sent') {
        errorRate.add(1);
        console.error('WebSocket error:', e.error());
      }
    });

    // 연결 유지 시간 (테스트 시간과 동일하게 설정)
    socket.setTimeout(function () {
      socket.close();
    }, 540000); // 9분 (전체 테스트 시간)
  });

  check(res, {
    'status is 101': (r) => r && r.status === 101,
  });
}

export function handleSummary(data) {
  return {
    'websocket-summary.json': JSON.stringify(data),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';

  return `
${indent}WebSocket Performance Test Summary
${indent}==================================
${indent}
${indent}Total Connections: ${data.metrics.ws_connecting ? data.metrics.ws_connecting.values.count : 0}
${indent}
${indent}Message Latency:
${indent}  P50: ${data.metrics.ws_message_latency ? data.metrics.ws_message_latency.values['p(50)'].toFixed(2) : 0}ms
${indent}  P95: ${data.metrics.ws_message_latency ? data.metrics.ws_message_latency.values['p(95)'].toFixed(2) : 0}ms
${indent}  P99: ${data.metrics.ws_message_latency ? data.metrics.ws_message_latency.values['p(99)'].toFixed(2) : 0}ms
${indent}
${indent}Error Rate: ${data.metrics.ws_errors ? (data.metrics.ws_errors.values.rate * 100).toFixed(2) : 0}%
  `;
}
