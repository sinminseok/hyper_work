## FunnyRun
다양한 경기 타입(스피드, 심박수, 케이던스)에 대해 경기를 만들고, 사용자들이 참가해 경기를 진행하는
프로젝트. 순위별로 상금을 지급한다.


### 모듈 설명
- run_admin : 관리자 페이지(CMS)를 개발하고 관리하는 모듈
- run_api : REST API 관련 모듈
- run_core : run_admin, run_api 모듈에서 공통으로 사용하는 도메인 모듈

### 컨벤션
- 기본 자바 컨벤션을 준수한다.
- 배포는 Git Actions 을 통해 EC2에 도커 이미지를 배포한다.
  - ./docker-compose.yml 참고

---

## 프로젝트 구조
- 이벤트를 나타내는 클래스는 event 폴더 안에 XXXEvent 로 네이밍 한다.
- 이벤트 리스너는 listener 폴더 안에 xxxListener 로 네이밍 한다.
