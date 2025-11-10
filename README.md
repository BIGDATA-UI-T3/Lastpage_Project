# 🐾 LastPage Project  
> 반려동물의 마지막 길을 따뜻하게 동행하는 올인원 플랫폼  

---

## 프로젝트 소개

**LastPage**는 반려동물 장례 예약, 심리상담, 추모굿즈, 커뮤니티 기능을 통합한  
반려인들을 위한 토탈 케어 서비스입니다.  

> "사랑하는 반려동물의 마지막 순간을 함께하며, 기억을 오래도록 이어줍니다."

---

##  주요 기능

###  1. 장례 서비스
- 장례 일정 예약 / 조회 / 취소
- 사용자 맞춤형 장례 옵션 선택
- 실시간 예약 가능 여부 표시

###  2. 심리 상담
- 전문 심리상담사 예약 및 후기 작성
- 예약 관리(수정/삭제) 기능
- 실시간 상담 예약 상태 확인

###  3. 추모굿즈
- 맞춤형 추모 상품 주문 및 결제
- 결제 방식: **TossPay / KakaoPay / NaverPay / 카드결제**
- 주문 내역 및 결제 내역 조회

###  4. 서비스 이용 후기 & 추모공간
- 반려동물과의 추억을 공유하는 공간
- 사용자 간 댓글 및 공감 기능
- 게시글 신고 및 관리 기능

###  5. 회원관리
- 자체 회원가입 / 소셜 로그인 (Kakao, Naver, Google)
- 회원정보 수정 및 실시간 세션 갱신
- 비밀번호 강도 / 재사용 검사 실시간 검증

---

## 6. 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Spring Boot 3.x, JPA, MySQL, Gradle |
| **Frontend** | Thymeleaf, HTML5, CSS3 (Bootstrap 5), JavaScript |
| **Auth** | Kakao, Naver, Google OAuth 2.0 |
| **Infra** | Docker Compose, Redis, Kafka (Event Handling) |
| **Tooling** | IntelliJ IDEA, GitHub, Postman |
| **Security** | BCryptPasswordEncoder, Session 인증, HTTPS 적용 예정 |

---

##  7. 프로젝트 구조

```plaintext
LastPage-Project/
 ┣ 📁 src
 ┃ ┣ 📂 main
 ┃ ┃ ┣ 📂 java/com/example/demo
 ┃ ┃ ┃ ┣ 📂 Controller
 ┃ ┃ ┃ ┃ ┣ 
 ┃ ┃ ┃ ┃ ┗       
 ┃ ┃ ┃ ┣ 📂 Domain
 ┃ ┃ ┃ ┃ ┗ 📂 Common
 ┃ ┃ ┃ ┃ ┃ ┣ 📂 Entity
 ┃ ┃ ┃ ┃ ┃ ┃ 
 ┃ ┃ ┃ ┃ ┃ ┣ 📂 Dto
 ┃ ┃ ┃ ┃ ┃ ┣ 📂 Service
 ┃ ┃ ┃ ┃ ┃ ┗ 📂 Repository
 ┃ ┃ ┗ 📂 resources
 ┃ ┃ ┃ ┣ 📂 templates
 ┃ ┃ ┃ ┃ ┣ mypage/
 ┃ ┃ ┃ ┃ ┣ pay/
 ┃ ┃ ┃ ┗ 📂 static
 ┃ ┃ ┃ ┃ ┣ 📂 css
 ┃ ┃ ┃ ┃ ┃ 
 ┃ ┃ ┃ ┃ ┃ 
 ┃ ┃ ┃ ┃ ┣ 📂 js
 ┃ ┃ ┃ ┃ ┃
 ┃ ┃ ┃ ┃ ┗ 📂 img / Asset
 ┃ ┃ ┃ ┃ ┃ 
 ┣ 📄 build.gradle
 ┣ 📄 docker-compose.yml
 ┣ 📄 application.yml
 ┣ 📄 README.md
 ┗ 📄 LICENSE

