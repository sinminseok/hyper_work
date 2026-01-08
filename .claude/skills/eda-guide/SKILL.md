---
name: eda-guide
description: FunnyRun 프로젝트의 이벤트 주도 아키텍처(EDA) 가이드 인덱스. 이벤트/리스너 네이밍, Outbox 패턴, 구현 가이드 등의 상세 문서로 연결. "이벤트", "리스너", "네이밍", "Outbox", "Spring Event", "SNS/SQS" 관련 질문 시 사용.
---

# FunnyRun EDA 가이드

## 개요

FunnyRun은 **Hybrid EDA 아키텍처**를 사용합니다:
- **금전 거래**: Outbox 패턴 + SNS/SQS (결제, 상금, 환불)
- **일반 이벤트**: Spring Event (경기 신청, 취소, 알림)
- **조회**: 이벤트 사용 안 함

## 빠른 참고

### 핵심 네이밍
- Spring Event: `xxxEvent` / `xxxListener`
- SNS/SQS: `xxxMessage` / `xxxPayload` / `xxxConsumer`

### 이벤트 사용 기준
- ✅ 사용: 금전 거래, 부가 작업 2개 이상, 다른 도메인 영향
- ❌ 미사용: 단순 조회, 단순 CRUD

## 상세 문서

필요한 내용을 선택하여 확인하세요:

1. **네이밍 규칙** → `.claude/skills/eda-guide/references/naming-rules.md`
   - Event, Message, Payload, Listener, Consumer, Processor 네이밍 상세
   - JobType enum 관리

2. **Outbox 패턴** → `.claude/skills/eda-guide/references/outbox-pattern.md`
   - 패턴 목적과 동작 흐름
   - 주요 컴포넌트 역할

3. **클래스 역할** → `.claude/skills/eda-guide/references/class-roles.md`
   - run_core 이벤트 관련 클래스별 역할
   - 패키지 구조 및 책임

4. **구현 가이드** → `.claude/skills/eda-guide/references/implementation-guide.md`
   - Spring Event 구현 방법
   - SNS/SQS (Outbox) 구현 방법
   - 체크리스트

## 사용 방법

상세 정보가 필요하면 위 파일 경로를 Read tool로 읽어서 확인하세요.

예시:
```
네이밍 규칙이 궁금해
→ .claude/skills/eda-guide/references/naming-rules.md 읽기

Outbox 패턴 구현이 궁금해
→ .claude/skills/eda-guide/references/outbox-pattern.md 읽기

새 이벤트 구현 방법이 궁금해
→ .claude/skills/eda-guide/references/implementation-guide.md 읽기
```
