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
Lastpage_Project/
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
├── .env
├── .gitignore
├── uploads/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── Config/
│       │       │   ├── ChatbotConfig
│       │       │   ├── DataSourceConfig
│       │       │   ├── MybatisConfig
│       │       │   ├── SecurityConfig
│       │       │   └── WebConfig
│       │       │
│       │       ├── Controller/
│       │       │   ├── GlobalException/
│       │       │   │   └── GlobalExceptionHandler
│       │       │   ├── AdminDashboardController
│       │       │   ├── AdminEditInfoController
│       │       │   ├── AdminQnaController
│       │       │   ├── AdminReserveController
│       │       │   ├── AdminUserController
│       │       │   ├── ChatController
│       │       │   ├── CommunityCommentController
│       │       │   ├── CommunityPageController
│       │       │   ├── CommunityPostController
│       │       │   ├── EditInfoController
│       │       │   ├── ExceptionTestController
│       │       │   ├── FindController
│       │       │   ├── FindViewController
│       │       │   ├── FooterController
│       │       │   ├── FuneralServiceController
│       │       │   ├── GoodsServiceController
│       │       │   ├── HeaderController
│       │       │   ├── LoginController
│       │       │   ├── MypageController
│       │       │   ├── PaymentsController
│       │       │   ├── PaymentsViewController
│       │       │   ├── PsyServiceController
│       │       │   ├── QnaImageController
│       │       │   ├── ReserveController
│       │       │   ├── SigninController
│       │       │   ├── SignupController
│       │       │   ├── SimpleChatController
│       │       │   ├── SupportController
│       │       │   ├── SwaggerDemoController
│       │       │   └── UserProfileController
│       │       │
│       │       ├── Domain/
│       │       │   ├── Common/
│       │       │   │   ├── Dto/
│       │       │   │   ├── Entity/
│       │       │   │   └── Service/
│       │       │   │       ├── Admin/
│       │       │   │       │   ├── AuthService
│       │       │   │       │   ├── ChatSessionService
│       │       │   │       │   ├── CommentService
│       │       │   │       │   ├── CustomUserDetailsService
│       │       │   │       │   ├── EditInfoService
│       │       │   │       │   ├── EmailService
│       │       │   │       │   ├── FindService
│       │       │   │       │   ├── FollowService
│       │       │   │       │   ├── FuneralReserveService
│       │       │   │       │   ├── GoodsReserveService
│       │       │   │       │   ├── OAuthService
│       │       │   │       │   ├── OurpageReserveService
│       │       │   │       │   ├── PaymentsService
│       │       │   │       │   ├── PostService
│       │       │   │       │   ├── PsyReserveService
│       │       │   │       │   ├── QnaImageService
│       │       │   │       │   ├── QnaService
│       │       │   │       │   └── SignupService
│       │       │   │
│       │       │   └── Repository/
│       │       │       └── (JPA Repository Interfaces)
│       │       │
│       │       ├── support/
│       │       │   ├── OpenApiDocumentLoader
│       │       │   ├── VectorSearchService
│       │       │   └── ServletInitializer
│       │       │
│       │       └── DemoApplication.java
│       │
│       ├── resources/
│       │   ├── mapper/
│       │   ├── static/
│       │   │   ├── Asset/
│       │   │   ├── css/
│       │   │   │   ├── adminDashboard.css
│       │   │   │   ├── adminQna.css
│       │   │   │   ├── adminReserveList.css
│       │   │   │   ├── adminUserList.css
│       │   │   │   ├── chatbot-global.css
│       │   │   │   ├── common.css
│       │   │   │   ├── Community.css
│       │   │   │   ├── EditInfo.css
│       │   │   │   ├── f_service.css
│       │   │   │   ├── findId.css
│       │   │   │   ├── findPassword.css
│       │   │   │   ├── Funeral_reserve.css
│       │   │   │   ├── Goods.css
│       │   │   │   ├── Goods_reserve.css
│       │   │   │   ├── index.css
│       │   │   │   ├── Lastpage.css
│       │   │   │   ├── Mainpage.css
│       │   │   │   ├── Mypage.css
│       │   │   │   ├── Ourpage.css
│       │   │   │   ├── Ourpage_reserve.css
│       │   │   │   ├── Payments.css
│       │   │   │   ├── paymentsSuccess.css
│       │   │   │   ├── psy_reserve.css
│       │   │   │   ├── Psypage.css
│       │   │   │   ├── Signin.css
│       │   │   │   ├── Signup.css
│       │   │   │   └── Support.css
│       │   │   └── js/
│       │       │   ├── adminQna.js
│       │       │   ├── adminReserveList.js
│       │       │   ├── adminUserList.js
│       │       │   ├── chat-main-launcher.js
│       │       │   ├── common.js
│       │       │   ├── community.js
│       │       │   ├── doc-chat.js
│       │       │   ├── editInfo.js
│       │       │   ├── f_service.js
│       │       │   ├── findId.js
│       │       │   ├── findPassword.js
│       │       │   ├── footer.js
│       │       │   ├── funeralReserve.js
│       │       │   ├── goods.js
│       │       │   ├── goodsReserve.js
│       │       │   ├── header.js
│       │       │   ├── mainpage.js
│       │       │   ├── mypage.js
│       │       │   ├── ourpage.js
│       │       │   ├── ourpageReserve.js
│       │       │   ├── payments.js
│       │       │   ├── paymentsSuccess.js
│       │       │   ├── psyReserve.js
│       │       │   ├── signin.js
│       │       │   ├── signup.js
│       │       │   ├── simple-chat.js
│       │       │   ├── support.js
│       │       │   └── tuned-chat.js
│       │
│       │   ├── templates/
│       │   │   ├── aboutuspage/
│       │   │   │   └── aboutus.html
│       │   │   ├── admin/
│       │   │   │   ├── AdminDashboard.html
│       │   │   │   ├── AdminQna.html
│       │   │   │   ├── AdminReserveList.html
│       │   │   │   └── AdminUserList.html
│       │   │   ├── communitypage/
│       │   │   │   └── Community.html
│       │   │   ├── fragments/
│       │   │   │   └── chatbot-fragment.html
│       │   │   ├── funeralpage/
│       │   │   │   ├── f_service.html
│       │   │   │   └── Funeralpage.html
│       │   │   ├── goodspage/
│       │   │   │   └── Goods.html
│       │   │   ├── mainpage/
│       │   │   │   └── Mainpage.html
│       │   │   ├── mypage/
│       │   │   │   ├── EditInfo.html
│       │   │   │   └── Mypage.html
│       │   │   ├── ourpage/
│       │   │   │   └── ourpage.html
│       │   │   ├── payments/
│       │   │   │   ├── Payments.html
│       │   │   │   ├── PaymentsCancel.html
│       │   │   │   ├── PaymentsFailure.html
│       │   │   │   └── PaymentsSuccess.html
│       │   │   ├── post/
│       │   │   │   └── Comm-Post.html
│       │   │   ├── psy/
│       │   │   │   └── Psypage.html
│       │   │   ├── reserve/
│       │   │   │   ├── Funeral_reserve.html
│       │   │   │   ├── Goods_reserve.html
│       │   │   │   ├── Ourpage_reserve.html
│       │   │   │   └── psy_reserve.html
│       │   │   ├── signin/
│       │   │   │   ├── FindId.html
│       │   │   │   ├── FindPassword.html
│       │   │   │   └── Signin.html
│       │   │   ├── signup/
│       │   │   │   └── Signup.html
│       │   │   └── support/
│       │   │       ├── Support.html
│       │   │       ├── footer.html
│       │   │       ├── global_error.html
│       │   │       ├── header.html
│       │   │       └── index.html
│       │
│       │   ├── application.properties
│       │   └── application-docker.properties
│       │
│       └── webapp/
│
└── gradlew / gradlew.bat


