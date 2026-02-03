---
name: aws-architecture-guide
description: FunnyRun 프로젝트의 AWS 인프라 아키텍처 가이드. VPC, Subnet, ALB, EC2, RDS, NAT Instance, Security Group 설정과 트래픽 흐름을 설명. "AWS", "인프라", "VPC", "서브넷", "ALB", "로드밸런서", "보안그룹", "RDS", "NAT", "라우팅", "네트워크" 관련 질문 시 사용.
---

# FunnyRun AWS 아키텍처 가이드

## 아키텍처 개요

FunnyRun은 VPC 내에서 계층형 보안(Defense in Depth) 구조로 구성되어 있다.

```
인터넷 → ALB (Public) → EC2 (Private) → RDS (DB Subnet)
```

**핵심 구성 요소:**
- VPC: 10.0.0.0/16
- Public Subnet: ALB, NAT Instance
- Private Subnet: EC2 (api-image:8080, admin-image:8081)
- DB Subnet: RDS MySQL (Multi-AZ)

## 상세 아키텍처

상세 다이어그램과 각 컴포넌트 설정은 [architecture-detail.md](references/architecture-detail.md) 참조.

주요 내용:
- 서브넷 구성 및 CIDR
- 라우팅 테이블 설정
- 보안 그룹 규칙
- 트래픽 흐름 (인바운드/아웃바운드)

## 설계 원칙

| 원칙 | 적용 |
|------|------|
| 계층형 보안 | Public → Private → DB 순으로 보호 수준 증가 |
| 최소 권한 | 보안 그룹으로 필요한 통신만 허용 |
| 단일 진입점 | ALB를 통해서만 외부 접근 가능 |
| 비용 최적화 | NAT Gateway 대신 NAT Instance 사용 |
| 고가용성 준비 | 2개 AZ에 서브넷 배치 |

## 확장 시나리오

트래픽 증가 시:
1. EC2 스케일 업/아웃 (Auto Scaling Group)
2. RDS 스케일 업 또는 Read Replica 추가
3. NAT Instance → NAT Gateway 전환

고가용성 필요 시:
1. EC2 Multi-AZ 배포
2. RDS Multi-AZ 활성화
