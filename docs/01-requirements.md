# SKALA-SHOP 요구사항 명세서

원본: `shopping_api.pdf` (SKALA 스프링 실습과제). 이 문서는 PDF의 요구사항을 구현 우선순위(1~5순위) 기준으로
재정리한 것이다. 실행 순서와 승인 게이트는 `설계 계획` 문서(플랜)를 따른다.

## 0. 서비스 개요 (PDF 2p)

| 항목 | 내용 |
|---|---|
| 서비스 | SKALA-SHOP — 온라인쇼핑몰 백엔드 REST API |
| 목표 | 상품·고객·주문을 관리하는 API를 Controller-Service-Repository 계층형 구조로 구현 |
| 핵심 도메인 | 상품(Product), 고객(Customer), 주문상품(OrderItem — 고객이 주문한 상품, 1:N) |
| 사용자 | 쇼핑몰 고객(회원가입·로그인 후 상품을 주문) |
| 기술스택 | Spring Boot, JPA/H2, JWT 인증, Gradle |
| 기능범위 | 상품 CRUD, 고객관리/인증, 주문(담기), 주문취소 |

## 1. 사용자 여정 (User Journey, PDF 3p)

| 단계 | 고객 행동 | API |
|---|---|---|
| 1 | 회원가입: ID·비밀번호로 가입, 초기 포인트 지급 | `POST /api/customers` |
| 2 | 로그인: 로그인 성공 시 JWT 토큰 발급 | `POST /api/customers/login` |
| 3 | 상품조회: 판매상품 목록·상세 확인 | `GET /api/products` |
| 4 | 상품주문: 원하는 상품을 수량만큼 주문(포인트 차감) | `POST /api/customers/order` |
| 5 | 주문확인: 내가 주문한 상품목록 조회 | `GET /api/customers/{id}` |
| 6 | 주문취소: 주문취소(포인트 환급) | `POST /api/customers/cancel` |

## 2. 공통 비즈니스 규칙 & 예외처리 (PDF 4p)

- **포인트**: 고객은 보유 포인트로 주문한다. 포인트가 부족하면 주문을 거부한다(`INSUFFICIENT_FUNDS`).
- **주문수량**: 같은 상품을 재주문하면 수량이 누적된다. 취소 시 수량이 차감되며, 0이 되면 보유 항목을 삭제한다.
- **인증(JWT)**: 주문·취소는 로그인이 필수다. 토큰(Cookie)에서 고객을 식별한다.
- **입력검증**: 상품명·가격 등 필수값을 검증하고, 실패 시 `ParameterException`을 던진다.
- **예외처리**: 존재하지 않는 상품·고객을 조회하면 `DATA_NOT_FOUND`로 전역 예외 처리한다.
- **트랜잭션**: 주문·취소는 `@Transactional`로 원자적으로 처리해 포인트·수량의 일관성을 보장한다.

이 규칙들은 1순위(기본 검증/DATA_NOT_FOUND)부터 시작해 2순위(포인트·수량·트랜잭션)와 4순위(JWT 인증)에
걸쳐 단계적으로 강제된다. 세부 매핑은 아래 순위별 절 참고.

---

## 1순위 — CRUD 기능 구현

### 상품(Product) CRUD
- 상품 목록 조회: 페이지 단위(offset/count, 기본 0/10)로 전체 조회
- 상품 상세 조회: ID로 단건 조회, 없으면 `DATA_NOT_FOUND`
- 상품 등록: `productName`이 비어있거나 `productPrice`가 유효하지 않으면 `ParameterException`;
  이름이 이미 존재하면 `DATA_DUPLICATED`; 신규 등록 시 ID는 0L로 세팅해 JPA가 자동 생성
- 상품 수정: 입력값 검증 동일, 대상이 없으면 `DATA_NOT_FOUND`
- 상품 삭제: 대상이 없으면 `DATA_NOT_FOUND`

### 고객(Customer) CRUD
- 고객 목록 조회: 페이지 단위 조회
- 고객 상세 조회: `customerId`로 조회, 없으면 `DATA_NOT_FOUND`; 응답에 보유 주문상품 목록 포함
- 고객 등록(회원가입): `customerId`/`customerPassword`가 비어있으면 `ParameterException`;
  ID가 이미 존재하면 `DATA_DUPLICATED`; 등록 시 초기 적립 포인트 지급
- 고객 수정: `customerId`/`customerPoint` 유효성 확인, 없으면 `DATA_NOT_FOUND`
- 고객 삭제: 없으면 `DATA_NOT_FOUND`

### 공통 요구사항
- 모든 API 응답은 공통 `Response` 포맷을 사용한다.
- 목록 조회는 `PagedList`로 페이징 정보를 포함해 반환한다.

---

## 2순위 — 나머지 API + Swagger 문서화

