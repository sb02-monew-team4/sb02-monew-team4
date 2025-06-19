# sb02-monew-team4

### Test Coverage

[![codecov](https://codecov.io/gh/sb02-monew-team4/sb02-monew-team4/branch/dev/graph/badge.svg)](https://codecov.io/gh/sb02-monew-team4/sb02-monew-team4)

# Team4 소개합니다🍦

# 📝Team4 문서

[https://www.notion.so/201a8e208ca980a1849bd16a16563526](https://www.notion.so/201a8e208ca980a1849bd16a16563526?pvs=21)

---

## 👨‍👩‍👧‍👦팀원 구성

- 공한나 (https://github.com/HANNAKONG)
- 김태희 (https://github.com/WaiCat)
- 이영인 (https://github.com/ddday366)
- 정종현 (https://github.com/JJHyunDev)

---

## 🎈프로젝트 소개

MongoDB 및 PostgreSQL 백업/복구를 포함한 뉴스 기반 커뮤니티 플랫폼의 Spring 백엔드 시스템 구축

프로젝트 기간: 2025.05.24 ~ 2025.06.18

---

## 💸기술 스택

- Backend: Spring Boot, Spring Data JPA, QueryDSL, MapStruct
- Database: PostgreSQL, MongoDB
- Cloud: AWS
- API 문서화: Swagger
- 협업 도구: Discord, GitHub, Notion, Jira
- CI/CD: GitHub Actions
- 배포 인프라: AWS

---

## ⚙팀원별 구현 기능 상세 및 역할 분배

### 공한나

- **사용자(User) 관리 기능**
    - 회원가입, 로그인, 사용자 정보 수정, 탈퇴 기능 구현
    - Interceptor를 활용한 인증(Authentication), AOP를 활용한 인가(Authorization) 기능 구현
- **알림(Notification) 관리 기능**
    - 사용자 알림 도메인 설계 및 CRUD API 개발
    - 비동기 이벤트 핸들러 및 리스너를 활용한 알림 생성 로직 구현(구독 중인 관심사와 관련된 기사가 새로 등록된 경우, 작성한 댓글에 좋아요가 눌린 경우)
    - 1주일이 경과된 알림을 자동으로 삭제하는 배치 스케줄러 구현

---

### 김태희

- **사용자 활동 내역 관리**
    - MongoDB를 활용한 로그 데이터 저장 및 조회 기능 구현
- **공통 기능**
    - 예외 처리 글로벌 핸들러 구현
    - 로깅 기능 구축 (Logback 설정)
    - CI/CD 파이프라인 (GitHub Actions) 설정 및 AWS 배포 자동화
    - Docker 이미지 빌드 및 ECS 배포 스크립트 작성

---

### 이영인

- **관심사(Interest) 관리 기능**
    - 관심사 도메인 설계 및 CRUD API 개발
    - 관심사별 사용자 매핑 및 권한 체크 로직 구현
- **댓글(Comment) 관리 기능**
    - 댓글 생성, 조회, 수정, 삭제 기능 구현 (soft delete 포함)
    - 댓글 좋아요 기능과 관련 API 개발
    - 커서 기반 페이지네이션 적용 및 테스트 코드 작성

---

### 정종현

- **뉴스 기사(Article) 관리 기능**
    - 뉴스 기사 도메인 설계 및 기사 등록, 수정, 삭제 API 개발
    - 기사와 관심사 간 연관관계 매핑 및 데이터 처리
    - RSS 피드 수집기 및 기사 수집 스케줄러 구현
- **데이터 수집 자동화**
    - 외부 뉴스 오픈 API 연동 및 데이터 자동 동기화 구현
    - 스케줄러 및 비동기 처리 로직 작성

---

## 파일 구조

```
/project-root
 └── src
      └── main
          ├── java
          │    └── com.team4.monew
          │         ├── asynchronous
          │         ├── auth
          │         ├── config
          │         ├── controller
          │         ├── dto
          │         ├── entity
          │         ├── exception
          │         ├── interceptor
          │         ├── logging
          │         ├── mapper
          │         ├── repository
          │         ├── scheduler
          │         ├── service
          │         └── util
          └── resources
               ├── static
               ├── application.yml
               ├── logback-spring.xml
               └── schema.sql
```

---

## 구현 홈페이지

[http://43.202.40.114/](http://43.202.40.114/login)

---

## 프로젝트 자료

https://drive.google.com/drive/folders/1TIxxXv79T0Qvhs-gbnsSWo6qwkmuFjQr
