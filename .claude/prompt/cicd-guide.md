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

## IAM 역할 및 정책

### 1. EC2 역할 (EC2-SSM-Role)

EC2에 연결된 역할. 다음 정책이 필요:

- `AmazonSSMManagedInstanceCore` (SSM 접속용)
- `AmazonEC2RoleforAWSCodeDeploy` (CodeDeploy Agent용)
- `AmazonS3ReadOnlyAccess` (S3에서 배포 파일 다운로드)
- `CloudWatchLogsFullAccess` (선택: 애플리케이션 로그 전송)

### 2. CodeDeploy 역할 (FunnyRun-CodeDeploy-Role)

CodeDeploy 서비스 역할:

- `AWSCodeDeployRole`

### 3. GitHub Actions용 IAM 사용자 (github-actions-deploy)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3Access",
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject"],
      "Resource": "arn:aws:s3:::funnyrun-deploy-artifacts/*"
    },
    {
      "Sid": "CodeDeployAccess",
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment",
        "codedeploy:RegisterApplicationRevision",
        "codedeploy:GetDeploymentConfig"
      ],
      "Resource": "*"
    }
  ]
}
```

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

## EC2 초기 설정

### 1. CodeDeploy Agent 설치

```bash
sudo apt update
sudo apt install ruby-full wget -y
cd /home/ubuntu
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
sudo ./install auto
sudo systemctl start codedeploy-agent
sudo systemctl enable codedeploy-agent
sudo systemctl status codedeploy-agent
```

### 2. Docker 설치 확인

```bash
docker --version
docker compose version
```

### 3. 배포 디렉토리 확인

배포 파일 위치: `/home/ubuntu/funy-run/`

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

---

## 수동 배포/재시작

### 컨테이너 재시작

```bash
cd /home/ubuntu/funy-run
sudo docker compose down
sudo docker compose up -d
```

### 이미지 업데이트 후 재시작

```bash
cd /home/ubuntu/funy-run
sudo docker compose pull
sudo docker compose down
sudo docker compose up -d
```

---

## 트러블슈팅

### CodeDeploy 배포 실패

1. AWS 콘솔 → CodeDeploy → 배포 → 실패한 배포 클릭
2. 배포 수명 주기 이벤트에서 실패 단계 확인
3. 로그 보기 클릭

### EC2에서 로그 확인

```bash
# CodeDeploy Agent 로그
sudo tail -200 /var/log/aws/codedeploy-agent/codedeploy-agent.log

# 배포 스크립트 로그
sudo cat /opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log
```

### 흔한 에러

| 에러 | 원인 | 해결 |
|------|------|------|
| `exit code 127` | 명령어를 찾을 수 없음 | docker compose 설치 확인 |
| `Unknown database` | DB가 없음 | RDS에 데이터베이스 생성 |
| `permission denied` | 권한 문제 | sudo 사용 또는 IAM 역할 확인 |

### RDS 데이터베이스 생성

```bash
mysql -h <RDS_엔드포인트> -u <사용자명> -p
CREATE DATABASE run;
exit;
```

---

## 보안 그룹 설정

### EC2 보안 그룹

| 유형 | 포트 | 소스 |
|------|------|------|
| HTTP | 8080 | ALB 보안 그룹 |
| HTTP | 8081 | ALB 보안 그룹 |

### RDS 보안 그룹

| 유형 | 포트 | 소스 |
|------|------|------|
| MySQL | 3306 | EC2 보안 그룹 또는 10.0.10.0/24 |

### ALB 보안 그룹

| 유형 | 포트 | 소스 |
|------|------|------|
| HTTPS | 443 | 0.0.0.0/0 |
| HTTP | 80 | 0.0.0.0/0 |