### 로그인
- `customerId`/`customerPassword` 검증(`ParameterException`)
- 존재하지 않는 ID 조회 시 `DATA_NOT_FOUND`, 비밀번호 불일치 시 `NOT_AUTHENTICATED`
- 인증 성공 시 세션/토큰을 발급해 이후 요청에서 고객을 식별할 수 있게 한다 (이 시점은 JWT 이전 임시 구현, 4순위에서 교체)
- 응답 바디의 비밀번호는 null 처리

### 상품 주문 (`POST /api/customers/order`)
- 로그인 필요 — 미인증이면 이후 4순위에서 `NOT_AUTHENTICATED`로 차단(2순위 시점엔 세션 식별만 사용)
- 현재 로그인된 customerId로 customer/product 조회, 없으면 `DATA_NOT_FOUND`
- 포인트 충분성 체크 후 차감, 부족하면 `INSUFFICIENT_FUNDS`
- 이미 보유한 상품이면 수량 누적, 없으면 신규 보유 항목 생성
- `@Transactional`로 원자적 처리

### 주문 취소 (`POST /api/customers/cancel`)
- 취소 대상 customer/product 조회, 없으면 `DATA_NOT_FOUND`
- 보유 수량 검증, 취소 수량이 보유 수량을 초과하면 `INSUFFICIENT_QUANTITY`
- 수량 감소 또는(0이 되면) 삭제 처리 후 취소 금액만큼 포인트 환급
- `@Transactional`로 원자적 처리

### API 로그 (AOP)
- 모든 컨트롤러 API 호출에 대해 요청/응답/소요시간을 로깅한다.

### Swagger 문서화
- 전체 API가 `/swagger-ui.html`에서 조회·테스트 가능해야 한다 (PDF 35p: "Postman으로 전체 API 요청·응답 테스트" 요구사항을 Swagger로 보완).

---

## 3순위 — 그 외 새로운 기능 (PDF에 없는, 사용자 지정 확장 요구사항)

PDF 자체에는 없는 요구사항이며, 사용자가 지정한 5개 기능을 하나씩 순차 구현한다. 각 하위 단계는 완료 후 사용자
승인을 받아야 다음 단계로 진행한다.

### 3-1 장바구니
- 고객이 상품을 장바구니에 담고, 조회/삭제할 수 있어야 한다.
- 장바구니를 체크아웃하면 기존 주문 로직(포인트 차감, 수량 누적)이 그대로 적용되어야 한다.

### 3-2 주문 상세 및 내역 관리
- 현재 "보유 수량" 스냅샷(OrderItem)만으로는 주문/취소 이력을 알 수 없다.
- 주문·취소가 발생할 때마다 이력이 남아야 하고, 고객별로 시간 역순 이력 조회 및 개별 이력 상세 조회가 가능해야 한다.

### 3-3 상품 리뷰 및 평점
- 고객은 상품에 평점(1~5)과 코멘트를 남길 수 있어야 한다.
- **해당 상품을 구매(주문)한 이력이 있는 고객만 리뷰를 작성할 수 있다** (구매 안 한 고객이 작성 시도 시 거부).
  주문 후 취소했더라도 구매 이력 자체는 남아있으므로 리뷰 작성은 허용한다.
- 상품별 평균 평점을 조회할 수 있어야 하며, 범위를 벗어난 평점은 거부되어야 한다.
- 리뷰 삭제는 작성자 본인만 가능해야 한다.

### 3-4 위시리스트/찜하기
- 고객은 상품을 위시리스트에 추가/삭제/조회할 수 있어야 한다.
- 동일 상품을 중복으로 찜할 수 없어야 한다.

### 3-5 카테고리 및 검색
- 상품은 카테고리를 가질 수 있어야 한다(선택 값).
- 키워드와 카테고리를 조합해 상품을 검색할 수 있어야 한다.

---

## 4순위 — JWT 구현 (보류: 사용자 지시 전까지 착수하지 않음)

- 로그인 성공 시 실제 서명된 JWT를 발급하고 쿠키(`bff-access`)에 담아 응답한다 (PDF 5p 예시: `Set-Cookie: bff-access=<JWT>`).
- 이후 요청은 쿠키의 JWT에서 customerId를 추출해 고객을 식별한다 (PDF 4p: "토큰(Cookie)에서 고객을 식별").
- 주문/취소 및 3순위에서 만든 로그인 필요 기능(장바구니, 위시리스트)은 유효한 JWT가 없으면 `NOT_AUTHENTICATED`(401)로 거부한다.
- PDF의 build.gradle(12p)에는 Spring Security가 없으므로, Security 없이 순수 Filter/Interceptor 기반으로 구현한다.

## 5순위 — Docker 배포 (보류: 사용자 지시 전까지 착수하지 않음)

- Gradle로 빌드한 jar를 Docker 이미지로 패키징한다 (PDF 34p: `gradle build → docker build → docker push`).
- 컨테이너 하나에 내장 Tomcat + H2(in-memory)를 포함해 `docker run -p 8080:8080`으로 단독 실행 가능해야 한다.
- 외부 DB나 볼륨 없이, 클라이언트(브라우저/Postman)가 `:8080`으로 REST API를 호출할 수 있어야 한다.
