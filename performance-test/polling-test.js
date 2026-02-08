import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const pollLatency = new Trend('poll_latency');

export const options = {
  stages: [
    { duration: '1m', target: 100 },    // Warm-up: 0 → 100명
    { duration: '2m', target: 500 },    // Load increase: 100 → 500명
    { duration: '2m', target: 1000 },   // Peak load: 500 → 1,000명
    { duration: '3m', target: 1000 },   // Sustain: 1,000명 유지
    { duration: '1m', target: 0 },      // Ramp-down: 1,000 → 0명
  ],
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
    'errors': ['rate<0.01'], // 에러율 < 1%
  },
};

export default function () {
  const baseUrl = __ENV.API_URL || 'http://localhost:8080';
  const gameId = __ENV.GAME_ID || '1';
  const userId = __ENV.USER_ID || '1';

  const res = http.get(`${baseUrl}/v1/api/game-histories/status?gameId=${gameId}&userId=${userId}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: { name: 'PollingAPI' },
  });

  const success = check(res, {
    'status is 200': (r) => r.status === 200,
    'response has data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true && body.data !== null;
      } catch (e) {
        return false;
      }
    },
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  errorRate.add(!success);
  pollLatency.add(res.timings.duration);

  // Adaptive Polling: 서버가 지정한 주기 사용
  let pollInterval = 3; // 기본값
  try {
    const body = JSON.parse(res.body);
    if (body.data && body.data.pollInterval) {
      pollInterval = body.data.pollInterval;
    }
  } catch (e) {
    // JSON 파싱 실패 시 기본값 사용
  }

  // Jitter 추가 (±500ms)
  const jitter = Math.random() - 0.5;
  const sleepTime = Math.max(1, pollInterval + jitter);

  sleep(sleepTime);
}

export function handleSummary(data) {
  return {
    'summary.json': JSON.stringify(data),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  const indent = options.indent || '';
  const enableColors = options.enableColors || false;

  return `
${indent}Polling Performance Test Summary
${indent}================================
${indent}
${indent}Total Requests: ${data.metrics.http_reqs.values.count}
${indent}Request Rate: ${data.metrics.http_reqs.values.rate.toFixed(2)} req/s
${indent}
${indent}Response Times:
${indent}  P50: ${data.metrics.http_req_duration.values['p(50)'].toFixed(2)}ms
${indent}  P95: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms
${indent}  P99: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms
${indent}
${indent}Success Rate: ${((1 - data.metrics.errors.values.rate) * 100).toFixed(2)}%
${indent}Failed Requests: ${data.metrics.http_req_failed ? data.metrics.http_req_failed.values.passes : 0}
  `;
}
