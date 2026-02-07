# FunnyRun CI/CD 가이드 (AWS CodeDeploy)

## 아키텍처 개요

```
GitHub (master push)
       ↓
GitHub Actions
   ├── Gradle 빌드
   ├── Docker 이미지 빌드 → DockerHub 푸시
   ├── 배포 아티팩트 ZIP 생성 → S3 업로드
   └── CodeDeploy 배포 트리거
       ↓
S3 Bucket (funnyrun-deploy-artifacts)
       ↓
CodeDeploy
       ↓
EC2 (Private Subnet) - CodeDeploy Agent
   ├── BeforeInstall: 기존 컨테이너 중지
   ├── AfterInstall: 파일 권한 설정
   ├── ApplicationStart: docker compose up
   └── ValidateService: 헬스체크
```

---

## AWS 인프라 구성

| 리소스 | 이름/설정 | 설명 |
|--------|-----------|------|
| VPC | - | 10.0.0.0/16 |
| Public Subnet | 10.0.1.0/24, 10.0.2.0/24 | NAT Instance, ALB |
| Private Subnet | 10.0.10.0/24 | EC2 (funny-run-ec2) |
| DB Subnet | 10.0.100.0/24 | RDS MySQL |
| S3 | funnyrun-deploy-artifacts | 배포 아티팩트 저장 |
| CodeDeploy App | FunnyRun | 애플리케이션 |
| CodeDeploy Group | FunnyRun-DeployGroup | 배포 그룹 |

---


---

## GitHub Secrets 설정

| Secret Name | 설명 |
|-------------|------|
| `DOCKERHUB_USERNAME` | DockerHub 사용자명 |
| `DOCKERHUB_TOKEN` | DockerHub Access Token |
| `AWS_ACCESS_KEY_ID` | GitHub Actions용 IAM Access Key |
| `AWS_SECRET_ACCESS_KEY` | GitHub Actions용 IAM Secret Key |
| `DB_RDS_URL` | RDS JDBC URL |
| `RDS_USERNAME` | RDS 사용자명 |
| `RDS_PASSWORD` | RDS 비밀번호 |
| `JWT_KEY` | JWT 시크릿 키 |
| `REDIS_HOST` | Redis 호스트 (localhost) |
| 기타 | .env 파일에 필요한 환경변수들 |

---

## 주요 파일 구조

```
/
├── .github/workflows/
│   └── deploy.yml          # GitHub Actions 워크플로우
├── appspec.yml             # CodeDeploy 배포 명세
├── scripts/
│   ├── before_install.sh   # 기존 컨테이너 중지
│   ├── after_install.sh    # 파일 권한 설정
│   ├── application_start.sh # docker compose up
│   └── validate_service.sh  # 헬스체크
├── docker-compose.yml      # Docker Compose 설정
└── run_api/
    └── Dockerfile
```

---


## 배포 흐름

### 1. 자동 배포 (master 푸시)

```bash
git add .
git commit -m "feat: 새로운 기능"
git push origin master
```

### 2. 모니터링

- **GitHub Actions**: GitHub 레포지토리 → Actions 탭
- **CodeDeploy**: AWS 콘솔 → CodeDeploy → 배포

### 3. EC2에서 확인

```bash
# SSM Session Manager로 접속 후
sudo su - ubuntu
cd /home/ubuntu/funy-run

# 컨테이너 상태 확인
sudo docker ps

# 로그 확인
sudo docker logs -f funy-run-api
sudo docker logs -f funy-run-admin
```
